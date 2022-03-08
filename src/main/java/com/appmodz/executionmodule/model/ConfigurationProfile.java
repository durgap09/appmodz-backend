package com.appmodz.executionmodule.model;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Configuration_Profile")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
public class ConfigurationProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Id")
    private long id;

    @Column(name="Name")
    private String name;

    @Type(type = "json")
    @Column(name="Config",columnDefinition = "json")
    private Object config;

    @ManyToOne
    @JoinColumn(name="ConfigurationProfile_CloudPlatform_fk")
    private CloudPlatform cloudPlatform;

    @ManyToOne
    @JoinColumn(name="ConfigurationProfile_User_fk")
    private User owner;
}
