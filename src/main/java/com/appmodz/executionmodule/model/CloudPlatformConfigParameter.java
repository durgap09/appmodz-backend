package com.appmodz.executionmodule.model;

import javax.persistence.*;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Cloud_Platform_Config_Parameters")
public class CloudPlatformConfigParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Id")
    private long id;

    @Column(name="Name")
    private String name;

    @Column(name="Appmodz_Name")
    private String appmodzName;

    @OneToOne
    @JoinColumn(name="CloudPlatformConfigParametersFk")
    private CloudPlatform cloudPlatform;

}
