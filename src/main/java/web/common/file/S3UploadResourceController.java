package web.common.file;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class S3UploadResourceController {

    private static final String S3_UPLOAD_PREFIX = "/uploads/s3/";

    private final S3StorageService s3StorageService;

    @GetMapping("/uploads/s3/**")
    public ResponseEntity<byte[]> readS3Upload(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String objectKey = requestPath.substring(requestPath.indexOf(S3_UPLOAD_PREFIX) + S3_UPLOAD_PREFIX.length());
        S3StorageService.StoredS3Object storedObject = s3StorageService.readObject(objectKey);

        return ResponseEntity.ok()
                .contentType(resolveMediaType(storedObject.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(storedObject.bytes().length))
                .body(storedObject.bytes());
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return MediaType.parseMediaType(contentType);
    }
}
