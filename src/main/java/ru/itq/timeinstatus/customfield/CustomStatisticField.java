package ru.itq.timeinstatus.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.itq.timeinstatus.ao.Statistic;
import ru.itq.timeinstatus.service.StatisticService;
import ru.itq.timeinstatus.utils.TimeFormatter;

import javax.annotation.Nullable;
import java.util.Arrays;

@Slf4j
public class CustomStatisticField extends GenericTextCFType {

    @Autowired
    private final StatisticService statisticService;

    protected CustomStatisticField(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager, StatisticService statisticService) {
        super(customFieldValuePersister, genericConfigManager);
        this.statisticService = statisticService;
    }


    @Nullable
    @Override
    public String getValueFromIssue(CustomField field, Issue issue) {
        try {
            String fieldName = field.getFieldName();
            Statistic[] statisticForIssue = statisticService.getStatisticForIssue(issue);
            return TimeFormatter.formatTime(Arrays.stream(statisticForIssue)
                    .filter(
                            s -> fieldName.equals(s.getLastState() + "-&gt;" + s.getNextState())
                    )
                    .map(Statistic::getTimeSpent)
                    .reduce(0L, Long::sum));
        } catch (Exception e) {
            log.error("error on object\n" + field + "\n" + e.getLocalizedMessage());
        }
        return null;
    }

}