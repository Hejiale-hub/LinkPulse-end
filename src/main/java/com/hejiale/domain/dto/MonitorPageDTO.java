package com.hejiale.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class MonitorPageDTO extends PageDTO{
    private String linkCode;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String linkTitleKeyword;
    private String linkCodeKeyword;
    private String originalUrlKeyword;
    private String regionKeyword;
}
