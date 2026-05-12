package web.common.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileService {

    private final Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "career-images");

    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "career image file is required.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only image files can be uploaded.");
        }

        try {
            Files.createDirectories(uploadDir);

            String fileName = UUID.randomUUID() + "_" + cleanFileName(file.getOriginalFilename());
            Path savePath = uploadDir.resolve(fileName);

            file.transferTo(savePath);

            return Paths.get("uploads", "career-images", fileName).toString().replace("\\", "/");
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to save file.", exception);
        }
    }

    private String cleanFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "career-image";
        }

        return Paths.get(originalFileName)
                .getFileName()
                .toString()
                .replaceAll("[\\\\/:*?\"<>|]", "-")
                .replaceAll("\\s+", "_");
    }
}
