package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.common.TopazUtil;
import com.github.foxty.topaz.controller.anno.EP;
import com.github.foxty.topaz.controller.interceptor.FinalInterceptor;
import com.github.foxty.topaz.controller.interceptor.IInterceptor;
import com.github.foxty.topaz.controller.interceptor.InterceptorChain;
import com.github.foxty.topaz.dao.DaoManager;
import com.github.foxty.topaz.dao.ITransVisitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Endpoint stand for an access point, which target to a method of a controller.
 * It has interceptors, baseUri mapping, controller instance and method instance.
 * <p>
 * Created by itian on 6/13/2017.
 */
public class Endpoint {

    private static Log log = LogFactory.getLog(Endpoint.class);

    private String baseUri;
    private String methodUri;
    private Object controller;
    private List<IInterceptor> interceptorList;
    private Method method;
    private HttpMethod allowHttpMethod;
    private boolean isTransactional;

    public Endpoint(String baseUri, List<IInterceptor> interceptorList, Object controller, Method method) {
        this.baseUri = baseUri;
        this.interceptorList = new ArrayList<>(interceptorList);
        this.controller = controller;
        this.method = method;

        init();
    }

    private void init() {
        Objects.requireNonNull(interceptorList, "InterceptorList should not be null.");
        EP epAnno = method.getAnnotation(EP.class);
        Objects.requireNonNull(epAnno, "@EP should not be null.");
        methodUri = epAnno.uri();
        allowHttpMethod = epAnno.method();
        isTransactional = epAnno.isTransactional();

        FinalInterceptor fin = new FinalInterceptor(controller, method);
        interceptorList.add(fin);

        baseUri = TopazUtil.cleanUri(baseUri);
        methodUri = TopazUtil.cleanUri(methodUri);
    }

    public String getBaseUri() {
        return baseUri;
    }

    public String getEndpointUri() {
        return TopazUtil.cleanUri(baseUri + "/" + methodUri);
    }

    public void execute() {
        WebContext wc = WebContext.get();
        HttpMethod requestMethod = HttpMethod.valueOf(wc.getRequest().getMethod());
        if (log.isDebugEnabled()) {
            log.debug("Access endpoint: " + this);
        }

        if (allowHttpMethod != requestMethod) {
            wc.getResponse().setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            log.warn("Endpoint " + this + " is not support method " + requestMethod);
            return;
        }
        // interceptor chain
        InterceptorChain chain = new InterceptorChain(interceptorList);
        if (isTransactional) {
            if (log.isDebugEnabled()) {
                log.debug("Wrap transaction on " + this.toString() + ".");
            }
            DaoManager.getInstance().useTransaction(() ->{
                    chain.proceed();
            });
        } else {
            chain.proceed();
        }
    }

    public String toString() {
        return "Endpoint: " + this.getEndpointUri() + " on " +
                controller.getClass().getName() + "." + method.getName();
    }
}
