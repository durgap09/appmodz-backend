package com.appmodz.executionmodule.model;

import javax.persistence.*;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="License_Permissions")
public class LicensePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="License_Permission_Id")
    private long licensePermissionId;

    @Column(name="License_Permission_Description")
    private String licensePermissionDescription;

    @Column(name="License_Permission_Name")
    private String licensePermissionName;

    @Column(name="License_Permision_Resource_Limit")
    private Long licensePermissionResourceLimit;

    @Column(name="License_Permision_Allowed")
    private Boolean licensePermissionAllowed;
}
