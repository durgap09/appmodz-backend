package com.appmodz.executionmodule.dto;

import java.util.List;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class StackGroupRequestDTO extends SearchRequestDTO {
    String stackGroupName;
    Long stackGroupId;
    List<String> components;
    List<Long> stackIds;
    List<Long> ids;
}
