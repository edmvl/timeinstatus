package ru.itq.timeinstatus.provider;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import ru.itq.timeinstatus.ao.Statistic;
import ru.itq.timeinstatus.service.StatisticService;

import java.util.HashMap;
import java.util.Map;

@ComponentScan
@Slf4j
public class ModelProvider extends AbstractJiraContextProvider {

    private final WebResourceManager webResourceManager;
    private final StatisticService statisticService;

    public ModelProvider(StatisticService statisticService) {
        this.statisticService = statisticService;
        this.webResourceManager = ComponentAccessor.getWebResourceManager();
    }

    @SneakyThrows
    @Override
    public Map<String, Object> getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
        Statistic[] statisticForIssue = statisticService.getStatisticForIssue(currentIssue);
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("statisticForIssue", statisticForIssue);
        contextMap.put("webResourceManager", webResourceManager);
        return contextMap;
    }



}