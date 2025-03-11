package org.telebotv1.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "secrets.telegram")
public class AllowedClientsConfig {

    private List<String> allowedClients;
}
