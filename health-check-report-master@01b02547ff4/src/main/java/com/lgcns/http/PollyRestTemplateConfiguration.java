package com.lgcns.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.classic.Logger;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Setter
@Getter
@ConfigurationProperties("config.rest")
public class PollyRestTemplateConfiguration {
    
    private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
    
    private int maxConnTotal;
    private int maxConnPerRoute;
    private int connectTimeout;
    private int readTimeout;
    
    @Bean
    public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactoryBean() {
        HttpClient httpClient = HttpClientBuilder.create()
                                                    .setMaxConnTotal(maxConnTotal)
                                                    .setMaxConnPerRoute(maxConnPerRoute)
                                                    .build();
        
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpComponentsClientHttpRequestFactory.setHttpClient(httpClient);
        httpComponentsClientHttpRequestFactory.setConnectTimeout(connectTimeout);
        httpComponentsClientHttpRequestFactory.setReadTimeout(readTimeout);

        logger.info("==============Setting for RestTemplate");
        logger.info("- MAX_CONNECTION_POLL : " + maxConnTotal);
        logger.info("- MAX_CONNECTION_PER_ROUTE : " + maxConnPerRoute);
        logger.info("- MAX_CONNECTION_POLL : " + connectTimeout);
        logger.info("- MAX_CONNECTION_POLL : " + readTimeout);

        return httpComponentsClientHttpRequestFactory;
    }

    @Bean
    public RestTemplate restTemplate() {
        logger.info("==============Create RestTemplate");
        return new RestTemplate(httpComponentsClientHttpRequestFactoryBean());
    }
}
