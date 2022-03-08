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
@Table(name="Appmodz_Components_Categories")
public class AppmodzComponentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Appmodz_Components_Category_Id")
    private long appmodzComponentsCategoryId;

    @OneToOne
    @JoinColumn(name="Appmodz_Category_Id")
    private AppmodzCategory appmodzCategory;

    @OneToMany(targetEntity= Component.class, fetch = FetchType.EAGER)
    @JoinColumn(name="Appmodz_Components_Category_Fk")
    private List<Component> components;

    @Column(name="Appmodz_Components_Category_Created_On")
    @CreationTimestamp
    private Date appmodzComponentsCategoryCreatedOn;

    @Column(name="Appmodz_Components_Category_Updated_On")
    @UpdateTimestamp
    private Date appmodzComponentsCategoryUpdatedOn;

}
