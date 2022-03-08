package com.appmodz.executionmodule.model;
import javax.persistence.*;
import java.util.List;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="UsersOrganizations")
public class UserOrganization {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="User_Organization_Id")
    private long userOrganizationId;

    @ManyToOne
    @JoinColumn(name="User_Id")
    private User user;

    @ManyToOne
    @JoinColumn(name="Users_Organizations_Fk")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name="Users_Roles_Fk")
    private Role role;
}
