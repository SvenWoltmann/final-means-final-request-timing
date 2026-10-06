package io.metricstack.requesttiming;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Registers the {@link RequestTimingFilter}. New in version 1.0.1, picked up
 * automatically through
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 *
 * <p>No order is set, so the filter runs after Spring Security's filter chain
 * (which sits at {@code SecurityProperties.DEFAULT_FILTER_ORDER = -100}). That is
 * why the filter sees the authenticated user.
 */
@AutoConfiguration
public class RequestTimingAutoConfiguration {

    @Bean
    public FilterRegistrationBean<RequestTimingFilter> requestTimingFilter() {
        FilterRegistrationBean<RequestTimingFilter> registration =
                new FilterRegistrationBean<>(new RequestTimingFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
