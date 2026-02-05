package com.games.util;

import lombok.Data;

import java.util.List;

@Data
public class PageDataResUtil<T> {
    private long total;
    private int page;
    private int size;
    private int totalPages;
    private List<T> list;
}
