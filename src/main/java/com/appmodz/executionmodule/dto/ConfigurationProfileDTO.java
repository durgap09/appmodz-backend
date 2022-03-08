package com.appmodz.executionmodule.dto;

import java.util.List;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class ConfigurationProfileDTO extends SearchRequestDTO{
    Long id;
    List<Long> ids;
    String name;
    Object config;
    Long cloudPlatformId;
}
