package vn.truongngo.apartcom.one.service.admin.presentation.base;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedApiResponse<T>(List<T> data, Meta meta) {

    public record Meta(int page, int size, long total) {}

    public static <T> PagedApiResponse<T> of(Page<T> page) {
        return new PagedApiResponse<>(
                page.getContent(),
                new Meta(page.getNumber(), page.getSize(), page.getTotalElements())
        );
    }
}
