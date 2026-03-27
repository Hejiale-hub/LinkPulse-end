package com.hejiale.common.domain.po;

import lombok.Data;

import java.util.Map;

@Data
public class RequestInfo {
    private Long linkId;
    private Map<String, String> header;
    private String remoteAddr;
}
