package com.hejiale.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MonitorListVO {
    private Long linkId;
    private String linkCode;            // 短链接
    private String linkTitle;          // 链接标题
    private String originalUrl;       // 原始链接
    private Long clickCount;         // 总点击次数
    private String topProvince;       // 省份
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestClickTime;       // 最后点击时间
    private Long detailTotal;         // 访问详情总数
}
