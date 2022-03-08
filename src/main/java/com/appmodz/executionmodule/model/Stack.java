package com.appmodz.executionmodule.model;

import com.appmodz.executionmodule.dto.StackButtonStateDTO;
import com.appmodz.executionmodule.dto.StackMessageDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Stacks")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
public class Stack implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Stack_Id")
    private long stackId;

    @OneToOne
    @JoinColumn(name="Stack_Owner_Id")
    private User owner;

    @OneToOne
    @JoinColumn(name="Stack_Workspace_Name")
    private TerraformBackend terraformBackend;

    @ManyToOne
    @JoinColumn(name="Stack_Organization_Id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name="Stack_CloudPlatform_fk")
    private CloudPlatform cloudPlatform;

    @ManyToOne
    @JoinColumn(name="Stack_StackGroup_fk")
    private StackGroup stackGroup;

    @ManyToOne
    @JoinColumn(name="Stack_ConfigurationProfile_fk")
    private ConfigurationProfile configurationProfile;

    @Type(type = "json")
    @Column(name="Stack_State",columnDefinition = "json")
    private Object stackState;

    @Type(type = "json")
    @Column(name="Stack_Draft_State",columnDefinition = "json")
    private Object stackDraftState;

    @Column(name="Stack_Location")
    private String stackLocation;

    @ManyToOne
    @JoinColumn(name="Stack_Workspace_Id")
    private Workspace workspace;

    @Column(name="AWS_Region")
    private String awsRegion;

    @Column(name="AWS_Access_Key")
    private String awsAccessKey;

    @Column(name="AWS_Secret_Access_Key")
    private String awsSecretAccessKey;

    @Column(name="Stack_Created_On")
    @CreationTimestamp
    private Date stackCreatedOn;

    @Column(name="Stack_Is_Deleting")
    private Boolean stackIsDeleting;

    @Column(name="Stack_Is_Deployed")
    private Boolean stackIsDeployed;

    @Column(name="Stack_Is_Wizard_Stack")
    private Boolean stackIsWizardStack;

    @Column(name="Stack_Wizard_Type")
    private String stackWizardType;

    @Type(type = "json")
    @Column(name="Stack_Wizard_State",columnDefinition = "json")
    private Object stackWizardState;

    @Type(type = "json")
    @Column(name="Stack_Deployed_Components",columnDefinition = "json")
    private Object stackDeployedComponents;

    @Type(type = "json")
    @Column(name="Stack_Deployed_Components_With_Count",columnDefinition = "json")
    private Map<String, Integer> stackDeployedComponentsWithCount;

    @Column(name="Stack_Is_Applied")
    private Boolean stackIsApplied;

    @Column(name="Stack_Error_Message")
    private String errorMessage;

    @Type(type = "json")
    @Column(name="Stack_Messages",columnDefinition = "json")
    private List<StackMessageDTO> stackMessages;

    @Type(type = "json")
    @Column(name="Stack_Button_State",columnDefinition = "json")
    private List<StackButtonStateDTO> stackButtonState;

    @Column(name="Stack_Updated_On")
    @UpdateTimestamp
    private Date stackUpdatedOn;
}
