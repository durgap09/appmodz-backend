package com.appmodz.executionmodule.dto;

import java.util.List;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class TemplateRequestDTO extends SearchRequestDTO{
    String name;
    String action;
    long stackId;
    long id;
    Object state;
    List<Long> ids;
}
