package com.appmodz.executionmodule.model;

import javax.persistence.*;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Product_Packages")
public class ProductPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Product_Package_Id")
    private long productPackageId;

    @Column(name="Product_Package_Name")
    private String productPackageName;
}
