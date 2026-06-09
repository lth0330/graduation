package web.common.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
}
