package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.PermissionDAO;
import com.appmodz.executionmodule.dao.RoleDAO;
import com.appmodz.executionmodule.dao.RolePermissionDAO;
import com.appmodz.executionmodule.dao.UserOrganizationDAO;
import com.appmodz.executionmodule.dto.PermissionDTO;
import com.appmodz.executionmodule.dto.RolesRequestDTO;
import com.appmodz.executionmodule.dto.SearchRequestDTO;
import com.appmodz.executionmodule.dto.SearchResultDTO;
import com.appmodz.executionmodule.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleService {
    @Autowired
    RoleDAO roleDAO;

    @Autowired
    RolePermissionDAO rolePermissionDAO;

    @Autowired
    PermissionDAO permissionDAO;

    @Autowired
    UtilService utilService;

    @Autowired
    UserOrganizationDAO userOrganizationDAO;

    public Role getRoleById(long id) throws Exception{
        Role role = roleDAO.get(id);
        if(role==null)
            throw new Exception("No role with this role id exists");
        else if(!utilService.checkPermission( PermissionDTO.builder().build(), "GET_ROLE"))
            throw new Exception("GET ROLE ACTION NOT PERMITTED FOR THIS USER");
        utilService.logEvents(null,log,"Fetched Role With Id "+ role.getRoleId());
        return role;
    }


    public void deleteRole(Long id) throws Exception {
        Role role = roleDAO.get(id);
        if(Boolean.TRUE.equals(role.getRoleIsDefault()))
            throw new Exception("Default Roles Cant Be Deleted");
        if(!utilService.checkPermission(PermissionDTO.builder().build(),"DELETE_ROLE"))
            throw new Exception("DELETE ROLE ACTION NOT PERMITTED FOR THIS USER");
        roleDAO.delete(role);
        utilService.logEvents(null,log,"Deleted Role With Id "+ role.getRoleId());
    }

    public String deleteMultipleRoles(RolesRequestDTO rolesRequestDTO) throws Exception{
        List<Long> ids = rolesRequestDTO.getIds();
        StringBuilder exceptions = new StringBuilder();
        StringBuilder successes = new StringBuilder();
        for (long id: ids) {
            Role role = roleDAO.get(id);
            if(!utilService.checkPermission(PermissionDTO.builder().build(),"DELETE_ROLE"))
                exceptions.append("DELETE ROLE ACTION NOT PERMITTED FOR THIS USER FOR ID").append(id);
            try {
                List<UserOrganization> userOrganizations = userOrganizationDAO.getUserOrganizationsByRole(id);
                if(userOrganizations.size()>0)
                    throw new Exception("Role "+role.getRoleName()+" Attached To A User");
                RolePermissions rolePermissions = rolePermissionDAO.getByRoleId(id);
                rolePermissionDAO.delete(rolePermissions);
                roleDAO.delete(role);
                utilService.logEvents(null,log,"Deleted Role With Id "+ role.getRoleId());
                successes.append("Successfully Deleted ").append(id).append("\n");
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                exceptions.append("Unable To Delete id ").append(id).append(" Due To Possible Foreign Key associations").append("\n");
            } catch (Exception e) {
                e.printStackTrace();
                exceptions.append(e.getMessage());
            }
        }

        if(exceptions.toString().length()>0)
            throw new Exception(exceptions.toString());

        return successes.toString();
    }

    public Role createRole(RolesRequestDTO rolesRequestDTO) throws Exception{
        if(!utilService.checkPermission(PermissionDTO.builder().build(),"CREATE_ROLE"))
            throw new Exception("CREATE ROLE ACTION NOT PERMITTED FOR THIS USER");
        Role role = new Role();
        role.setRoleName(rolesRequestDTO.getName());
        role.setRoleDescription(rolesRequestDTO.getDescription());
        roleDAO.save(role);
        List<Permission> perms= new ArrayList<>();
        RolePermissions rolePermissions = new RolePermissions();
        rolePermissions.setPermissions(perms);
        rolePermissions.setRoleId(role);
        rolePermissionDAO.save(rolePermissions);
        utilService.logEvents(null,log,"Created Role With Id "+ role.getRoleId());
        return role;
    }

    public Role updateRole(RolesRequestDTO rolesRequestDTO) throws Exception{
        Role role = roleDAO.get(rolesRequestDTO.getId());
        if(Boolean.TRUE.equals(role.getRoleIsDefault()))
            throw new Exception("Default Roles Cant Be Modified");
        if(!utilService.checkPermission(PermissionDTO.builder().build(),"UPDATE_ROLE"))
            throw new Exception("UPDATE ROLE ACTION NOT PERMITTED FOR THIS USER");
        if(rolesRequestDTO.getName() != null)
            role.setRoleName(rolesRequestDTO.getName());
        if(rolesRequestDTO.getDescription() != null)
            role.setRoleDescription(rolesRequestDTO.getDescription());
        roleDAO.save(role);
        utilService.logEvents(null,log,"Updated Role With Id "+ role.getRoleId());
        return role;
    }

    public List listRoles() {
        List<Role> roles = roleDAO.getAll();
        roles = roles.stream().filter(u->utilService.checkPermission(PermissionDTO.builder().build(),
                "GET_ROLE")).collect(Collectors.toList());
        utilService.logEvents(null,log,"Fetched Roles");
        return roles;
    }

    public SearchResultDTO searchRoles(SearchRequestDTO searchRequestDTO) {
        SearchResultDTO roles = roleDAO.search(searchRequestDTO);
        roles.setData((List) roles.getData().stream()
                .filter(u->utilService.checkPermission(PermissionDTO.builder().build(),
                        "GET_ROLE")).collect(Collectors.toList()));
        utilService.logEvents(null,log,"Searched Roles");
        return roles;
    }

    public RolePermissions getRolePermissionsByRoleId(long id) throws Exception{
        RolePermissions rolePermissions = rolePermissionDAO.getByRoleId(id);
        if(rolePermissions==null)
            throw new Exception("No role permissions with this role id exists");
        else if(!utilService.checkPermission( PermissionDTO.builder().build(), "GET_ROLE"))
            throw new Exception("GET ROLE ACTION NOT PERMITTED FOR THIS USER");
        utilService.logEvents(null,log,"Fetched Role Permissions By Id "+id);
        return rolePermissions;
    }

    public RolePermissions editPermissionsOfRole(RolesRequestDTO rolesRequestDTO) throws Exception{
        Role role = roleDAO.get(rolesRequestDTO.getId());
        if(Boolean.TRUE.equals(role.getRoleIsDefault()))
            throw new Exception("Default Roles Cant Be Modified");
        RolePermissions rolePermissions = rolePermissionDAO.getByRoleId(rolesRequestDTO.getId());
        if(rolePermissions==null)
            throw new Exception("No role permissions with this role id exists");
        else if(!utilService.checkPermission( PermissionDTO.builder().build(), "UPDATE_ROLE"))
            throw new Exception("UPDATE ROLE ACTION NOT PERMITTED FOR THIS USER");
        rolePermissions.setPermissions(rolesRequestDTO.getPermissions());
        rolePermissionDAO.save(rolePermissions);
        utilService.logEvents(null,log,"Edited Permissions Of Role With Id "+rolesRequestDTO.getId());
        return rolePermissions;
    }

    public List listPermissions() {
        List<Permission> permissions = permissionDAO.getAll();
        permissions = permissions.stream().filter(u->utilService.checkPermission(PermissionDTO.builder().build(),
                "GET_PERMISSION")).collect(Collectors.toList());
        utilService.logEvents(null,log,"Listed Permissions");
        return permissions;
    }

    public Role createSuperAdminRole() throws Exception{
        String permissionString = "[\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can get users\",\n" +
                "                \"permissionName\": \"GET_USER\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114093,\n" +
                "                \"permissionUpdatedOn\": 1634732114093\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can create users\",\n" +
                "                \"permissionName\": \"CREATE_USER\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114094,\n" +
                "                \"permissionUpdatedOn\": 1634732114094\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can update users\",\n" +
                "                \"permissionName\": \"UPDATE_USER\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114094,\n" +
                "                \"permissionUpdatedOn\": 1634732114094\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can delete users\",\n" +
                "                \"permissionName\": \"DELETE_USER\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114095,\n" +
                "                \"permissionUpdatedOn\": 1634732114095\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can get organizations\",\n" +
                "                \"permissionName\": \"GET_ORGANIZATION\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114095,\n" +
                "                \"permissionUpdatedOn\": 1634732114095\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can create organizations\",\n" +
                "                \"permissionName\": \"CREATE_ORGANIZATION\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114096,\n" +
                "                \"permissionUpdatedOn\": 1634732114096\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can update organizations\",\n" +
                "                \"permissionName\": \"UPDATE_ORGANIZATION\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114096,\n" +
                "                \"permissionUpdatedOn\": 1634732114096\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can delete organizations\",\n" +
                "                \"permissionName\": \"DELETE_ORGANIZATION\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114097,\n" +
                "                \"permissionUpdatedOn\": 1634732114097\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can get WORKSPACE\",\n" +
                "                \"permissionName\": \"GET_WORKSPACE\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114097,\n" +
                "                \"permissionUpdatedOn\": 1634732114097\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can create WORKSPACE\",\n" +
                "                \"permissionName\": \"CREATE_WORKSPACE\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114098,\n" +
                "                \"permissionUpdatedOn\": 1634732114098\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can update WORKSPACE\",\n" +
                "                \"permissionName\": \"UPDATE_WORKSPACE\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114098,\n" +
                "                \"permissionUpdatedOn\": 1634732114098\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can delete WORKSPACE\",\n" +
                "                \"permissionName\": \"DELETE_WORKSPACE\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114098,\n" +
                "                \"permissionUpdatedOn\": 1634732114098\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can DELETE ROLE\",\n" +
                "                \"permissionName\": \"DELETE_ROLE\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114099,\n" +
                "                \"permissionUpdatedOn\": 1634732114099\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can UPDATE ROLE\",\n" +
                "                \"permissionName\": \"UPDATE_ROLE\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732114099,\n" +
                "                \"permissionUpdatedOn\": 1634732114099\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can CREATE ROLE\",\n" +
                "                \"permissionName\": \"CREATE_ROLE\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": 1634732279718,\n" +
                "                \"permissionUpdatedOn\": 1634732279718\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can GET ROLE\",\n" +
                "                \"permissionName\": \"GET_ROLE\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": null,\n" +
                "                \"permissionUpdatedOn\": null\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": null,\n" +
                "                \"permissionName\": \"GET_STACK\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": null,\n" +
                "                \"permissionUpdatedOn\": null\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": null,\n" +
                "                \"permissionName\": \"CREATE_STACK\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": null,\n" +
                "                \"permissionUpdatedOn\": null\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": null,\n" +
                "                \"permissionName\": \"UPDATE_STACK\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": null,\n" +
                "                \"permissionUpdatedOn\": null\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": null,\n" +
                "                \"permissionName\": \"DELETE_STACK\",\n" +
                "                \"permissionScope\": \"GLOBAL\",\n" +
                "                \"permissionCreatedOn\": null,\n" +
                "                \"permissionUpdatedOn\": null\n" +
                "            }\n" +
                "        ]";

        ObjectMapper mapper = new ObjectMapper();
        List<Permission> permissions = mapper.readValue(permissionString, new TypeReference<List<Permission>>(){});
        Role role = new Role();
        role.setRoleName("SuperAdmin");
        role.setRoleDescription("SuperAdmin");
        role.setRoleIsDefault(true);
        roleDAO.save(role);

        RolePermissions rolePermissions = new RolePermissions();
        rolePermissions.setRoleId(role);
        rolePermissions.setPermissions(permissions);
        rolePermissionDAO.save(rolePermissions);

        return role;
    }

    public Role createOrgAdminRole() throws Exception{
        String permissionString = "[\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can get users\",\n" +
                "                \"permissionName\": \"GET_USER\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": 1634732114093,\n" +
                "                \"permissionUpdatedOn\": 1634732114093\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can create users\",\n" +
                "                \"permissionName\": \"CREATE_USER\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": 1634732114094,\n" +
                "                \"permissionUpdatedOn\": 1634732114094\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can update users\",\n" +
                "                \"permissionName\": \"UPDATE_USER\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": 1634732114094,\n" +
                "                \"permissionUpdatedOn\": 1634732114094\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can delete users\",\n" +
                "                \"permissionName\": \"DELETE_USER\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": 1634732114095,\n" +
                "                \"permissionUpdatedOn\": 1634732114095\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can get WORKSPACE\",\n" +
                "                \"permissionName\": \"GET_WORKSPACE\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": 1634732114097,\n" +
                "                \"permissionUpdatedOn\": 1634732114097\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can create WORKSPACE\",\n" +
                "                \"permissionName\": \"CREATE_WORKSPACE\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": 1634732114098,\n" +
                "                \"permissionUpdatedOn\": 1634732114098\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can update WORKSPACE\",\n" +
                "                \"permissionName\": \"UPDATE_WORKSPACE\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": 1634732114098,\n" +
                "                \"permissionUpdatedOn\": 1634732114098\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": \"Can delete WORKSPACE\",\n" +
                "                \"permissionName\": \"DELETE_WORKSPACE\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": 1634732114098,\n" +
                "                \"permissionUpdatedOn\": 1634732114098\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": null,\n" +
                "                \"permissionName\": \"GET_STACK\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": null,\n" +
                "                \"permissionUpdatedOn\": null\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": null,\n" +
                "                \"permissionName\": \"CREATE_STACK\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": null,\n" +
                "                \"permissionUpdatedOn\": null\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": null,\n" +
                "                \"permissionName\": \"UPDATE_STACK\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": null,\n" +
                "                \"permissionUpdatedOn\": null\n" +
                "            },\n" +
                "            {\n" +
                "                \"permissionDescription\": null,\n" +
                "                \"permissionName\": \"DELETE_STACK\",\n" +
                "                \"permissionScope\": \"ORG\",\n" +
                "                \"permissionCreatedOn\": null,\n" +
                "                \"permissionUpdatedOn\": null\n" +
                "            }\n" +
                "        ]";

        ObjectMapper mapper = new ObjectMapper();
        List<Permission> permissions = mapper.readValue(permissionString, new TypeReference<List<Permission>>(){});
        Role role = new Role();
        role.setRoleName("OrgAdmin");
        role.setRoleDescription("OrgAdmin");
        role.setRoleIsDefault(true);
        roleDAO.save(role);

        RolePermissions rolePermissions = new RolePermissions();
        rolePermissions.setRoleId(role);
        rolePermissions.setPermissions(permissions);
        rolePermissionDAO.save(rolePermissions);
        return role;
    }

}
