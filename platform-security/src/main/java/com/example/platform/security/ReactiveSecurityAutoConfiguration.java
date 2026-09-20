package com.example.platform.security;

import com.example.platform.security.jwt.JwtProperties;
import com.example.platform.security.jwt.ReactiveJwtAuthenticationConverters;
import com.example.platform.security.jwt.ReactiveJwtDecoderFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import reactor.core.publisher.Mono;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass({NimbusReactiveJwtDecoder.class, Mono.class})
@EnableConfigurationProperties(JwtProperties.class)
public class ReactiveSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ReactiveJwtDecoder reactiveJwtDecoder(JwtProperties properties) {
        return ReactiveJwtDecoderFactory.create(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter() {
        return ReactiveJwtAuthenticationConverters.rolesClaim();
    }
}
