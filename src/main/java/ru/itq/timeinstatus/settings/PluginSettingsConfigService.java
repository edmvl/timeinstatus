package ru.itq.timeinstatus.settings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.itq.timeinstatus.dto.PluginSettingsDto;

@Slf4j
@Service
public class PluginSettingsConfigService {

    private final PluginSettingsManager pluginSettingsManager;

    @Autowired
    public PluginSettingsConfigService(PluginSettingsManager pluginSettingsManager) {
        this.pluginSettingsManager = pluginSettingsManager;
    }

    public void saveSettings(PluginSettingsDto pluginSettingsDto) {
/*
        String customFieldProfileEmployee = pluginSettingsDto.getCustomFieldProfileEmployee();

        pluginSettingsManager.setSetting(CUSTOM_FIELD_PROFILE_EMPLOYEE, customFieldProfileEmployee);
*/
    }

}
