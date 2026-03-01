package com.games.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDataResUtil<T> {
    private long total;
    private int page;
    private int size;
    private int totalPages;
    private List<T> list;
}
