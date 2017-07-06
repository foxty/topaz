/**
 * 作者 Foxty
 * 描述 核心控制器，根据url获取具体的controller
 */

package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.common.Config;
import com.github.foxty.topaz.annotation._Controller;
import com.github.foxty.topaz.annotation._Endpoint;
import com.github.foxty.topaz.controller.interceptor.IInterceptor;
import com.sun.istack.internal.Nullable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Isaac Tian
 */
public class CoreFilter implements Filter {

    private static Log log = LogFactory.getLog(CoreFilter.class);
    public static final String DEFAULT_CONT_PACKAGE = "com.github.foxty.topaz.controller";
    public static final String DEFAULT_VIEW_BASE = "/WEB-INF/view";

    private String contPackageName = DEFAULT_CONT_PACKAGE;
    private String viewBase = DEFAULT_VIEW_BASE;
    private String cfgFilePath;
    private boolean xssFilterOn = true;

    private ConcurrentHashMap<String, Object> controllerMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IInterceptor> interceptorMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Endpoint> endpointMap = new ConcurrentHashMap<>();

    /*
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        String cBase = config.getInitParameter("controllerPackage");
        String vBase = config.getInitParameter("viewBase");
        String cFile = config.getInitParameter("configFile");
        String xssFilterFlag = config.getInitParameter("xssFilterOn");
        if (StringUtils.isNotBlank(cBase)) {
            contPackageName = cBase;
        }
        if (StringUtils.isNotBlank(vBase)) {
            viewBase = vBase;
        }
        if (StringUtils.isNotBlank(cFile)) {
            cfgFilePath = cFile;
        }
        if (StringUtils.isNotBlank(xssFilterFlag)) {
            xssFilterOn = Boolean.valueOf(xssFilterFlag);
        }

        log.info("Start load Config from file " + cfgFilePath);
        Config.init(new File(cfgFilePath));

        if (StringUtils.isBlank(contPackageName)) {
            log.error("controllerPackage not defined in web.xml");
            throw new ControllerException("controllerPackage not defined in web.xml");
        }
        log.info("Start scan controllers");
        scanControllers(contPackageName);
        log.info("Topaz initialized: contPackageName=" + contPackageName
                + ", viewBase=" + viewBase);
    }


    /**
     * Scan controllers in the package list and do the initialization.
     *
     * @param contPackageName
     */
    private void scanControllers(String contPackageName) {

        boolean recursive = true;
        String packageDirName = contPackageName.replace('.', '/');
        Enumeration<URL> dirs;
        URL url = Thread.currentThread().getContextClassLoader().getResource(
                packageDirName);
        File pFile = new File(url.getFile());
        if (pFile.exists()) {
            scanControllersInFolder(contPackageName, pFile);
        } else {
            log.warn("Package " + contPackageName + " not exist.");
        }

    }

    private void scanControllersInFolder(String contPackageName, File folder) {
        for (File f : folder.listFiles()) {
            String pName = f.getName();
            if (f.isFile()) {
                if (pName.endsWith(".class")) {
                    String clsPath = contPackageName + "." + pName.replace(".class", "");
                    Class cls = null;
                    try {
                        cls = Thread.currentThread().getContextClassLoader().loadClass(clsPath);
                    } catch (ClassNotFoundException e) {
                        log.error("Can not find class " + clsPath, e);
                        continue;
                    }

                    if (cls.isAnnotationPresent(_Controller.class)) {
                        initControllerAndEndpoints(cls);
                    }
                }
            } else {
                scanControllersInFolder(contPackageName + "." + f.getName(), f);
            }
        }
    }

    private void initControllerAndEndpoints(Class contClazz) {

        _Controller contAnno = (_Controller) contClazz.getAnnotation(_Controller.class);

        // Init all interceptors
        List<IInterceptor> interceptors = new LinkedList<>();
        Class tmpClazz = contClazz;
        while (tmpClazz != Object.class) {
            Class<? extends IInterceptor>[] interceptorClazzs = contAnno.interceptors();
            if (interceptorClazzs != null) {
                for (Class<? extends IInterceptor> interceptorClazz : interceptorClazzs) {
                    try {
                        IInterceptor inter = interceptorMap.get(interceptorClazz.getName());
                        if (null == inter) {
                            inter = interceptorClazz.newInstance();
                            interceptorMap.putIfAbsent(interceptorClazz.getName(), inter);
                        }
                        interceptors.add(inter);
                    } catch (InstantiationException e) {
                        log.error("Initialize " + interceptorClazz + " failed!");
                    } catch (IllegalAccessException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            tmpClazz = tmpClazz.getSuperclass();
        }

        // Instant the controller object.
        Object c = null;
        try {
            c = contClazz.newInstance();
            controllerMap.putIfAbsent(contClazz.getName(), c);
        } catch (Exception e) {
            log.error(e);
            throw new ControllerException(e);
        }

        // Create endpoints
        int epcount = 0;
        String baseUri = contAnno.uri();
        Method[] methods = contClazz.getMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(_Endpoint.class)) {
                Endpoint ep = new Endpoint(baseUri, interceptors, c, m);
                Endpoint oldValue = endpointMap.putIfAbsent(ep.getEndpointUri(), ep);
                if (null != oldValue) {
                    throw new ControllerException("EP URI conflict between " + oldValue + " and " + ep);
                }
                epcount++;
            }
        }
        log.info("C " + contClazz.getName() + " created with " + interceptors.size() + " interceptors, " + epcount + " endpoints.");
    }

    /**
     * Find the endpoint
     *
     * @param request HTTP Request
     * @param response HTTP Response
     * @param chain Filter Chain
     */
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.toString(), e);
        }
        response.setCharacterEncoding("UTF-8");
        String uri = req.getRequestURI().replaceFirst(req.getContextPath(), "")
                .toLowerCase();
        log.debug("Current uri = " + uri);

        // if its REST style
        if (uri.indexOf('.') <= 0) {
            // Search endpoint info by requested uri
            Endpoint endpoint = searchEndpoint(uri);
            if (null != endpoint) {
                WebContext ctx = WebContext.create(req, resp, viewBase);
                if (!xssFilterOn) {
                    ctx.xssFilterOff();
                }
                endpoint.execute();
                return;
            } else {
                log.warn("Can't find endpoint info for URI " + uri);
            }
        }

        // Execute the rest filter/servlet
        chain.doFilter(request, response);
    }

    /**
     *
     * @param uri
     * @return
     */
    @Nullable
    private Endpoint searchEndpoint(String uri) {
        return endpointMap.get(uri);
    }

    public void destroy() {
        log.info("CoreFilter has been destroyed!");
    }
}