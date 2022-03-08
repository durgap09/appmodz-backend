package com.appmodz.executionmodule.dto;

import com.appmodz.executionmodule.model.CloudPlatform;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

@lombok.Getter
@lombok.Setter
public class PulumiRequestDTO {
    String stackPath;
    Object draftState;
    StackConfigDTO config;
    String token;
    String prev_token;
    Boolean isStackListEmpty;
    String stackWizardType;
    CloudPlatform cloudPlatform;
    String component_full_name;
    Object deployedComponents;

    @SneakyThrows
    @JsonIgnoreProperties
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
