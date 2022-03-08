package com.appmodz.executionmodule.dto;

import com.appmodz.executionmodule.model.*;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class AuthResponseDTO {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String jwt;

    private User user;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String roleName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<UserOrganization> userOrganizations;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Permission> permissions;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String licenseName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<LicensePermission> licensePermissions;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object TenDukeResponse;
}
