package ru.itq.timeinstatus.actions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.itq.timeinstatus.ao.Statistic;
import ru.itq.timeinstatus.dto.IssueDto;
import ru.itq.timeinstatus.service.StatisticService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
public class StatisticServlet extends HttpServlet {

    private final TemplateRenderer templateRenderer;
    private final StatisticService statisticService;
    private final WebResourceManager webResourceManager;
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

    @SneakyThrows
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        Map<String, Object> context = new HashMap<>();
        String epicKey = req.getParameter("epicKey");
        String projectKey = req.getParameter("projectKey");
        context.put("selectedProjectKey", projectKey);
        Project projectByCurrentKeyIgnoreCase = projectManager.getProjectByCurrentKeyIgnoreCase(projectKey);
        context.put("projectList", projectManager.getProjects().stream().map(Project::getKey));
        context.put("webResourceManager", webResourceManager);
        if (Objects.nonNull(projectKey)) {
            Collection<Long> issueIdsForProject = issueManager.getIssueIdsForProject(projectByCurrentKeyIgnoreCase.getId()).stream()
                    .filter(aLong -> issueLinkManager.getOutwardLinks(aLong).size() > 0)
                    .collect(Collectors.toList());
            List<Issue> issueObjects = issueManager.getIssueObjects(issueIdsForProject);
            context.put("issueIdsForProject", issueObjects.stream()
                    .map(Issue::getKey).collect(Collectors.toList())
            );
        }
        if (Objects.nonNull(epicKey)) {
            MutableIssue epicIssue = issueManager.getIssueObject(epicKey);
            List<IssueDto> inwardLinks = issueLinkManager.getOutwardLinks(epicIssue.getId()).stream()
                    .map(issueLink -> {
                        Issue destinationObject = issueLink.getDestinationObject();
                        return IssueDto.builder()
                                .key(destinationObject.getKey())
                                .description(destinationObject.getSummary())
                                .build();
                    })
                    .collect(Collectors.toList());
            String[] issueKeys = inwardLinks.stream().map(IssueDto::getKey).toArray(String[]::new);
            Statistic[] statisticForIssue = statisticService.getStatisticForIssues(issueKeys);
            context.put("statisticForIssue", statisticForIssue);
            context.put("selectedEpicKey", epicKey);
            context.put("inwardLinks", inwardLinks);
        }
        resp.setContentType("text/html;charset=utf-8");
        templateRenderer.render("/templates/statistic/statistic.vm", context, resp.getWriter());
    }

}
