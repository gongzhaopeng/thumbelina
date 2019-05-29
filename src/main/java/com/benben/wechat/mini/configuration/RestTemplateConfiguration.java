package com.benben.wechat.mini.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    @Primary
    public RestTemplate restTemplate() {

        final var restTemplate = new RestTemplate();

        setDefaultCharsetOfStringHttpMessageConverter(restTemplate,
                Charset.forName("UTF-8"));

        return restTemplate;
    }

    private void setDefaultCharsetOfStringHttpMessageConverter(
            RestTemplate restTemplate, Charset defaultCharset) {

        for (final var converter : restTemplate.getMessageConverters()) {

            if (converter instanceof StringHttpMessageConverter) {

                final var stringConverter =
                        (StringHttpMessageConverter) converter;

                stringConverter.setDefaultCharset(defaultCharset);
            }
        }
    }
}
