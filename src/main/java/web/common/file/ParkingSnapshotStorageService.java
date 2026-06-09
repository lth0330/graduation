package web.common.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ParkingSnapshotStorageService {

    private static final String PUBLIC_UPLOAD_PREFIX = "/uploads/parking-snapshots/";
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path uploadRoot;

    public ParkingSnapshotStorageService() {
        this(Paths.get(System.getProperty("user.dir"), "uploads"));
    }

    ParkingSnapshotStorageService(Path uploadRoot) {
        this.uploadRoot = uploadRoot;
    }

    public String saveBase64Image(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            return null;
        }

        ParsedImage parsedImage = parseImage(imageBase64.trim());
        byte[] imageBytes;
        try {
            imageBytes = Base64.getMimeDecoder().decode(parsedImage.base64Body());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "스냅샷 이미지 base64 형식이 올바르지 않습니다.");
        }

        Path snapshotDir = uploadRoot.resolve("parking-snapshots");
        String fileName = "parking-snapshot-"
                + LocalDateTime.now().format(FILE_TIME_FORMAT)
                + "-"
                + UUID.randomUUID()
                + parsedImage.extension();

        try {
            Files.createDirectories(snapshotDir);
            Files.write(snapshotDir.resolve(fileName), imageBytes);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "스냅샷 이미지 저장에 실패했습니다.");
        }

        return PUBLIC_UPLOAD_PREFIX + fileName;
    }

    private ParsedImage parseImage(String rawBase64) {
        if (!rawBase64.startsWith("data:")) {
            return new ParsedImage(rawBase64, ".jpg");
        }

        int commaIndex = rawBase64.indexOf(',');
        if (commaIndex < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "스냅샷 이미지 data URL 형식이 올바르지 않습니다.");
        }

        String header = rawBase64.substring(0, commaIndex).toLowerCase();
        String extension = header.contains("image/png") ? ".png" : ".jpg";
        return new ParsedImage(rawBase64.substring(commaIndex + 1), extension);
    }

    private record ParsedImage(String base64Body, String extension) {
    }
}
