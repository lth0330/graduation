package python.service;

import app.repository.RegisteredCarRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.parking.repository.ResidentVehicleRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PythonGateService {

    private final ResidentVehicleRepository residentVehicleRepository;
    private final RegisteredCarRepository registeredCarRepository;

    public Map<String, Object> checkPlate(String plate) {
        String normalizedPlate = normalizePlate(plate);
        boolean isRegistered = normalizedPlate != null
                && (residentVehicleRepository.existsByNumber(normalizedPlate)
                || registeredCarRepository.existsByNumber(normalizedPlate)
                || existsByCompactPlate(normalizedPlate));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("is_resident", isRegistered);
        return response;
    }

    private String normalizePlate(String plate) {
        if (plate == null || plate.isBlank()) {
            return null;
        }
        return plate.trim();
    }

    private boolean existsByCompactPlate(String plate) {
        String compactPlate = compact(plate);
        return residentVehicleRepository.findAll()
                .stream()
                .anyMatch(vehicle -> compact(vehicle.getNumber()).equals(compactPlate))
                || registeredCarRepository.findAll()
                .stream()
                .anyMatch(vehicle -> compact(vehicle.getNumber()).equals(compactPlate));
    }

    private String compact(String plate) {
        if (plate == null) {
            return "";
        }
        return plate.replaceAll("\\s+", "");
    }
}
