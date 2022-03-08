package com.appmodz.executionmodule.dto;

import java.util.Map;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class AnalyticsResponseDTO {
    Long stacks;
    Long organizations;
    Long workspaces;
    Long users;
    Long deployedStacks;
    Map<String,Integer> deployedComponents;
}
