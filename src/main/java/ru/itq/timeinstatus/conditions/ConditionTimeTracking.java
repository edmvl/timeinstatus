package ru.itq.timeinstatus.conditions;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractIssueCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import ru.itq.timeinstatus.ao.History;
import ru.itq.timeinstatus.service.HistoryService;

import java.util.Objects;

@Slf4j
@ComponentScan
public class ConditionTimeTracking extends AbstractIssueCondition {

    private final HistoryService historyService;

    public ConditionTimeTracking(HistoryService historyService) {
        this.historyService = historyService;
    }

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, Issue issue, JiraHelper jiraHelper) {
        History[] historyForIssue = historyService.getHistoryForIssue(issue);
        return Objects.nonNull(historyForIssue) && historyForIssue.length > 1;
    }


}