package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.ComponentDAO;
import com.appmodz.executionmodule.dao.ConfigurationProfileDAO;
import com.appmodz.executionmodule.dto.*;
import com.appmodz.executionmodule.model.ConfigurationProfile;
import com.appmodz.executionmodule.model.Stack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfigurationProfileService {

    @Autowired
    ConfigurationProfileDAO configurationProfileDAO;

    @Autowired
    UtilService utilService;

    @Autowired
    ComponentDAO componentDAO;

    public ConfigurationProfile getConfigurationProfileById(ConfigurationProfileDTO configurationProfileDTO) throws Exception{
        ConfigurationProfile configurationProfile = configurationProfileDAO.get(configurationProfileDTO.getId());
        if(configurationProfile==null)
            throw new Exception("Configuration Profile Not Found");
        return configurationProfile;
    }

    public ConfigurationProfile createConfigurationProfile(ConfigurationProfileDTO configurationProfileDTO) throws Exception{
        ConfigurationProfile configurationProfile = configurationProfileDAO.getByName(configurationProfileDTO.getName());
        if(configurationProfile!=null)
            throw new Exception("Configuration Profile With Same Name Already Exists");
        configurationProfile = new ConfigurationProfile();
        configurationProfile.setName(configurationProfileDTO.getName());
        configurationProfile.setConfig(configurationProfileDTO.getConfig());
        configurationProfile.setCloudPlatform(componentDAO.getCloudPlatform(configurationProfileDTO.getCloudPlatformId()));
        configurationProfile.setOwner(utilService.getAuthenticatedUser());
        configurationProfileDAO.save(configurationProfile);
        return configurationProfile;
    }

    public ConfigurationProfile editConfigurationProfile(ConfigurationProfileDTO configurationProfileDTO) throws Exception{
        ConfigurationProfile configurationProfile = configurationProfileDAO.get(configurationProfileDTO.getId());
        if(configurationProfileDTO.getName()!=null){
            ConfigurationProfile configurationProfile1 = configurationProfileDAO.getByName(configurationProfileDTO.getName());
            if(configurationProfile1!=null&&configurationProfile1.getId()!=configurationProfile.getId())
                throw new Exception("Configuration Profile With Same Name Already Exists");
            configurationProfile.setName(configurationProfileDTO.getName());
        }
        if(configurationProfileDTO.getConfig()!=null)
            configurationProfile.setConfig(configurationProfileDTO.getConfig());
        if(configurationProfileDTO.getCloudPlatformId()!=null)
            configurationProfile.setCloudPlatform(componentDAO.getCloudPlatform(configurationProfileDTO.getCloudPlatformId()));
        configurationProfile.setOwner(utilService.getAuthenticatedUser());
        configurationProfileDAO.save(configurationProfile);
        return configurationProfile;
    }

    public List listAllConfigurationProfiles(long cloudPlatformId) throws Exception{
        if(cloudPlatformId!=0)
            return configurationProfileDAO.getAllByCloudPlatformId(cloudPlatformId);
        return configurationProfileDAO.getAll();
    }

    public SearchResultDTO searchConfigurationProfiles(SearchRequestDTO searchRequestDTO) {
        SearchResultDTO searchResultDTO = configurationProfileDAO.search(searchRequestDTO);
        return searchResultDTO;
    }

    public Object getCloudPlatformConfigParameters(CanvasRequestDTO canvasRequestDTO) throws Exception{
        return componentDAO.getCloudPlatformConfigParameters(canvasRequestDTO.getCloudPlatformId());
    }

    public String deleteMultipleConfigurationProfiles(ConfigurationProfileDTO configurationProfileDTO) throws Exception{
        List<Long> ids = configurationProfileDTO.getIds();
        StringBuilder exceptions = new StringBuilder();
        StringBuilder successes = new StringBuilder();
        for (long id: ids) {
            ConfigurationProfile configurationProfile = configurationProfileDAO.get(id);
            try {
                configurationProfileDAO.delete(configurationProfile);
                successes.append("Successfully Deleted ").append(id).append("\n");
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                exceptions.append("Unable To Delete id ").append(id).append(" Due To Possible Foreign Key associations").append("\n");
            }
        }

        if(exceptions.toString().length()>0)
            throw new Exception(exceptions.toString());

        return successes.toString();
    }
}
