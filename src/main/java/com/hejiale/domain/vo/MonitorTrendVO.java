package com.hejiale.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;


@Data
public class MonitorTrendVO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate time;
    private Long clicks;
}
