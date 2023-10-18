package ru.itq.timeinstatus.postfunction;

import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.itq.timeinstatus.service.HistoryService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import static ru.itq.timeinstatus.utils.Constants.*;

@Slf4j
@RequiredArgsConstructor
public class IssueResolveFunction extends AbstractJiraFunctionProvider {

    private final I18nResolver i18n;
    private final CustomFieldManager customFieldManager;
    @Autowired
    private final HistoryService historyService;

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);
        String issueResolveFieldId = (String) getParameter(args, ISSUE_RESOLVE_HISTORY_FIELD_ID);
        CustomField customField = customFieldManager.getCustomFieldObject(issueResolveFieldId);
        Object customFieldOldValue = issue.getCustomFieldValue(customField);
        System.out.println(customFieldOldValue);
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        issue.setCustomFieldValue(customField, timestamp);
        historyService.saveHistory(issue.getProjectId(), issue.getKey(), issueResolveFieldId, customFieldOldValue, timestamp);
    }


    private Object getParameter(Map args, String key) {
        Object value = args.get(key);
        if (value == null) {
            log.error("[{}] Couldn't get value of function parameter {}", getClass().getSimpleName(), key);
            throw new RuntimeException(i18n.getText("parameter-getting-error", key));
        }
        return value;
    }

}

