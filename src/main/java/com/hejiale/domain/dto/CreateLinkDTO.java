package com.hejiale.domain.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class CreateLinkDTO {
    @JsonPropertyDescription("需要转换的原始链接")
    private String originalUrl;
    @JsonPropertyDescription("需要设置的链接标题")
    private String linkTitle;


    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public void setLinkTitle(String linkTitle) {
        this.linkTitle = linkTitle;
    }
}
