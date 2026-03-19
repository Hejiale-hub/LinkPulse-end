package com.hejiale.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PageDTO implements Serializable {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private Integer totalPage;
}
