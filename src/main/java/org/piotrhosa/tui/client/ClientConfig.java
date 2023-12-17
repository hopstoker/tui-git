package org.piotrhosa.tui.client;

import org.piotrhosa.tui.config.ConfigProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {

    @Bean
    public ClientRequestInterceptor clientRequestInterceptor(ConfigProperties configProperties) {
        return new ClientRequestInterceptor(configProperties.getToken());
    }

    @Bean
    public RestTemplate restTemplate(ClientRequestInterceptor clientRequestInterceptor) {
        return new RestTemplateBuilder()
                .interceptors(clientRequestInterceptor)
                .build();
    }
}
