package br.com.backend.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration class for integrating components from the libintegration package.
 */
@Configuration
@ComponentScan(basePackages = "br.com.libintegration")
public class IntegrationAutoConfig {
}
