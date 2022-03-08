package com.appmodz.executionmodule.dto;

import com.appmodz.executionmodule.model.ComponentCategory;
import com.appmodz.executionmodule.model.Property;

import java.util.Date;
import java.util.List;

@lombok.Getter
@lombok.Setter
public class ComponentDTO {
    private Long id;
    private ComponentCategory componentCategory;
    private String name;
    private String appmodzName;
    private Boolean isVisible;
    private Long iacType;
    private String path;
    private List<Long> ids;
    private List<Property> properties;
    private Date componentCreatedOn;
    private Date componentUpdatedOn;
}
