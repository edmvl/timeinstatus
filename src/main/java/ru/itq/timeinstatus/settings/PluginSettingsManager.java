package ru.itq.timeinstatus.settings;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PluginSettingsManager {

    private static final String CHECKLIST_PLUGIN_SETTINGS_KEY = "time-in-status-plugin-settings";
    private static final String SETTING_KEY_TEMPLATE = CHECKLIST_PLUGIN_SETTINGS_KEY + "-%s";

    private final PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    public PluginSettingsManager() {
        this.pluginSettingsFactory = ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
    }

    public String getSetting(String settingKey) {
        try {
            PluginSettings pluginSettings = pluginSettingsFactory
                .createSettingsForKey(CHECKLIST_PLUGIN_SETTINGS_KEY);
            String key = String.format(SETTING_KEY_TEMPLATE, settingKey);
            return (String) pluginSettings.get(key);
        } catch (Exception ex) {
            log.error("[{}] error during getting setting with key {}: {}",
                getClass().getSimpleName(), settingKey, ExceptionUtils.getStackTrace(ex));
            return null;
        }
    }

    public void setSetting(String settingKey, String settingValue) {
        try {
            PluginSettings pluginSettings = pluginSettingsFactory
                .createSettingsForKey(CHECKLIST_PLUGIN_SETTINGS_KEY);
            String key = String.format(SETTING_KEY_TEMPLATE, settingKey);
            pluginSettings.put(key, settingValue);
        } catch (Exception ex) {
            log.error("[{}] error during set setting with key {} and value {}: {}",
                getClass().getSimpleName(), settingKey, settingValue, ExceptionUtils.getStackTrace(ex));
        }
    }
}
