package com.hejiale.common.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CountLogVO {
    private String linkId;
    private Long clickCount;
    private String topProvince;
    private LocalDateTime latestClickTime;
    private Long detailTotal;
}
