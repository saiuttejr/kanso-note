package com.bankingoop.finance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Web security configuration with CORS policy and security headers.
 */
@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false);
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("http://127.0.0.1:8080");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public Filter securityHeadersFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                if (response instanceof HttpServletResponse httpResponse) {
                    httpResponse.setHeader("X-Content-Type-Options", "nosniff");
                    httpResponse.setHeader("X-Frame-Options", "DENY");
                    httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
                    httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                    httpResponse.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
                }
                chain.doFilter(request, response);
            }
        };
    }
}
