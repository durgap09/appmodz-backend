package com.appmodz.executionmodule.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Properties_L2")
public class PropertyL2 {
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


    @OneToMany(targetEntity= PropertyL3.class, fetch = FetchType.EAGER,cascade=CascadeType.ALL)
    @JoinColumn(name="SubProperty_Fk")
    private List<PropertyL3> subProperties;

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
