package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.*;
import com.appmodz.executionmodule.dto.AnalyticsResponseDTO;
import com.appmodz.executionmodule.model.User;
import com.appmodz.executionmodule.model.UserOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    StackDAO stackDAO;

    @Autowired
    UserDAO userDAO;

    UtilService utilService;

    @Autowired
    WorkspaceDAO workspaceDAO;

    @Autowired
    OrganizationDAO organizationDAO;

    @Autowired
    UserOrganizationDAO userOrganizationDAO;

    @Autowired
    public AnalyticsService(@Lazy UtilService utilService) {
        this.utilService = utilService;
    }

    public AnalyticsResponseDTO getAnalytics() {
        AnalyticsResponseDTO analyticsResponseDTO = new AnalyticsResponseDTO();
        if (utilService.getPermissionScope("GET_ORGANIZATION")!=null&&utilService.getPermissionScope("GET_ORGANIZATION").equals("GLOBAL")) {
            analyticsResponseDTO.setUsers(userDAO.getCount());
            analyticsResponseDTO.setOrganizations(organizationDAO.getCount());
            analyticsResponseDTO.setWorkspaces(workspaceDAO.getCount());
            analyticsResponseDTO.setStacks(stackDAO.getCount());
            analyticsResponseDTO.setDeployedStacks(stackDAO.getCountDeployed());

            Map<String,Integer> countMap = new HashMap<String, Integer>();
            List<Map<String,Integer>> countList = stackDAO.getDeployedCount();
            for(Map<String,Integer> m:countList) {
                if(m!=null) {
                    for (Map.Entry<String,Integer> entry : m.entrySet()) {
                        Integer count = countMap.get(entry.getKey());
                        countMap.put(entry.getKey(), (count == null) ? entry.getValue() : count + entry.getValue());
                    }
                }
            }

            analyticsResponseDTO.setDeployedComponents(countMap);
        } else {
            User user = utilService.getAuthenticatedUser();
            List<UserOrganization> userOrganizations = userOrganizationDAO.getUserOrganizations(user.getUserId());
            long userCount = 0;
            long orgCount = 0;
            long workspaceCount = 0;
            long stackCount = 0;
            long deployedStacksCount = 0;
            Map<String,Integer> countMap = new HashMap<String, Integer>();
            List<Long> organizationIds = new ArrayList<>();
            for(UserOrganization userOrganization:userOrganizations) {
                workspaceCount = workspaceCount+workspaceDAO.getCountByOrganizationId(userOrganization.getOrganization()
                        .getOrganizationId());
                stackCount = stackCount+stackDAO.getCountByOrganizationId(userOrganization.getOrganization()
                        .getOrganizationId());
                deployedStacksCount = deployedStacksCount+stackDAO.getCountDeployedByOrganizationId(userOrganization.getOrganization()
                        .getOrganizationId());
                orgCount = orgCount + 1;
                List<Map<String,Integer>> countList = stackDAO.getDeployedCountByOrganizationId(userOrganization.
                        getOrganization().getOrganizationId());
                organizationIds.add(userOrganization.getOrganization()
                        .getOrganizationId());
                for(Map<String,Integer> m:countList) {
                    if(m!=null) {
                        for (Map.Entry<String,Integer> entry : m.entrySet()) {
                            Integer count = countMap.get(entry.getKey());
                            countMap.put(entry.getKey(), (count == null) ? entry.getValue() : count + entry.getValue());
                        }
                    }
                }
            }
            userCount = userOrganizationDAO.getCountOfUsersInOrganizations(organizationIds);
            analyticsResponseDTO.setUsers(userCount);
            analyticsResponseDTO.setOrganizations(orgCount);
            analyticsResponseDTO.setWorkspaces(workspaceCount);
            analyticsResponseDTO.setStacks(stackCount);
            analyticsResponseDTO.setDeployedStacks(deployedStacksCount);
            analyticsResponseDTO.setDeployedComponents(countMap);
        }

        return analyticsResponseDTO;
    }
}
