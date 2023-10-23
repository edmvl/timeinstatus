package ru.itq.timeinstatus.ao;

import net.java.ao.Entity;

public interface Statistic extends Entity {

    Long getProjectId();
    void setProjectId(Long projectId);

    String getIssueKey();
    void setIssueKey(String issueKey);

    String getLastStateName();
    void setLastStateName(String fieldValue);

    String getNextStateName();
    void setNextStateName(String fieldValue);

    String getLastStateTime();
    void setLastStateTime(String fieldValue);

    String getNextStateTime();
    void setNextStateTime(String fieldValue);

    long getTimeSpent();
    void setTimeSpent(long fieldValue);
}
