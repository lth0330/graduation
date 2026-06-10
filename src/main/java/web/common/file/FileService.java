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
// 파일 저장 서비스: 업로드 파일을 서버 uploads 폴더에 저장하고 접근 경로를 반환한다.
public class FileService {

    private final Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "career-images");
    private final S3StorageService s3StorageService;

    public FileService(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    public String saveFile(MultipartFile file) {
        // Create: 업로드된 파일을 고유 파일명으로 저장한다.
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "career image file is required.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only image files can be uploaded.");
        }

        if (s3StorageService.isEnabled()) {
            return s3StorageService.uploadMultipartFile(file, "career-images");
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
