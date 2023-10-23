package ru.itq.timeinstatus.conditions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.property.JsonEntityPropertyManagerImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractIssueCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;

import java.util.Objects;


@Slf4j
@ComponentScan
public class ConditionTimeTracking extends AbstractIssueCondition {

    private final JsonEntityPropertyManagerImpl jsonEntityPropertyManager;

    public ConditionTimeTracking() {
        this.jsonEntityPropertyManager = ComponentAccessor.getComponent(JsonEntityPropertyManagerImpl.class);
    }

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, Issue issue, JiraHelper jiraHelper) {
        return !Objects.isNull(issue);
    }


}