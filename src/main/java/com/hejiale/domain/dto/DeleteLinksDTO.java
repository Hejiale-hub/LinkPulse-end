package com.hejiale.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeleteLinksDTO {
    private List<Long> linkIds;
}
