package vn.truongngo.apartcom.one.service.admin.presentation.base;

public record ApiResponse<T>(T data) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }
}