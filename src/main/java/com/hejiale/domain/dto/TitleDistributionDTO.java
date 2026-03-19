package com.hejiale.domain.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class TitleDistributionDTO {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String linkTitleKeyword;
    private String linkCodeKeyword;
    private String originalUrlKeyword;
    private String regionKeyword;
    private Integer topN;
}
