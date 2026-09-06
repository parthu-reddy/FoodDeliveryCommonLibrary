package com.fooddelivery.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private int size;
    private int number;
    private boolean first;
    private int numberOfElements;
    private boolean empty;

    public static <T> PageResponseDto<T> of(org.springframework.data.domain.Page<T> page) {
        return PageResponseDto.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .size(page.getSize())
                .number(page.getNumber())
                .first(page.isFirst())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }
}
