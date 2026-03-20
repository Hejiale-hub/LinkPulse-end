package com.hejiale.common.domain.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CountLogVO {
    private LocalDate time;
    private String linkId;
    private Long clickCount;
    private String topProvince;
    private LocalDateTime latestClickTime;
    private Long detailTotal;
}
