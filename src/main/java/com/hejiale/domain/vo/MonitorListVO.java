package com.hejiale.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class MonitorListVO<T> {
    private String linkId;
    private String linkCode;            // 短链接
    private String linkTitle;          // 链接标题
    private String originalUrl;       // 原始链接
    private Long clickCount;         // 总点击次数
    private List<T> clickRecords;
}
