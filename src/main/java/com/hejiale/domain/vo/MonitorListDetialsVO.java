package com.hejiale.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MonitorListDetialsVO implements Serializable {
    private String linkId;           // linkId
    private String ip;               // 访问者 IP
    private String province;         // IP 对应省份
    private String city;             // IP 对应城市
    private String ua;               // 访问者 User-Agent
    private String os;               // 访问者操作系统
    private String browser;          // 访问者浏览器
    private LocalDateTime clickTime;        // 点击时间（ISO 字符串 或 yyyy-MM-dd HH:mm:ss）
}
