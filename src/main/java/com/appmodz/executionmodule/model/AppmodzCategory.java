package com.appmodz.executionmodule.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Appmodz_Categories")
public class AppmodzCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Appmodz_Category_Id")
    private long appmodzCategoryId;

    @Column(name="Appmodz_Category_Name")
    private String appmodzCategoryName;

    @Column(name="Appmodz_Category_Created_On")
    @CreationTimestamp
    private Date appmodzCategoryCreatedOn;

    @Column(name="Appmodz_Category_Updated_On")
    @UpdateTimestamp
    private Date appmodzCategoryUpdatedOn;
}
