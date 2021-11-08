package com.wellbeeing.wellbeeing.domain.message;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PaginatedResponse {
    List<?> objects;
    int currentPage;
    int totalPages;
    long totalItems;
}
