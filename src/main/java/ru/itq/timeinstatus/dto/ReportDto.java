package ru.itq.timeinstatus.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class ReportDto {
    private List<IssueReportsDto> byIssues;
    private Map<String, Long> byStatuses;
}
