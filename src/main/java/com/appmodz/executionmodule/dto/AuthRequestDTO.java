package com.appmodz.executionmodule.dto;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class AuthRequestDTO {
    private String username;
    private String password;
    private String idToken;
    private String accessToken;
    private String email;
    private String token;
    private long userOrganizationId;
}
