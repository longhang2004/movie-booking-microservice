package com.example.platform.security;

import com.example.platform.security.jwt.JwtAuthenticationConverters;
import com.example.platform.security.jwt.JwtDecoderFactory;
import com.example.platform.security.jwt.JwtProperties;
import com.example.platform.security.web.CorrelationIdFilter;
import com.example.platform.security.web.PlatformExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
@EnableConfigurationProperties(JwtProperties.class)
@Import(PlatformExceptionHandler.class)
public class ServletSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    JwtDecoder jwtDecoder(JwtProperties properties) {
        return JwtDecoderFactory.create(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        return JwtAuthenticationConverters.rolesClaim();
    }
}
