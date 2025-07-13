package com.server.spring.ws.el.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class MediaTypeConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(true) // Enable content negotiation via URL parameter
                .parameterName("format") // Specify the parameter name to look for in the URL
                .ignoreAcceptHeader(true)
                .defaultContentType(MediaType.APPLICATION_JSON) // Set the default media type
                .mediaType("json", MediaType.APPLICATION_JSON) // Map "json" to JSON
                .mediaType("xml", MediaType.APPLICATION_XML); // Map "xml" to XML
    }
}