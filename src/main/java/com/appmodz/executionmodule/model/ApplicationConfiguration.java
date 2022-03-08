package com.appmodz.executionmodule.model;

import javax.persistence.*;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Application_Configuration")
public class ApplicationConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Id")
    private long id;

    @Column(name="Key")
    private String key;

    @Column(name="DataType")
    private String datatype;

    @Column(name="Value")
    private String value;
}
