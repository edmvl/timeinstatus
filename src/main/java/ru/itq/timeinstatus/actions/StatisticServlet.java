package ru.itq.timeinstatus.actions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
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
import org.ofbiz.core.entity.GenericEntityException;
import org.springframework.stereotype.Component;
import ru.itq.timeinstatus.ao.Statistic;
import ru.itq.timeinstatus.service.ExcelGeneratorService;
import ru.itq.timeinstatus.service.StatisticService;
import ru.itq.timeinstatus.utils.TimeFormatter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
public class StatisticServlet extends HttpServlet {

    private TemplateRenderer templateRenderer;
    private StatisticService statisticService;
    private WebResourceManager webResourceManager;
    private final ProjectManager projectManager = ComponentAccessor.getProjectManager();
    private final IssueManager issueManager = ComponentAccessor.getIssueManager();
    private final IssueLinkManager issueLinkManager = ComponentAccessor.getIssueLinkManager();


    public StatisticServlet(
            TemplateRenderer templateRenderer, StatisticService statisticService, WebResourceManager webResourceManager
    ) {
        this.templateRenderer = templateRenderer;
        this.statisticService = statisticService;
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
            fillProjectIssues(context, projectByCurrentKeyIgnoreCase);
        }
        if (Objects.nonNull(epicKey)) {
            fillLinkedIssues(context, epicKey, selectedIssueType);
        }
        resp.setContentType("text/html;charset=utf-8");
        templateRenderer.render("/templates/statistic/statistic.vm", context, resp.getWriter());
    }

    private void fillProjectIssues(Map<String, Object> context, Project projectByCurrentKeyIgnoreCase) throws GenericEntityException {
        Collection<Long> issueIdsForProject = issueManager.getIssueIdsForProject(projectByCurrentKeyIgnoreCase.getId()).stream()
                .filter(aLong -> issueLinkManager.getOutwardLinks(aLong).size() + issueLinkManager.getInwardLinks(aLong).size() > 1)
                .collect(Collectors.toList());
        List<Issue> issueObjects = issueManager.getIssueObjects(issueIdsForProject);
        context.put("issueIdsForProject", issueObjects.stream().map(Issue::getKey).collect(Collectors.toList()));
    }

    private void fillLinkedIssues(Map<String, Object> context, String epicKey, String selectedIssueType) {
        log.error("start fillLinkedIssues");
        MutableIssue epicIssue = issueManager.getIssueObject(epicKey);
        if (Objects.isNull(epicIssue)) {
            log.debug("error while get epic issue object {}", epicKey);
            return;
        }
        List<Issue> outwardLinks = getLinks(epicIssue);
        List<Issue> linkedIssues = outwardLinks.stream()
                .filter(issue -> Objects.isNull(selectedIssueType) || (Objects.nonNull(issue.getIssueType()) && selectedIssueType.equals(issue.getIssueType().getId())))
                .collect(Collectors.toList());
        log.debug("filtered issues for issue {} :\n{}", epicIssue, linkedIssues);
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
        log.debug("finish fillLinkedIssues");
    }

    private List<Issue> getLinks(MutableIssue epicIssue) {
        List<Issue> outwardLinks = issueLinkManager.getOutwardLinks(epicIssue.getId()).stream()
                .map(IssueLink::getDestinationObject).collect(Collectors.toList());
        List<Issue> inwardLinks = issueLinkManager.getInwardLinks(epicIssue.getId()).stream()
                .map(IssueLink::getSourceObject).collect(Collectors.toList());
        log.debug("outwardLinks issues for issue {} :\n{}", epicIssue, outwardLinks);
        log.debug("inwardLinks issues for issue {} :\n{}", epicIssue, inwardLinks);
        outwardLinks.addAll(inwardLinks);
        return outwardLinks;
    }

}
