package fi.riista.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy//(proxyTargetClass = true)
public class AopConfig {
    // Advice order, lower value = higher priority
    public static final int ORDER_TRANSACTION = 30;
    public static final int ORDER_METHOD_SECURITY = 40; // LOWEST priority
}
