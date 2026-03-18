package com.hejiale.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageDTO implements Serializable {
    private int page;
    private int pageSize;
    private int pageTotal;
}
