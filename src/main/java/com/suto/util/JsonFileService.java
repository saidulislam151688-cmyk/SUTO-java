package com.suto.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonFileService {

    private static final String DATA_DIRECTORY = "data";
    private final ObjectMapper objectMapper;

    public JsonFileService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        ensureDataDirectoryExists();
    }

    private void ensureDataDirectoryExists() {
        try {
            Path path = Paths.get(DATA_DIRECTORY);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data directory", e);
        }
    }

    private File getFilePath(String filename) {
        Path dirPath = Paths.get(DATA_DIRECTORY);
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new File(DATA_DIRECTORY, filename);
    }

    public <T> List<T> readList(String fileName, TypeReference<List<T>> typeReference) {
        File file = getFilePath(fileName);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(file, typeReference);
        } catch (IOException e) {
            System.err.println("Error reading " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public <T> void writeList(String fileName, List<T> data) {
        File file = getFilePath(fileName);
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to " + fileName, e);
        }
    }

    public <T> T readObject(String fileName, Class<T> clazz) {
        File file = getFilePath(fileName);
        if (!file.exists()) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create default instance", e);
            }
        }
        try {
            return objectMapper.readValue(file, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + fileName, e);
        }
    }

    public <T> void writeObject(String fileName, T data) {
        File file = getFilePath(fileName);
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to " + fileName, e);
        }
    }
}
