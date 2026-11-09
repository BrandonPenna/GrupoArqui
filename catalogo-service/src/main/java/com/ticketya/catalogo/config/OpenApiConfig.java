package com.ticketya.catalogo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Datos generales de la documentación OpenAPI (Swagger) del servicio
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TicketYa - catalogo-service")
                        .version("1.0")
                        .description("Lugares, sectores, eventos y búsqueda. En la Entrega 1 el usuario se identifica "
                                + "con los encabezados X-User-Id y X-User-Role (ADMIN, ORGANIZADOR, CLIENTE)."))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway"),
                        new Server().url("http://localhost:8081").description("Directo al servicio")));
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
