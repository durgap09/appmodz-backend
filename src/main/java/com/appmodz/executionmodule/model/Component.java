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
@Table(name="Components")
public class Component {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Component_Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="Component_Category_Id")
    private ComponentCategory componentCategory;

    @Column(name="Component_Name")
    private String name;

    @Column(name="Component_Appmodz_Name")
    private String appmodzName;

    @Column(name="Component_Is_Visible")
    private Boolean isVisible;

    @Column(name="Component_IAC_Type")
    private long iacType;

    @Column(name="Component_Terraform_Resource_Path")
    private String path;

    @OneToMany(targetEntity=Property.class, fetch = FetchType.EAGER,cascade=CascadeType.ALL)
    @JoinColumn(name="Component_Fk")
    private List<Property> properties;

    @Column(name="Component_Created_On")
    @CreationTimestamp
    private Date componentCreatedOn;

    @Column(name="Component_Updated_On")
    @UpdateTimestamp
    private Date componentUpdatedOn;
}
