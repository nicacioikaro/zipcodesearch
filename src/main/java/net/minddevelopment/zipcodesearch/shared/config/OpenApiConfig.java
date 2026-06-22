package net.minddevelopment.zipcodesearch.shared.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(
                    new Info()
                            .title("Zipcode Search API")
                            .version("1.0.0")
                            .description("API for Brazilian zipcode (CEP) lookup and street search, with multi-source resolution and resilience.")
                            .contact(new Contact()
                                    .name("Ikaro Nicacio")
                                    .email("devnicacio@email.com")
                                    .url("https://github.com/nicacioikaro"))
                            .license(new License()
                                    .name("MIT")
                                    .url("https://opensource.org/licenses/MIT")
                            )
            );
    }
}
