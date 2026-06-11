package web.common.file;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService {

    private final ObjectProvider<S3Client> s3ClientProvider;
    private final String bucket;

    public S3StorageService(
            ObjectProvider<S3Client> s3ClientProvider,
            @Value("${spring.cloud.aws.s3.bucket:}") String bucket
    ) {
        this.s3ClientProvider = s3ClientProvider;
        this.bucket = bucket;
    }

    public boolean isEnabled() {
        return bucket != null && !bucket.isBlank() && s3ClientProvider.getIfAvailable() != null;
    }

    public String uploadMultipartFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "upload file is required.");
        }

        try {
            String objectKey = buildObjectKey(directory, file.getOriginalFilename());
            uploadBytes(objectKey, file.getBytes(), file.getContentType());
            return getPublicUrl(objectKey);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to read upload file.", exception);
        }
    }

    public String uploadBytes(byte[] bytes, String contentType, String directory, String fileName) {
        if (bytes == null || bytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "upload bytes are required.");
        }

        String objectKey = buildObjectKey(directory, fileName);
        uploadBytes(objectKey, bytes, contentType);
        return getPublicUrl(objectKey);
    }

    public boolean deleteByUrl(String fileUrl) {
        if (!isEnabled() || fileUrl == null || fileUrl.isBlank()) {
            return false;
        }

        String objectKey = extractObjectKey(fileUrl);
        if (objectKey == null || objectKey.isBlank()) {
            return false;
        }

        s3Client().deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build());
        return true;
    }

    private void uploadBytes(String objectKey, byte[] bytes, String contentType) {
        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey);

        if (contentType != null && !contentType.isBlank()) {
            requestBuilder.contentType(contentType);
        }

        s3Client().putObject(requestBuilder.build(), RequestBody.fromBytes(bytes));
    }

    private String getPublicUrl(String objectKey) {
        URL url = s3Client().utilities().getUrl(GetUrlRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build());
        return url.toString();
    }

    private S3Client s3Client() {
        S3Client s3Client = s3ClientProvider.getIfAvailable();
        if (s3Client == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 client is not configured.");
        }
        return s3Client;
    }

    private String buildObjectKey(String directory, String originalFileName) {
        String cleanDirectory = cleanDirectory(directory);
        String cleanFileName = cleanFileName(originalFileName);
        return cleanDirectory + "/" + UUID.randomUUID() + "_" + cleanFileName;
    }

    private String cleanDirectory(String directory) {
        if (directory == null || directory.isBlank()) {
            return "uploads";
        }
        return directory.replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String cleanFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "upload-file";
        }
        return originalFileName
                .replace("\\", "/")
                .replaceAll(".*/", "")
                .replaceAll("[\\\\/:*?\"<>|]", "-")
                .replaceAll("\\s+", "_");
    }

    private String extractObjectKey(String fileUrl) {
        int bucketIndex = fileUrl.indexOf(bucket);
        if (bucketIndex < 0) {
            return null;
        }

        String afterBucket = fileUrl.substring(bucketIndex + bucket.length());
        return afterBucket.replaceFirst("^\\.s3[.-][^/]+\\.amazonaws\\.com/", "")
                .replaceFirst("^\\.s3\\.amazonaws\\.com/", "")
                .replaceFirst("^/", "");
    }
}
