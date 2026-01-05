
package br.com.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Main application class for the Spring Boot backend application.
 * It scans multiple base packages to include components from different modules.
 */
@SpringBootApplication(scanBasePackages = {
        "br.com.backend",
        "br.com.libintegration",
        "br.com.libdomain.strategy"
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
