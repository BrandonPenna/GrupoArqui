package com.ticketya.ventas_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Datos generales de la documentación OpenAPI (Swagger) del servicio (Entrega 1 + Entrega 2 con JWT)
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ventasOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TicketYa - ventas-service")
                        .version("2.0")
                        .description("Reservas temporales, fila virtual, compras con QR, devoluciones, control de acceso QR y reportes. "
                                + "Soporta token Bearer JWT (Authorization: Bearer <token>) o encabezados X-User-Id y X-User-Role."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway"),
                        new Server().url("http://localhost:8082").description("Directo al servicio")));
    }

    // Los errores (4xx y 5xx) se documentan con el formato ProblemDetail que devuelve el GlobalExceptionHandler
    @Bean
    public OpenApiCustomizer erroresComoProblemDetail() {
        return openApi -> {
            Schema<?> problemDetail = new ObjectSchema()
                    .addProperty("title", new StringSchema().example("Conflict"))
                    .addProperty("status", new IntegerSchema().example(409))
                    .addProperty("detail", new StringSchema().example("Descripción del error"))
                    .addProperty("instance", new StringSchema().example("/api/..."));
            openApi.getComponents().addSchemas("ProblemDetail", problemDetail);

            openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation ->
                    operation.getResponses().forEach((codigo, respuesta) -> {
                        if (codigo.startsWith("4") || codigo.startsWith("5")) {
                            respuesta.setContent(new Content().addMediaType("application/problem+json",
                                    new MediaType().schema(new Schema<>().$ref("#/components/schemas/ProblemDetail"))));
                        }
                    })));
        };
    }
}
