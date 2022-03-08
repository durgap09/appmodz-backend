package com.appmodz.executionmodule.dto;

import java.util.List;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.Builder
public class PermissionDTO {
    Long userId;
    Long workspaceId;
    List<Long> organizationIds;
}
