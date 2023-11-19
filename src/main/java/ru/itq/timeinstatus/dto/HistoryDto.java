package ru.itq.timeinstatus.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HistoryDto {
    private String key;
    private String value;
}
