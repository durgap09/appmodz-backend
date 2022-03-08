package com.appmodz.executionmodule.dto;

import com.appmodz.executionmodule.model.Organization;
import com.appmodz.executionmodule.model.TerraformBackend;
import com.appmodz.executionmodule.model.User;
import com.appmodz.executionmodule.model.Workspace;
import java.util.Date;

public class StackResponseDTO {

    private long stackId;

    private User owner;

    private TerraformBackend terraformBackend;

    private Organization organization;

    private Workspace workspace;

    private String awsRegion;

    private String awsAccessKey;

    private String awsSecretAccessKey;

    private Date stackCreatedOn;

    private Boolean stackIsDeleting;

    private Boolean stackIsDeployed;

    private Boolean stackIsWizardStack;

    private String stackWizardType;

    private Boolean stackIsApplied;
}
