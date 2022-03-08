package com.appmodz.executionmodule.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@lombok.Getter
@lombok.Setter
@Table(name="Stack_Groups")
public class StackGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Stack_Group_Id")
    private long stackGroupId;

    @Column(name="Stack_Group_Name")
    private String stackGroupName;

    @JsonIgnore
    @OneToMany(targetEntity=Stack.class, fetch = FetchType.LAZY, mappedBy = "stackGroup")
    private List<Stack> stacks;

    @ManyToOne
    @JoinColumn(name="StackGroup_User_fk")
    private User owner;

    @Column(name="Stack_Group_Updated_On")
    @UpdateTimestamp
    private Date stackGroupUpdatedOn;

    @Column(name="Stack_Group_Created_On")
    @CreationTimestamp
    private Date stackGroupCreatedOn;
}
