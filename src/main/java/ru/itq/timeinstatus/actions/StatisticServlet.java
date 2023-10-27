package ru.itq.timeinstatus.actions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.link.IssueLinkManager;
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
        String issueKeys = req.getParameter("issueKeys");
        String epicKey = req.getParameter("epicKey");
        Collection<Long> issueIdsForProject = issueManager.getIssueIdsForProject(10001L).stream()
                .filter(aLong -> issueLinkManager.getOutwardLinks(aLong).size() > 0)
                .collect(Collectors.toList());
        List<Issue> issueObjects = issueManager.getIssueObjects(issueIdsForProject);
        Map<String, Object> context = new HashMap<>();
        context.put("webResourceManager", webResourceManager);
        context.put("issueIdsForProject", issueObjects.stream()
                .map(Issue::getKey).collect(Collectors.toList())
        );
        if (Objects.nonNull(issueKeys)) {
            String[] selectedIssueKeys = issueKeys.split(",");
            Statistic[] statisticForIssue = statisticService.getStatisticForIssues(selectedIssueKeys);
            context.put("selectedIssueKeys", Arrays.asList(selectedIssueKeys));
            context.put("statisticForIssue", statisticForIssue);
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
            context.put("selectedEpicKey", epicKey);
            context.put("inwardLinks", inwardLinks);
        }
        resp.setContentType("text/html;charset=utf-8");
        templateRenderer.render("/templates/statistic/statistic.vm", context, resp.getWriter());
    }

}