package com.appmodz.executionmodule.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class StackRequestDTO extends SearchRequestDTO{
    MultipartFile file;
    String action;
    Long id;
    Long terraformBackendId;
    Long ownerId;
    Long workspaceId;
    Long templateId;
    Long cloudPlatformId;
    Long configurationProfileId;
    Long stackGroupId;
    String stackState;
    String name;
    String stackLocation;
    String awsAccessKey;
    String awsSecretAccessKey;
    String awsRegion;
    List<Long> ids;
    Boolean isWizard;
    String stackWizardType;
    Object wizardState;
}
