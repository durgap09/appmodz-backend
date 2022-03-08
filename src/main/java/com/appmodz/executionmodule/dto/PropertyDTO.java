package com.appmodz.executionmodule.dto;

import com.appmodz.executionmodule.model.PropertyL2;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@lombok.Getter
@lombok.Setter
public class PropertyDTO {
    private Long id;
    private List<Long> ids;
    private int level;
    private String name;
    private String hint;
    private String appmodzName;
    private String type;
    private String defaultValue;
    private Boolean isConnectable;
    private Boolean isVisible;
    private String propertyTerraformRootName;
    private Boolean isMultiValued;
    private Boolean isAdvanced;
    private List<PropertyDTO> subProperties;
    private Date propertyCreatedOn;
    private Date propertyUpdatedOn;
}
