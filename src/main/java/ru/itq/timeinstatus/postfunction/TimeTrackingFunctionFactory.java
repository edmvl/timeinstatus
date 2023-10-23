package ru.itq.timeinstatus.postfunction;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class TimeTrackingFunctionFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> map) {

    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {

    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {
    }

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> map) {
        Map<String, Object> descriptorParams = new HashMap<>();
        return descriptorParams;
    }

}
