package com.games.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtils {

    public static Pageable genericPage(Integer pageNum, Integer pageSize, String param) {
        pageNum = pageNum <= 0 ? 1 : pageNum;
        pageSize = pageSize <= 0 ? 1000 : pageSize;
        return PageRequest.of(
                pageNum - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, param));
    }
}
