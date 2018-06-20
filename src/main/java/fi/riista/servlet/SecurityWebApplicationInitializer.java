package fi.riista.servlet;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

@Order(200)
public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
}
