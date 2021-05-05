package no.nav.henvendelse.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.service.ApiKey
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfig {
    @Bean
    fun docket(): Docket = Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis { handler ->
            handler
                ?.key()
                ?.pathMappings
                ?.any { it.startsWith("/api") }
                ?: false
        }
        .build()
        .apply {
            val scheme = ApiKey("Authorization", "Authorization", "header")
            val context = SecurityContext.builder()
                .securityReferences(listOf(SecurityReference("Authorization", arrayOf())))
                .build()

            securitySchemes(listOf(scheme))
            securityContexts(listOf(context))
        }
}
