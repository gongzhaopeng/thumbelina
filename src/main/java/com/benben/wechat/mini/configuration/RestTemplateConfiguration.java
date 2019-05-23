package com.benben.wechat.mini.configuration;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.KeyStore;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    @Primary
    public RestTemplate restTemplate() {

        return new RestTemplate();
    }

    @Bean
    @WepayCertRestTemplate
    public RestTemplate wepayCertRestTemplate(
            WechatPayConfiguration wepayConfig,
            RestTemplateBuilder builder) throws Exception {

        final var certPassword = wepayConfig.getMchId();

        final var keyStore = wepayKeyStore(wepayConfig.getCertPath(), certPassword);
        final var sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, certPassword.toCharArray())
                .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                .build();

        final var client = HttpClients.custom()
                .setSSLContext(sslContext)
                .build();

        return builder.requestFactory(() ->
                new HttpComponentsClientHttpRequestFactory(client))
                .build();
    }

    private KeyStore wepayKeyStore(String certPath, String certPassword)
            throws Exception {

        final var keyStore = KeyStore.getInstance("PKCS12");
        final var key = ResourceUtils.getFile(certPath);

        try (InputStream in = new FileInputStream(key)) {
            keyStore.load(in, certPassword.toCharArray());
        }

        return keyStore;
    }

    @Target({
            ElementType.CONSTRUCTOR, ElementType.FIELD,
            ElementType.METHOD, ElementType.TYPE,
            ElementType.PARAMETER
    })
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface WepayCertRestTemplate {
    }
}
