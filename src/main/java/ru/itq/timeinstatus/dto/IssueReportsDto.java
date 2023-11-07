package ru.itq.timeinstatus.dto;

import com.atlassian.jira.issue.Issue;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Builder
@Data
public class IssueReportsDto {
    private String key;
    private Issue issue;
    private List<HashMap<String, Long>> transitionsSpent;
}
