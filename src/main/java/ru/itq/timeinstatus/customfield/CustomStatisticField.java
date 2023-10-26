package ru.itq.timeinstatus.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import org.springframework.beans.factory.annotation.Autowired;
import ru.itq.timeinstatus.ao.Statistic;
import ru.itq.timeinstatus.service.StatisticService;

import javax.annotation.Nullable;
import java.util.Arrays;

public class CustomStatisticField extends GenericTextCFType {

    @Autowired
    private final StatisticService statisticService;

    protected CustomStatisticField(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager, StatisticService statisticService) {
        super(customFieldValuePersister, genericConfigManager);
        this.statisticService = statisticService;
    }

    private static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long d = seconds / 32400;
        long h = seconds / 3600 - d * 9;
        long m = (seconds % 3600) / 60;
        return addLeadingZero(d) + ":" + addLeadingZero(h) + ":" + addLeadingZero(m);
    }

    private static String addLeadingZero(long d) {
        return d > 9 ? "" + d : "0" + d;
    }


    @Nullable
    @Override
    public String getValueFromIssue(CustomField field, Issue issue) {
        String fieldName = field.getFieldName();
        Statistic[] statisticForIssue = statisticService.getStatisticForIssue(issue);
        return formatTime(Arrays.stream(statisticForIssue)
                .filter(
                        s -> fieldName.equals(s.getLastStateName() + "-&gt;" + s.getNextStateName())
                )
                .map(Statistic::getTimeSpent)
                .reduce(0L, Long::sum));
    }

}