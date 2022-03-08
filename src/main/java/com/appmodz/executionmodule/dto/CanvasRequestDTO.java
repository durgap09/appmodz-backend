package com.appmodz.executionmodule.dto;

import com.appmodz.executionmodule.model.Property;

import java.util.List;
import java.util.Objects;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class CanvasRequestDTO extends SearchRequestDTO {
    long stackId;
    String path;
    Object draftState;
    Boolean isDraft;

    long cloudPlatformId;
    long componentCategoryId;
    long appmodzCategoryId;
    String componentName;
    String appmodzName;
    String componentFullName;
    List<String> componentFullNames;
    String appmodzCategoryName;
    Boolean replaceIfExists;

    List<Property> componentProperties;

    public CanvasRequestDTO() {
        this.path = Objects.requireNonNullElse(path, "");
    }
}
