package org.skillbridge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI skillBridgeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SkillBridge NG API")
                        .description("Skills, jobs, and freelance marketplace for Nigeria.")
                        .version("v0.1.0")
                        .contact(new Contact().name("SkillBridge NG")))
                .servers(List.of(new Server().url("http://localhost:8081").description("Local")));
    }
}