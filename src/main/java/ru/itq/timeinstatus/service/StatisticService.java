package ru.itq.timeinstatus.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import lombok.extern.slf4j.Slf4j;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.itq.timeinstatus.ao.Statistic;
import ru.itq.timeinstatus.utils.WorkingDayUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatisticService {
    private final ActiveObjects ao;

    @Autowired
    public StatisticService(
            @ComponentImport ActiveObjects ao
    ) {
        this.ao = ao;
    }

    public void saveStatistic(
            Long projectId, String issueKey, String lastStateName, Date lastStateTime,
            String nextStateName, Date nextStateTime
    ) {
        Statistic history = ao.create(Statistic.class);
        history.setProjectId(projectId);
        history.setIssueKey(issueKey);
        history.setLastState(lastStateName);
        history.setLastTime(Objects.nonNull(lastStateTime) ? lastStateTime.toString() : null);
        history.setNextState(nextStateName);
        history.setNextTime(Objects.nonNull(nextStateTime) ? nextStateTime.toString() : null);
        history.setTimeSpent(getTimeSpent(lastStateTime, nextStateTime));
        history.save();
    }

    private long getTimeSpent(Date lastStateTime, Date nextStateTime) {
        LocalDateTime last = LocalDateTime.ofInstant(lastStateTime.toInstant(), ZoneId.systemDefault());
        LocalDateTime next = LocalDateTime.ofInstant(nextStateTime.toInstant(), ZoneId.systemDefault());
        return WorkingDayUtils.getTimeSpent(last, next);
    }

    public Statistic[] getStatisticForIssue(Issue issue) {
        Long projectId = issue.getProjectId();
        String key = issue.getKey();
        return ao.find(Statistic.class, Query.select().where("ISSUE_KEY=? AND PROJECT_ID=?", key, projectId));
    }

    public Statistic[] getStatisticForIssues(String[] issueIds) {
        String ph = Arrays.stream(issueIds).map(s -> "?").collect(Collectors.joining(","));
        Query query = Query.select().where("ISSUE_KEY IN (" + ph + ")", issueIds);
        return ao.find(Statistic.class, query);
    }

}
