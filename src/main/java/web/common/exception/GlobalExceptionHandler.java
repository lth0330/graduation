package web.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatusCode statusCode = exception.getStatusCode();
        int status = statusCode.value();
        String error = getError(status);
        String message = getMessage(exception, error);

        return ResponseEntity.status(statusCode)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        status,
                        error,
                        message,
                        request.getRequestURI()
                ));
    }

    private String getMessage(ResponseStatusException exception, String defaultMessage) {
        String reason = exception.getReason();
        return reason != null && !reason.isBlank() ? reason : defaultMessage;
    }

    private String getError(int status) {
        HttpStatus httpStatus = HttpStatus.resolve(status);
        return httpStatus != null ? httpStatus.getReasonPhrase() : "HTTP " + status;
    }

    public record ApiErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
    }
}
