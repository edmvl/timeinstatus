package ru.itq.timeinstatus.ao;

import net.java.ao.Entity;

public interface Statistic extends Entity {

    Long getProjectId();
    void setProjectId(Long projectId);

    String getIssueKey();
    void setIssueKey(String issueKey);

    String getLastState();
    void setLastState(String fieldValue);

    String getNextState();
    void setNextState(String fieldValue);

    String getLastTime();
    void setLastTime(String fieldValue);

    String getNextTime();
    void setNextTime(String fieldValue);

    long getTimeSpent();
    void setTimeSpent(long fieldValue);
}
