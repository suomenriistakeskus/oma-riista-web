package fi.riista.api.advice;

import fi.riista.config.Constants;
import fi.riista.api.pub.PublicApiResource;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice(basePackageClasses = PublicApiResource.class)
public class JsonpControllerAdvice extends AbstractJsonpResponseBodyAdvice {
    protected JsonpControllerAdvice() {
        super("callback");
    }

    @Override
    protected MediaType getContentType(final MediaType contentType,
                                       final ServerHttpRequest request,
                                       final ServerHttpResponse response) {
        return new MediaType("text", "javascript", Constants.DEFAULT_CHARSET);
    }

    @Override
    protected void beforeBodyWriteInternal(final MappingJacksonValue bodyContainer,
                                           final MediaType contentType,
                                           final MethodParameter returnType,
                                           final ServerHttpRequest request,
                                           final ServerHttpResponse response) {
        final HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

        if (isGET(servletRequest) && isJsonpURI(servletRequest)) {
            super.beforeBodyWriteInternal(bodyContainer, contentType, returnType, request, response);
        }
    }

    private static boolean isGET(HttpServletRequest httpRequest) {
        return "GET".equals(httpRequest.getMethod().toUpperCase());
    }

    private static boolean isJsonpURI(HttpServletRequest httpRequest) {
        return httpRequest.getRequestURI().endsWith(".jsonp");
    }
}
