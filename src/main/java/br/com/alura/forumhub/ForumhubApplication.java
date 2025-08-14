package br.com.alura.forumhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@ComponentScan(basePackages = "br.com.alura.forumhub")
@OpenAPIDefinition(info = @Info(
		title = "Fórum Hub API Challenge",
		version = "1.3",
		description = "API REST da aplicação FórumHub, contendo funcionalidades de CRUD de tópicos, respostas e usuários."
))
/*@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT"
)*/

@SecurityRequirement(name = "bearerAuth")
public class ForumhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(ForumhubApplication.class, args);
	}
}