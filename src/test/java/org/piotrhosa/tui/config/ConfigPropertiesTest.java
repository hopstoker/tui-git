package org.piotrhosa.tui.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class ConfigPropertiesTest {

    @Autowired
    ConfigProperties configProperties;

    @Test
    public void getTokenSucceeds() {
        assertEquals("https://api.github.com", configProperties.getUrl());
        assertEquals("test-token", configProperties.getToken());
    }
}
