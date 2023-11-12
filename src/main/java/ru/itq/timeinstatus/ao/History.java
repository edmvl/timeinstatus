package ru.itq.timeinstatus.ao;

import net.java.ao.Entity;

public interface History extends Entity {

    Long getProjectId();
    void setProjectId(Long projectId);

    String getIssueKey();
    void setIssueKey(String issueKey);

    String getFieldId();
    void setFieldId(String fieldId);

    String getOldValue();
    void setOldValue(String fieldValue);

    String getNewValue();
    void setNewValue(String fieldValue);
}
