/**
 * 作者 Foxty
 * 描述 核心控制器，根据url获取具体的controller
 */

package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.common.Config;
import com.github.foxty.topaz.controller.anno.Controller;
import com.github.foxty.topaz.controller.anno.Endpoint;
import com.github.foxty.topaz.controller.interceptor.IInterceptor;
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
    private String contPackageName = "com.github.foxty.topaz.controller";
    private String viewBase = "/WEB-INF/view";
    private String cfgFilePath;
    private boolean xssFilterOn = true;

    private ConcurrentHashMap<String, Object> controllersCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IInterceptor> interceptorsCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, EndpointInfo> endpointMap = new ConcurrentHashMap<>();

    private ModuleNode rootNode = new ModuleNode("", null);

    private static Log log = LogFactory.getLog(CoreFilter.class);

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
            if (!contPackageName.endsWith("/")) {
                contPackageName += "/";
            }
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
        config.getServletContext().setAttribute("contextPath",
                config.getServletContext().getContextPath());

        log.info("Start scan controllers");

        if (StringUtils.isBlank(contPackageName)) {
            log.error("controllerPackage not defined in web.xml");
            throw new ControllerException("controllerPackage not defined in web.xml");
        }
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
                        log.error("Can nt find class " + clsPath, e);
                        continue;
                    }

                    if (cls.isAnnotationPresent(Controller.class)) {
                        initControllerAndEndpoints(cls);
                    }
                }
            } else {
                contPackageName += "." + f.getName();
                scanControllersInFolder(contPackageName, f);
            }
        }
    }

    private void initControllerAndEndpoints(Class contClazz) {

        Controller contAnno = (Controller) contClazz.getAnnotation(Controller.class);

        // Init all interceptors
        List<IInterceptor> interceptors = new LinkedList<>();
        Class tmpClazz = contClazz;
        while (tmpClazz != null) {
            Class<? extends IInterceptor>[] inters = contAnno.interceptors();
            if (inters != null) {
                for (Class<? extends IInterceptor> clazz : inters) {
                    try {
                        IInterceptor inter = interceptorsCache.get(clazz);
                        if (null == clazz) {
                            inter = clazz.newInstance();
                            interceptorsCache.putIfAbsent(clazz.getName(), inter);
                        }
                        interceptors.add(inter);
                    } catch (InstantiationException e) {
                        log.error("Initialize " + clazz + " failed!");
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
            controllersCache.putIfAbsent(contClazz.getName(), c);
            log.info("Init controller " + contClazz.getName());
        } catch (Exception e) {
            log.error(e);
            throw new ControllerException(e);
        }

        String baseUri = contAnno.uri();
        // Create endpoints
        Method[] methods = contClazz.getMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(Endpoint.class)) {
                EndpointInfo ep = new EndpointInfo(baseUri, interceptors, c, m);
                EndpointInfo oldValue = endpointMap.putIfAbsent(ep.getEndpointUri(), ep);
                if (null != oldValue) {
                    throw new ControllerException("Endpoint confilict bwteen " + oldValue + " and " + ep);
                }
            }
        }
    }

    /**
     * Find the endpoint
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
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
        if (uri.indexOf('.') <= 0 && uri.length() > 1) {
            WebContext ctx = WebContext.create(req, resp, viewBase);
            if (!xssFilterOn) {
                ctx.xssFilterOff();
            }

            String[] uriArr = uri.split("[/;]");
            // First path is root, so we start search from 1
            ControllerNode cn = findControllerNode(rootNode, 1, uriArr);
            BaseController c = null;
            if (cn != null) {
                ctx.setModuleName(cn.getParent().fullPath());
                ctx.setControllerName(cn.getNodeName());
                if (cn.getPos() + 1 < uriArr.length) {
                    ctx.setMethodName(uriArr[cn.getPos() + 1]);
                }
                c = cn.getController();
            } else {
                cn = rootNode.findControllerNode("root");
                c = (cn != null ? cn.getController() : null);
                ctx.setModuleName(rootNode.fullPath());
                ctx.setControllerName("root");
                // First path is root, so we use 1 as the method name
                ctx.setMethodName(uriArr[1]);
            }
            request.setAttribute("requestResource", ctx.getRequestResource());

            //execute(c);
        } else {
            chain.doFilter(request, response);
        }
    }

    private ControllerNode findControllerNode(ModuleNode node, int pos,
                                              String[] pNames) {
        if (pos >= pNames.length)
            return null;
        String pName = pNames[pos];
        if (node.hasController(pName)) {
            return node.findControllerNode(pName);
        }
        if (node.hasNode(pName)) {
            return findControllerNode(node.findNode(pName), ++pos, pNames);
        }
        return null;
    }

    private void execute(EndpointInfo endpoint) {
        WebContext ctx = WebContext.get();

        // Send 404 error to client if both current controller doesn't exist
        if (endpoint == null) {
            try {
                ctx.getResponse().sendError(404);
            } catch (IOException e1) {
            }
            return;
        }

        // TODO: find endpoint and execute it.
    }

    public void destroy() {
        log.info("CoreFilter has been destroyed!");
    }
}