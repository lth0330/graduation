package web.common.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

class ParkingSnapshotStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveBase64ImageStoresParkingSnapshotUnderUploadsPath() throws Exception {
        ParkingSnapshotStorageService service = new ParkingSnapshotStorageService(tempDir);
        String imageBase64 = Base64.getEncoder().encodeToString("fake-jpeg-bytes".getBytes());

        String imagePath = service.saveBase64Image(imageBase64);

        assertThat(imagePath).startsWith("/uploads/parking-snapshots/");
        assertThat(imagePath).endsWith(".jpg");
        Path savedFile = tempDir.resolve(imagePath.replaceFirst("^/uploads/", "").replace("/", "\\"));
        assertThat(Files.readAllBytes(savedFile)).isEqualTo("fake-jpeg-bytes".getBytes());
    }

    @Test
    void saveBase64ImageAcceptsDataUrlPrefix() throws Exception {
        ParkingSnapshotStorageService service = new ParkingSnapshotStorageService(tempDir);
        String imageBase64 = Base64.getEncoder().encodeToString("png-bytes".getBytes());

        String imagePath = service.saveBase64Image("data:image/png;base64," + imageBase64);

        assertThat(imagePath).startsWith("/uploads/parking-snapshots/");
        assertThat(imagePath).endsWith(".png");
        Path savedFile = tempDir.resolve(imagePath.replaceFirst("^/uploads/", "").replace("/", "\\"));
        assertThat(Files.readAllBytes(savedFile)).isEqualTo("png-bytes".getBytes());
    }

    @Test
    void readObjectConvertsMissingS3ObjectToNotFoundResponse() {
        S3Client s3Client = mock(S3Client.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<S3Client> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(s3Client);
        when(s3Client.getObjectAsBytes(org.mockito.ArgumentMatchers.any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().statusCode(404).message("missing object").build());

        S3StorageService service = new S3StorageService(provider, "test-bucket");

        assertThatThrownBy(() -> service.readObject("parking-snapshots/missing.jpg"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
