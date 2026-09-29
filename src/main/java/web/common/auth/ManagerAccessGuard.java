package web.common.auth;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import web.common.type.UserRole;

// 아파트 관리자 본인 확인: 아파트 관리자는 자기 managerNo의 정보만 조회/수정할 수 있다.
// 웹 관리자는 모든 아파트 관리자 정보를 관리하므로 검사하지 않는다.
public final class ManagerAccessGuard {

    private ManagerAccessGuard() {
    }

    public static void checkSelfOrWebAdmin(Map<String, Object> principal, Integer managerNo) {
        if (isWebAdmin(principal)) {
            return;
        }

        Object userNo = principal != null ? principal.get("userNo") : null;
        if (!(userNo instanceof Number number) || managerNo == null || number.intValue() != managerNo) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 정보만 조회하거나 수정할 수 있습니다.");
        }
    }

    public static boolean isWebAdmin(Map<String, Object> principal) {
        return principal != null && UserRole.WEB_ADMIN.name().equals(principal.get("role"));
    }
}
