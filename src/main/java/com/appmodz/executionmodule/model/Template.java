package com.appmodz.executionmodule.model;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Table(name="Templates")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
public class Template implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="Id")
    private long id;

    @Column(name="name")
    private String name;

    @ManyToOne
    @JoinColumn(name="owner")
    private User owner;

    @Type(type = "json")
    @Column(name="State",columnDefinition = "json")
    private Object state;

}
