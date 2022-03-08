package com.appmodz.executionmodule.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Application_Logs")
public class ApplicationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Id")
    private long id;

    @Column(name="UserName")
    private String userName;

    @Column(name="Log",columnDefinition="TEXT")
    private String log;

    @Column(name="level")
    private String level;

    @Column(name="Time")
    private Date time;
}
