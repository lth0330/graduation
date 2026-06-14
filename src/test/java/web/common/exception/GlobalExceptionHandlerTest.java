package web.common.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    @Test
    void handleIllegalArgumentExceptionReturnsBadRequestApiError() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");

        var response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("잘못된 요청 값입니다."),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("잘못된 요청 값입니다.");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    void handleDataIntegrityViolationExceptionReturnsConflictApiError() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");

        var response = handler.handleDataIntegrityViolationException(
                new DataIntegrityViolationException("duplicate key"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("데이터 제약 조건을 위반했습니다.");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }
}
