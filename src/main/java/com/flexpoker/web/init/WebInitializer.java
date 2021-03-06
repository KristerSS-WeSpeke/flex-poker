package com.flexpoker.web.init;

import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.flexpoker.config.RedisConfig;
import com.flexpoker.config.SecurityConfig;
import com.flexpoker.config.WebConfig;
import com.flexpoker.config.WebSocketConfig;

public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { SecurityConfig.class, RedisConfig.class, WebSocketConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { WebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    protected void customizeRegistration(Dynamic registration) {
        registration.setAsyncSupported(true);
    }

}
