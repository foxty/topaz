package com.github.foxty.topaz.controller;

import com.github.foxty.topaz.common.TopazUtil;
import com.github.foxty.topaz.controller.anno.Endpoint;
import com.github.foxty.topaz.controller.interceptor.FinalInterceptor;
import com.github.foxty.topaz.controller.interceptor.IInterceptor;
import com.github.foxty.topaz.controller.interceptor.InterceptorChain;
import com.github.foxty.topaz.dao.DaoManager;
import com.github.foxty.topaz.dao.ITransVisitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * EndpointInfo stand for an access point, which target to a method of a controller.
 * It has interceptors, baseUri mapping, controller instance and method instance.
 * <p>
 * Created by itian on 6/13/2017.
 */
public class EndpointInfo {

    private static Log log = LogFactory.getLog(EndpointInfo.class);

    private String baseUri;
    private String methodUri;
    private Object controller;
    private List<IInterceptor> interceptorList;
    private Method method;
    private HttpMethod allowHttpMethod;
    private boolean isTransactional;

    public EndpointInfo(String baseUri, List<IInterceptor> interceptorList, Object controller, Method method) {
        this.baseUri = baseUri;
        this.interceptorList = interceptorList;
        this.controller = controller;
        this.method = method;

        init();
    }

    private void init() {
        Objects.requireNonNull(interceptorList, "InterceptorList should not be null.");
        Endpoint epAnno = method.getAnnotation(Endpoint.class);
        Objects.requireNonNull(epAnno, "@Endpoint should not be null.");
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
        HttpMethod reqeustMethod = HttpMethod.valueOf(wc.getRequest().getMethod());
        if (log.isDebugEnabled()) {
            log.debug("Access endpoiont: " + this);
        }

        if (allowHttpMethod != reqeustMethod) {
            throw new ControllerException("Request method " + this
                    + " is not allowd!");
        }
        // interceptors chain
        InterceptorChain chain = new InterceptorChain(interceptorList);
        if (isTransactional) {
            if (log.isDebugEnabled()) {
                log.debug("Wrap transaction on " + this.toString() + ".");
            }
            DaoManager.getInstance().useTransaction(new ITransVisitor() {
                public void visit() {
                    chain.proceed();
                }
            });
        } else {
            chain.proceed();
        }
    }
}
