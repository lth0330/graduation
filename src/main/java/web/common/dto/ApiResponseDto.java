package web.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, data);
    }

    public static ApiResponseDto<Void> success(String message) {
        return new ApiResponseDto<>(true, message, null);
    }

    public static ApiResponseDto<Void> fail(String message) {
        return new ApiResponseDto<>(false, message, null);
    }
}
