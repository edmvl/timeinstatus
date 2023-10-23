package ru.itq.timeinstatus.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import lombok.extern.slf4j.Slf4j;
import net.java.ao.Query;
import org.springframework.stereotype.Service;
import ru.itq.timeinstatus.ao.Statistic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatisticService {
    private final ActiveObjects ao;

    public StatisticService(@ComponentImport ActiveObjects ao) {
        this.ao = ao;
    }

    public void saveStatistic(
            Long projectId, String issueKey, String lastStateName, Object lastStateTime,
            String nextStateName, Object nextStateTime, long timeSpent
    ) {
        Statistic history = ao.create(Statistic.class);
        history.setProjectId(projectId);
        history.setIssueKey(issueKey);
        history.setLastStateName(lastStateName);
        history.setLastStateTime(Objects.nonNull(lastStateTime) ? lastStateTime.toString() : null);
        history.setNextStateName(nextStateName);
        history.setNextStateTime(Objects.nonNull(nextStateTime) ? nextStateTime.toString() : null);
        history.setTimeSpent(timeSpent);
        history.save();
    }

    public Statistic[] getStatisticForIssue(Issue issue) {
        Long projectId = issue.getProjectId();
        String key = issue.getKey();
        return ao.find(Statistic.class, Query.select().where("ISSUE_KEY=? AND PROJECT_ID=?", key, projectId));
    }

}
