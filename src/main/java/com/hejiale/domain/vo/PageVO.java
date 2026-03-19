package com.hejiale.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageVO<T> {
    private Long total;
    private Integer pageNo;
    private Integer pageSize;
    private List<T> list;

    public static <T> PageVO<T> empty() {
        return new PageVO<>(0L, 0, 0, null);
    }

    public static <T> PageVO<T> emptyList() {
        return new PageVO<>(0L, 0, 0, null);
    }
}
