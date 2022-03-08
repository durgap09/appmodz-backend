package com.appmodz.executionmodule.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Properties_L5")
public class PropertyL5 {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Property_Id")
    private long id;

    @Column(name="Property_Name")
    private String name;

    @Column(name="Property_Hint",length=5000)
    private String hint;

    @Column(name="Property_Appmodz_Name")
    private String appmodzName;

    @Column(name="Property_Type")
    private String type;

    @Column(name="Property_Default_Value")
    private String defaultValue;

    @Column(name="Property_Is_Connectable")
    private Boolean isConnectable;

    @Column(name="Property_Is_Visible")
    private Boolean isVisible;

    @Column(name="Property_Terraform_Root_Name")
    private String propertyTerraformRootName;

    @Column(name="Property_Is_MultiValued")
    private Boolean isMultiValued;

    @Column(name="Property_Created_On")
    @CreationTimestamp
    private Date propertyCreatedOn;

    @Column(name="Property_Updated_On")
    @UpdateTimestamp
    private Date propertyUpdatedOn;
}
