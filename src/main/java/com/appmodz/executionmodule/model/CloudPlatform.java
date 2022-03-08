package com.appmodz.executionmodule.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Cloud_Platforms")
public class CloudPlatform {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Cloud_Platform_Id")
    private long cloudPlatformId;

    @Column(name="Cloud_Platform_Name")
    private String cloudPlatformName;

    @Column(name="Cloud_Platform_Appmodz_Name")
    private String cloudPlatformAppmodzName;

    @Column(name="Cloud_Platform_Pulumi_Library")
    private String cloudPlatformPulumiLibrary;

    @Column(name="Cloud_Platform_Created_On")
    @CreationTimestamp
    private Date cloudPlatformCreatedOn;

    @Column(name="Cloud_Platform_Updated_On")
    @UpdateTimestamp
    private Date cloudPlatformUpdatedOn;
}
