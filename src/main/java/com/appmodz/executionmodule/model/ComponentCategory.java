package com.appmodz.executionmodule.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Components_Categories")
public class ComponentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Component_Category_Id")
    private long componentCategoryId;

    @Column(name="Component_Category_Name")
    private String componentCategoryName;

    @ManyToOne
    @JoinColumn(name="Cloud_Platforms_Id")
    private CloudPlatform cloudPlatform;

    @Column(name="Component_Category_Created_On")
    @CreationTimestamp
    private Date componentCategoryCreatedOn;

    @Column(name="Component_Category_Updated_On")
    @UpdateTimestamp
    private Date componentCategoryUpdatedOn;
}
