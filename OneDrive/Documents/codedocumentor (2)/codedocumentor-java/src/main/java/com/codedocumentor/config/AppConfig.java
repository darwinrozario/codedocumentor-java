package com.codedocumentor.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Application-wide configuration.
 * Loads .env variables and sets system properties so Spring's @Value annotations pick them up.
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

    static {
        // Load .env file from the working directory (mirrors Python's load_dotenv())
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(e -> {
                if (System.getenv(e.getKey()) == null) {
                    System.setProperty(e.getKey(), e.getValue());
                }
            });
        } catch (DotenvException ignored) {
            // .env is optional; environment variables take precedence
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve /static/** from classpath (mirrors Flask's static folder)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
