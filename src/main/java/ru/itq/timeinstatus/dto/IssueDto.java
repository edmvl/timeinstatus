package ru.itq.timeinstatus.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class IssueDto {
    private String key;
    private String description;
}
