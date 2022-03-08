package com.appmodz.executionmodule.dto;

import com.appmodz.executionmodule.model.User;
import com.appmodz.executionmodule.model.UserOrganization;

import java.util.List;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class UserResponseDTO {
    User user;
    List<UserOrganization> userOrganizations;
    Boolean isEditable;
}
