package ru.itq.timeinstatus.provider;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.springframework.context.annotation.ComponentScan;
import ru.itq.timeinstatus.ao.History;
import ru.itq.timeinstatus.dto.HistoryDto;
import ru.itq.timeinstatus.service.HistoryService;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

@ComponentScan
@Slf4j
public class ModelProvider extends AbstractJiraContextProvider {

    private final WebResourceManager webResourceManager;
    private final HistoryService historyService;
    private CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();

    public ModelProvider(HistoryService historyService) {
        this.historyService = historyService;
        this.webResourceManager = ComponentAccessor.getWebResourceManager();
    }

    @SneakyThrows
    @Override
    public Map<String, Object> getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
        History[] historyForIssue = historyService.getHistoryForIssue(currentIssue);
        Map<String, List<HistoryDto>> collect = Arrays.stream(historyForIssue)
                .map(history -> HistoryDto.builder()
                        .key(history.getFieldId())
                        .value(history.getNewValue().split("\\.")[0])
                        .build())
                .collect(Collectors.groupingBy(h -> {
                            CustomField customFieldObject = customFieldManager.getCustomFieldObject(h.getKey());
                            return customFieldObject != null ? customFieldObject.getFieldName() : "";
                        })
                );
        System.out.println(collect);
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("historyForIssue", collect);
        contextMap.put("webResourceManager", webResourceManager);
        return contextMap;
    }


}