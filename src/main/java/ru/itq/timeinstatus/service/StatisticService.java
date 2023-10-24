package ru.itq.timeinstatus.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import lombok.extern.slf4j.Slf4j;
import net.java.ao.Query;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import ru.itq.timeinstatus.ao.Statistic;

import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
public class StatisticService {
    private final ActiveObjects ao;

    public StatisticService(@ComponentImport ActiveObjects ao) {
        this.ao = ao;
    }

    public void saveStatistic(
            Long projectId, String issueKey, String lastStateName, Date lastStateTime,
            String nextStateName, Date nextStateTime
    ) {
        Statistic history = ao.create(Statistic.class);
        history.setProjectId(projectId);
        history.setIssueKey(issueKey);
        history.setLastStateName(lastStateName);
        history.setLastStateTime(Objects.nonNull(lastStateTime) ? lastStateTime.toString() : null);
        history.setNextStateName(nextStateName);
        history.setNextStateTime(Objects.nonNull(nextStateTime) ? nextStateTime.toString() : null);
        history.setTimeSpent(getTimeSpent(lastStateTime, nextStateTime));
        history.save();
    }

    private long getTimeSpent(Date lastStateTime, Date nextStateTime) {
        return 0;
    }

    public Statistic[] getStatisticForIssue(Issue issue) {
        Long projectId = issue.getProjectId();
        String key = issue.getKey();
        return ao.find(Statistic.class, Query.select().where("ISSUE_KEY=? AND PROJECT_ID=?", key, projectId));
    }

}
