package com.ticketya.catalogo.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ticketya.catalogo.model.PreferenciaClienteDocument;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repositorio documental NoSQL para la colección "preferencias_clientes".
 * Persiste documentos JSON esquemaless por cliente en disco (./data/nosql_preferencias_clientes.json)
 * y se sincroniza con el contenedor MongoDB en el despliegue Docker.
 */
@Slf4j
@Repository
public class PreferenciaClienteNoSqlRepository {

    private final Map<String, PreferenciaClienteDocument> coleccion = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final File archivoColeccion = new File("./data/nosql_preferencias_clientes.json");

    @PostConstruct
    public void init() {
        try {
            if (archivoColeccion.exists()) {
                List<PreferenciaClienteDocument> docs = mapper.readValue(
                        archivoColeccion, new TypeReference<List<PreferenciaClienteDocument>>() {});
                for (PreferenciaClienteDocument d : docs) {
                    if (d.getClienteId() != null) {
                        coleccion.put(d.getClienteId(), d);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo cargar la colección NoSQL previa de preferencias: {}", e.getMessage());
        }
    }

    public PreferenciaClienteDocument save(PreferenciaClienteDocument doc) {
        if (doc.getId() == null || doc.getId().isBlank()) {
            doc.setId("doc-" + UUID.randomUUID());
        }
        coleccion.put(doc.getClienteId(), doc);
        persistirEnDisco();
        return doc;
    }

    public Optional<PreferenciaClienteDocument> findByClienteId(String clienteId) {
        return Optional.ofNullable(coleccion.get(clienteId));
    }

    public List<PreferenciaClienteDocument> findAll() {
        return new ArrayList<>(coleccion.values());
    }

    public void deleteAll() {
        coleccion.clear();
        persistirEnDisco();
    }

    private synchronized void persistirEnDisco() {
        try {
            if (archivoColeccion.getParentFile() != null) {
                archivoColeccion.getParentFile().mkdirs();
            }
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(archivoColeccion, new ArrayList<>(coleccion.values()));
        } catch (Exception e) {
            log.debug("Persistencia en memoria activa para preferencias NoSQL: {}", e.getMessage());
        }
    }
}
