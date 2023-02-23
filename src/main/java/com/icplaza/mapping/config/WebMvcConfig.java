package com.icplaza.mapping.config;

import com.icplaza.mapping.handler.UserIpInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public UserIpInterceptor userIpInterceptor() {
        return new UserIpInterceptor();
    }

    /**
     * 注册拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(userIpInterceptor())
                .addPathPatterns("/*").excludePathPatterns("/*.html").excludePathPatterns("/*swagger-resources");
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
