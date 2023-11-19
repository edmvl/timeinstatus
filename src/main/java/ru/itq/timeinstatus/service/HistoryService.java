package ru.itq.timeinstatus.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import lombok.extern.slf4j.Slf4j;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.itq.timeinstatus.ao.History;

import java.util.Objects;

@Slf4j
@Service
public class HistoryService {
    private final ActiveObjects ao;

    @Autowired
    public HistoryService(@ComponentImport ActiveObjects ao) {
        this.ao = ao;
    }

    public void saveHistory(Long projectId, String issueKey, String issueResolveFieldId, Object fieldOldValue, Object fieldNewValue) {
        History history = ao.create(History.class);
        history.setProjectId(projectId);
        history.setIssueKey(issueKey);
        history.setFieldId(issueResolveFieldId);
        history.setOldValue(Objects.nonNull(fieldOldValue) ? fieldOldValue.toString() : null);
        history.setNewValue(Objects.nonNull(fieldNewValue) ? fieldNewValue.toString() : null);
        history.save();
    }

    public History[] getHistoryForIssue(Issue issue) {
        Long projectId = issue.getProjectId();
        String key = issue.getKey();
        return ao.find(History.class, Query.select().where("ISSUE_KEY=? AND PROJECT_ID=?", key, projectId));
    }

}
