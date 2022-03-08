package com.appmodz.executionmodule.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Licenses")
public class License {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="License_Id")
    private long licenseId;

    @Column(name="License_Name")
    private String licenseName;

    @Column(name="License_Rank")
    private long licenseRank;

    @JsonIgnore
    @OneToMany(targetEntity=LicensePermission.class, fetch = FetchType.LAZY)
    @JoinColumn(name="License_Fk")
    private List<LicensePermission> licensePermissions;
}
