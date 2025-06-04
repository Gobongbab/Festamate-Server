package com.gobongbob.festamate.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;

@Configuration
@EnableWebMvc
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        ArrayList<Server> servers = new ArrayList<>();
        servers.add(new Server().url("https://festamate.shop").description("FestaMate Server"));
        servers.add(new Server().url("http://localhost:8080").description("Local Server"));

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .name("JWT Authentication")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("access token을 넣어주세요!")))
                .info(apiInfo())
                .servers(servers)
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    private Info apiInfo() {
        return new Info()
                .title("FestaMate REST API Swagger Test Page")
                .description("made by FestaMate Backend Team")
                .contact(new Contact()
                        .name("FestaMAte BE GitHub")
                        .url("https://github.com/Gobongbab/Festamate-Server"))
                .version("1.0.0");
    }
}
