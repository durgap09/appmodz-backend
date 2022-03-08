package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.*;
import com.appmodz.executionmodule.dto.PermissionDTO;
import com.appmodz.executionmodule.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtParser;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UtilService {

    @Autowired
    UserService userService;

    @Autowired
    UserOrganizationDAO userOrganizationDAO;

    @Autowired
    RolePermissionDAO rolePermissionDAO;

    @Autowired
    WorkspaceDAO workspaceDAO;

    @Autowired
    ApplicationLogDAO applicationLogDAO;

    @Transactional
    public Boolean checkLicense(String requestPermission) {
        try {
            User user = this.getAuthenticatedUser();
            List<UserOrganization> userOrganizations = userOrganizationDAO.getUserOrganizations(user.getUserId());
            License license = user.getUserLicense();
            if(license==null) {
                return true;
            } else {
                List<LicensePermission> licensePermissions = license.getLicensePermissions();
                for(LicensePermission licensePermission:licensePermissions) {
                    if(requestPermission.equals(licensePermission.getLicensePermissionName())){
                        if(requestPermission.equals("CREATE_WORKSPACE")) {
                            long workspaceCount = 0;
                            for(UserOrganization userOrganization:userOrganizations) {
                                workspaceCount = workspaceCount + workspaceDAO.getCountByOrganizationId(
                                        userOrganization.getOrganization().getOrganizationId()
                                );
                            }
                            return workspaceCount < licensePermission.getLicensePermissionResourceLimit();
                        }
                        else if(requestPermission.equals("CREATE_ORGANIZATION")) {
                            long orgCount = userOrganizations.size();
                            return orgCount < licensePermission.getLicensePermissionResourceLimit();
                        }
                        else if(requestPermission.equals("CODE_EDITOR")) {
                            return licensePermission.getLicensePermissionAllowed();
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean checkPermission(PermissionDTO permissionDTO, String requestPermission) {
        try {
            User user = this.getAuthenticatedUser();
            UserOrganization userOrganization = this.getUserOrganization();
            if(userOrganization!=null) {
                RolePermissions rolePermissions = rolePermissionDAO.getByRoleId(userOrganization.getRole().getRoleId());
                List<Permission> permissions = rolePermissions.getPermissions();
                for (Permission permission: permissions) {
                    if(permission.getPermissionName().equals(requestPermission)) {
                        if(permission.getPermissionScope().equals("ORG")) {
                            return permissionDTO.getOrganizationIds().contains(userOrganization.
                                    getOrganization().getOrganizationId()) ;
                        }
                        if(permission.getPermissionScope().equals("SELF")) {
                            return user.getUserId() == permissionDTO.getUserId();
                        }
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


    public String getPermissionScope(String requestPermission) {
        try {
            User user = this.getAuthenticatedUser();
            UserOrganization userOrganization = this.getUserOrganization();
            if(userOrganization!=null) {
                RolePermissions rolePermissions = rolePermissionDAO.getByRoleId(userOrganization.getRole().getRoleId());
                List<Permission> permissions = rolePermissions.getPermissions();
                for (Permission permission: permissions) {
                    if(permission.getPermissionName().equals(requestPermission)) {
                        return permission.getPermissionScope();
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }


    public User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }
        try {
            return userService.getUserByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UserOrganization getUserOrganization() throws Exception{
        Collection<?extends GrantedAuthority> granted = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (GrantedAuthority authority : granted) {
            Long userOrganizationId = Long.parseLong(authority.getAuthority());
            UserOrganization userOrganization = userOrganizationDAO.getUserOrganization(userOrganizationId);
            return userOrganization;
        }
        return null;
    }

    @Async
    public void doLog(String userName, Logger logger, String level,String message) {
        switch (level) {
            case "info":
                logger.info("User " + userName + ": " + message);
                break;
            case "debug":
                logger.debug(message);
                break;
            case "error":
                logger.error(message);
                break;
        }
        ApplicationLog applicationLog = new ApplicationLog();
        applicationLog.setUserName(userName);
        applicationLog.setLog(message);
        applicationLog.setLevel(level);
        applicationLog.setTime(new Date());
        applicationLogDAO.save(applicationLog);
    }

    public void logEvents(String userName, Logger logger, String message) {
        if(userName==null)
            userName = this.getAuthenticatedUser().getUserName();
        doLog(userName,logger,"info",message);
    }

    public void logEvents(String userName, Logger logger, String message, String level) {
        if(userName==null)
            userName = this.getAuthenticatedUser().getUserName();
        doLog(userName,logger,level,message);
    }


    public Map<String, Object> getPayloadFromJwt(String token) throws Exception{
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
        return claims;
    }

}
