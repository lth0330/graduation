package web.parking;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ParkingSampleDataTest {

    private static final Pattern PARKING_ZONE_ROW = Pattern.compile(
            "\\(\\s*(\\d+),\\s*(\\d+),\\s*'([^']+)',\\s*'[^']*',\\s*'[^']*',\\s*'[^']*',\\s*"
                    + "(\\d+),\\s*(\\d+),\\s*(\\d+),\\s*(\\d+),"
    );

    @Test
    void parkingZoneSampleDataUsesLayoutSizeColumnsAndDoesNotOverlap() throws IOException {
        String sql = readProjectFile("src/main/resources/sql/data.sql");

        assertThat(sql).contains("layout_width", "layout_height");

        List<SampleParkingZone> zones = parseParkingZones(sql);
        assertThat(zones).isNotEmpty();

        for (int i = 0; i < zones.size(); i++) {
            for (int j = i + 1; j < zones.size(); j++) {
                SampleParkingZone first = zones.get(i);
                SampleParkingZone second = zones.get(j);

                if (first.parkingLotNo != second.parkingLotNo) {
                    continue;
                }

                assertThat(overlaps(first, second))
                        .as("%s and %s should not overlap", first.areaNumber, second.areaNumber)
                        .isFalse();
            }
        }
    }

    @Test
    void awsSchemaHasLayoutSizeColumns() throws IOException {
        String sql = readProjectFile("src/main/java/aws_db.sql");

        assertThat(sql).contains("layout_width INT", "layout_height INT");
    }

    private String readProjectFile(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath));
    }

    private List<SampleParkingZone> parseParkingZones(String sql) {
        List<SampleParkingZone> zones = new ArrayList<>();
        Matcher matcher = PARKING_ZONE_ROW.matcher(sql);

        while (matcher.find()) {
            zones.add(new SampleParkingZone(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    matcher.group(3),
                    Integer.parseInt(matcher.group(4)),
                    Integer.parseInt(matcher.group(5)),
                    Integer.parseInt(matcher.group(6)),
                    Integer.parseInt(matcher.group(7))
            ));
        }

        return zones;
    }

    private boolean overlaps(SampleParkingZone first, SampleParkingZone second) {
        int firstRowEnd = first.layoutRow + first.layoutHeight - 1;
        int firstColumnEnd = first.layoutColumn + first.layoutWidth - 1;
        int secondRowEnd = second.layoutRow + second.layoutHeight - 1;
        int secondColumnEnd = second.layoutColumn + second.layoutWidth - 1;

        boolean rowOverlaps = first.layoutRow <= secondRowEnd && firstRowEnd >= second.layoutRow;
        boolean columnOverlaps = first.layoutColumn <= secondColumnEnd && firstColumnEnd >= second.layoutColumn;
        return rowOverlaps && columnOverlaps;
    }

    private record SampleParkingZone(
            int parkingZoneNo,
            int parkingLotNo,
            String areaNumber,
            int layoutRow,
            int layoutColumn,
            int layoutWidth,
            int layoutHeight
    ) {
    }
}
