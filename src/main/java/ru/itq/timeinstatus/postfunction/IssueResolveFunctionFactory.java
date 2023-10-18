package ru.itq.timeinstatus.postfunction;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.sal.api.message.I18nResolver;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static ru.itq.timeinstatus.utils.Constants.*;

@RequiredArgsConstructor
public class IssueResolveFunctionFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {


    private final I18nResolver i18n;
    private final CustomFieldManager customFieldManager;

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> map) {
        Collection<Pair<String, String>> allCustomFields = getAllCustomFields();

        map.put(ISSUE_RESOLVE_HISTORY_FIELDS, withDefaultPair(allCustomFields));


    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {
        getVelocityParamsForInput(map);

        if (!(abstractDescriptor instanceof FunctionDescriptor)) {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) abstractDescriptor;
        map.put(ISSUE_RESOLVE_HISTORY_FIELD_ID_SELECTED, functionDescriptor.getArgs().get(ISSUE_RESOLVE_HISTORY_FIELD_ID));

    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {
        System.out.println("test");
    }

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> map) {
        Map<String, Object> descriptorParams = new HashMap<>();
        String checkboxFieldId = getSingleParam(map, ISSUE_RESOLVE_HISTORY_FIELD_ID);
        if (StringUtils.isEmpty(checkboxFieldId)) {
            throw new RuntimeException(i18n.getText("checklist-creation-checkbox-is-empty-error"));
        } else {
            descriptorParams.put(ISSUE_RESOLVE_HISTORY_FIELD_ID, checkboxFieldId);
        }

        return descriptorParams;
    }

    private Collection<Pair<String, String>> getAllCustomFields() {
        return customFieldManager.getCustomFieldObjects().stream()
                .filter(Objects::nonNull)
                .map(this::apply)
                .collect(Collectors.toList());
    }

    private Pair<String, String> apply(CustomField customField) {
        return Pair.of(customField.getId(),
                customField.getId().replace(CUSTOM_FIELD_ID, "") + " : " + customField.getFieldName());
    }

    private Collection<Pair<String, String>> withDefaultPair(Collection<Pair<String, String>> collection) {
        Collection<Pair<String, String>> collectionWithDefaultPair = new ArrayList<Pair<String, String>>() {{
            add(Pair.of(DEFAULT_KEY, DEFAULT_VALUE));
        }};

        collectionWithDefaultPair.addAll(collection);
        return collectionWithDefaultPair;
    }

    private String getSingleParam(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof String[]) {
            if (((String[]) value).length > 0) {
                return ((String[]) value)[0];
            }
        }
        return (String) value;
    }

}
