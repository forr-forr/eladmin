package com.itheima.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AESConfig {
    @Value("${aes.secret.key}")
    private String aesKey;

    public String getAesKey() {
        return aesKey;
    }
}
