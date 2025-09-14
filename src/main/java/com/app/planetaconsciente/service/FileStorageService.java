package com.app.planetaconsciente.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import java.util.Set;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    public String storeFile(MultipartFile file, String subDirectory) {
        try {
            // Validar el tipo de archivo
            validateFile(file);

            // Sanitizar el nombre del archivo
            String originalFileName = sanitizeFileName(file.getOriginalFilename());
            
            // Extraer la extensión
            String fileExtension = getFileExtension(originalFileName);
            
            // Generar nombre único
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Crear directorio de destino
            Path uploadPath = Paths.get(uploadDir, subDirectory).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Guardar archivo
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return subDirectory + "/" + uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Error al almacenar el archivo " + file.getOriginalFilename(), ex);
        }
    }

    public void deleteFile(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return;
            }

            // Eliminar la parte de la URL si es necesario
            String relativePath = filePath.replaceFirst("/uploads/", "");
            Path pathToDelete = Paths.get(uploadDir, relativePath).normalize();

            // Validar que el path esté dentro del directorio permitido
            if (!pathToDelete.startsWith(Paths.get(uploadDir).normalize())) {
                throw new SecurityException("Intento de acceder a un directorio no permitido");
            }

            Files.deleteIfExists(pathToDelete);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el archivo: " + filePath, e);
        }
    }

    // Método para sanitizar nombres de archivo
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "";
        }

        // Eliminar parámetros de consulta
        fileName = fileName.split("\\?")[0];

        // Eliminar rutas completas
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

        // Eliminar caracteres no permitidos
        fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");

        return StringUtils.cleanPath(fileName);
    }

    // Obtener extensión del archivo
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex).toLowerCase();
        }
        return "";
    }

    // Validar archivo
    private void validateFile(MultipartFile file) {
        // Validar tipo MIME
        String fileContentType = file.getContentType();
        if (fileContentType == null || !ALLOWED_MIME_TYPES.contains(fileContentType)) {
            throw new RuntimeException("Tipo de archivo no permitido: " + fileContentType);
        }

        // Validar extensión
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new RuntimeException("Nombre de archivo no válido");
        }

        String fileExtension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new RuntimeException("Extensión de archivo no permitida: " + fileExtension);
        }
    }

    // Alias para compatibilidad
    public String store(MultipartFile file) {
        return storeFile(file, "");
    }
}