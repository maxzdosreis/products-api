package com.maxzdosreis.products_api.config;

import com.maxzdosreis.products_api.interceptor.SimpleRateLimitInterceptor;
import com.maxzdosreis.products_api.serialization.converter.CustomMediaTypes;
import com.maxzdosreis.products_api.serialization.converter.YamlJackson2HttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.yaml.snakeyaml.Yaml;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SimpleRateLimitInterceptor rateLimitInterceptor;

    // Registra interceptors customizados
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                // Aplica rate limiting nestes paths
                .addPathPatterns("/auth/**", "/api/**")
                // NÃO aplica rate limiting nestes paths
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorParameter(false)
                .ignoreAcceptHeader(false)
                .useRegisteredExtensionsOnly(false)
                .defaultContentType(MediaType.APPLICATION_JSON)
                    .mediaType("json", MediaType.APPLICATION_JSON)
                    // adicionado as extensões xml e yaml, para no futuro produzir ou consumir outras extensões além de json
                    .mediaType("xml", MediaType.APPLICATION_XML)
                    .mediaType("yaml", CustomMediaTypes.APPLICATION_YAML);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new YamlJackson2HttpMessageConverter());
    }
}
