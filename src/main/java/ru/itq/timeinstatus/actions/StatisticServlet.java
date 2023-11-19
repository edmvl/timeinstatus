package ru.itq.timeinstatus.actions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.itq.timeinstatus.ao.Statistic;
import ru.itq.timeinstatus.service.ExcelGeneratorService;
import ru.itq.timeinstatus.service.StatisticService;
import ru.itq.timeinstatus.utils.TimeFormatter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
public class StatisticServlet extends HttpServlet {

    private TemplateRenderer templateRenderer;
    private StatisticService statisticService;
    private ExcelGeneratorService excelGeneratorService;
    private WebResourceManager webResourceManager;
    private final ProjectManager projectManager = ComponentAccessor.getProjectManager();
    private final IssueManager issueManager = ComponentAccessor.getIssueManager();
    private final IssueLinkManager issueLinkManager = ComponentAccessor.getIssueLinkManager();


    public StatisticServlet(
            TemplateRenderer templateRenderer, StatisticService statisticService, ExcelGeneratorService excelGeneratorService,
            WebResourceManager webResourceManager
    ) {
        this.templateRenderer = templateRenderer;
        this.statisticService = statisticService;
        this.excelGeneratorService = excelGeneratorService;
        this.webResourceManager = webResourceManager;
    }

    private StatisticServlet() {
    }

    @SneakyThrows
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        Map<String, Object> context = new HashMap<>();
        String epicKey = req.getParameter("epicKey");
        String projectKey = req.getParameter("projectKey");
        String selectedIssueType = req.getParameter("issueType");
        String baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
        context.put("baseUrl", baseUrl);
        context.put("selectedProjectKey", projectKey);
        Project projectByCurrentKeyIgnoreCase = projectManager.getProjectByCurrentKeyIgnoreCase(projectKey);
        context.put("projectList", projectManager.getProjects().stream().map(Project::getKey));
        context.put("webResourceManager", webResourceManager);
        if (Objects.nonNull(projectKey)) {
            Collection<Long> issueIdsForProject = issueManager.getIssueIdsForProject(projectByCurrentKeyIgnoreCase.getId()).stream()
                    .filter(aLong -> issueLinkManager.getOutwardLinks(aLong).size() > 0)
                    .collect(Collectors.toList());
            List<Issue> issueObjects = issueManager.getIssueObjects(issueIdsForProject);
            context.put("issueIdsForProject", issueObjects.stream().map(Issue::getKey).collect(Collectors.toList()));
        }
        if (Objects.nonNull(epicKey)) {
            MutableIssue epicIssue = issueManager.getIssueObject(epicKey);
            List<Issue> outwardLinks = issueLinkManager.getOutwardLinks(epicIssue.getId()).stream()
                    .map(IssueLink::getDestinationObject).collect(Collectors.toList());
            List<Issue> linkedIssues = outwardLinks.stream()
                    .filter(issue -> !Objects.nonNull(selectedIssueType) || selectedIssueType.equals(issue.getIssueType().getId()))
                    .collect(Collectors.toList());
            context.put("issueTypesForProject", outwardLinks.stream().map(Issue::getIssueType).filter(Objects::nonNull).collect(Collectors.toSet()));
            String[] issueKeys = linkedIssues.stream().map(Issue::getKey).toArray(String[]::new);
            Statistic[] statisticForIssue = statisticService.getStatisticForIssues(issueKeys);
            Map<String, Long> timeSpentMap = Arrays.stream(statisticForIssue).collect(Collectors.toMap(
                    Statistic::getIssueKey,
                    Statistic::getTimeSpent,
                    Long::sum
            ));
            context.put("timeSpentMap", timeSpentMap);
            context.put("timeFormatter", new TimeFormatter());
            context.put("statisticForIssue", statisticForIssue);
            context.put("selectedIssueType", selectedIssueType);
            context.put("selectedEpicKey", epicKey);
            context.put("inwardLinks", linkedIssues);
        }
        resp.setContentType("text/html;charset=utf-8");
        templateRenderer.render("/templates/statistic/statistic.vm", context, resp.getWriter());
    }

}
