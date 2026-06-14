package web.notification.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import web.notification.dto.ManagerNotificationDto;
import web.notification.service.ManagerNotificationService;

@RestController
@RequiredArgsConstructor
public class ManagerNotificationController {

    private final ManagerNotificationService managerNotificationService;

    @GetMapping("/api/manager-notifications")
    public ResponseEntity<List<ManagerNotificationDto>> findMyNotifications(
            @AuthenticationPrincipal Map<String, Object> principal
    ) {
        return ResponseEntity.ok(managerNotificationService.findMyNotifications(principal));
    }

    @GetMapping("/api/manager-notifications/{notificationNo}")
    public ResponseEntity<ManagerNotificationDto> findMyNotification(
            @AuthenticationPrincipal Map<String, Object> principal,
            @PathVariable Integer notificationNo
    ) {
        return ResponseEntity.ok(managerNotificationService.findMyNotification(principal, notificationNo));
    }

    @PatchMapping("/api/manager-notifications/{notificationNo}/read")
    public ResponseEntity<ManagerNotificationDto> markAsRead(
            @AuthenticationPrincipal Map<String, Object> principal,
            @PathVariable Integer notificationNo
    ) {
        return ResponseEntity.ok(managerNotificationService.markAsRead(principal, notificationNo));
    }

    @PatchMapping("/api/manager-notifications/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @AuthenticationPrincipal Map<String, Object> principal
    ) {
        return ResponseEntity.ok(managerNotificationService.markAllAsRead(principal));
    }

    @DeleteMapping("/api/manager-notifications/{notificationNo}")
    public ResponseEntity<Map<String, Object>> deleteMyNotification(
            @AuthenticationPrincipal Map<String, Object> principal,
            @PathVariable Integer notificationNo
    ) {
        return ResponseEntity.ok(managerNotificationService.deleteMyNotification(principal, notificationNo));
    }

    @DeleteMapping("/api/manager-notifications")
    public ResponseEntity<Map<String, Object>> deleteAllMyNotifications(
            @AuthenticationPrincipal Map<String, Object> principal
    ) {
        return ResponseEntity.ok(managerNotificationService.deleteAllMyNotifications(principal));
    }
}
