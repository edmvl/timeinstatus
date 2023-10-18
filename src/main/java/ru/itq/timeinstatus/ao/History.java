package ru.itq.timeinstatus.ao;

import net.java.ao.Entity;

public interface History extends Entity {

    Long getProjectId();
    void setProjectId(Long projectId);

    String getIssueKey();
    void setIssueKey(String issueKey);

    String getFieldId();
    void setFieldId(String fieldId);

    String getFieldOldValue();
    void setFieldOldValue(String fieldValue);

    String getFieldNewValue();
    void setFieldNewValue(String fieldValue);
}
