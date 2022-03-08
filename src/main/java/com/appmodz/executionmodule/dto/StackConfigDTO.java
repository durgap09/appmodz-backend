package com.appmodz.executionmodule.dto;

import com.appmodz.executionmodule.model.CloudPlatform;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class StackConfigDTO {
    String stackName;
    String projectName;
    String awsRegion;
    String awsAccessKey;
    String awsSecretKey;
    Object stackConfig;
    CloudPlatform cloudPlatform;
}
