package ru.itq.timeinstatus.postfunction;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.spi.SimpleStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.itq.timeinstatus.service.StatisticService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class TimeTrackingFunction extends AbstractJiraFunctionProvider {

    private final I18nResolver i18n;
    private final CustomFieldManager customFieldManager;
    @Autowired
    private final StatisticService statisticService;
    private final WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
    private final StatusManager statusManager = ComponentAccessor.getComponent(StatusManager.class);


    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        Object currentSteps = transientVars.get("currentSteps");
        MutableIssue issue = getIssue(transientVars);
        if (currentSteps instanceof List) {
            SimpleStep step = (SimpleStep) ((List<?>) currentSteps).get(0);
            Date startDate = step.getStartDate();
            Date finishDate = step.getFinishDate();
            int actionId = step.getActionId();
            try {
                String nextStatusIdForAction = workflowManager.getNextStatusIdForAction(issue, actionId);
                String currentStatus = issue.getStatus().getSimpleStatus().getName();
                String nextStatus = statusManager.getStatus(nextStatusIdForAction).getSimpleStatus().getName();
                statisticService.saveStatistic(
                        issue.getProjectId(), issue.getKey(), currentStatus, startDate, nextStatus, finishDate
                );
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
    }

}

