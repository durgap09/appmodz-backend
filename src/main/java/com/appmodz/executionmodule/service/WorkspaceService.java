package com.appmodz.executionmodule.service;
import com.appmodz.executionmodule.dao.*;
import com.appmodz.executionmodule.dto.*;
import com.appmodz.executionmodule.model.*;
import com.appmodz.executionmodule.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkspaceService {

    @Autowired
    WorkspaceDAO workspaceDAO;

    @Autowired
    UserDAO userDAO;

    @Autowired
    OrganizationDAO organizationDAO;

    @Autowired UtilService utilService;

    public Workspace getWorkspaceById(long workspaceId) throws Exception{
        Workspace workspace = workspaceDAO.get(workspaceId);
        if(workspace==null)
            throw new Exception("No workspace with this workspace id exists");
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(Collections.singletonList(workspace.getOrganization().getOrganizationId()))
                        .userId(workspace.getOwner().getUserId())
                        .build()
                , "GET_WORKSPACE"))
            throw new Exception("GET WORKSPACE ACTION NOT PERMITTED FOR THIS USER");
        utilService.logEvents(null,log,"Fetched Workspace With Id "+ workspace.getWorkspaceId());
        return workspace;
    }

    public Workspace createWorkspace(WorkspaceRequestDTO workspaceRequestDTO) throws Exception{
        Workspace workspace = new Workspace();
        User user = userDAO.get(workspaceRequestDTO.getOwnerId());
        if(!utilService.checkLicense("CREATE_WORKSPACE"))
            throw new Exception("User's License Doesnt Allow This Operation Or Resource Limit Reached For This Operation");
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(Collections.singletonList(workspaceRequestDTO.getOrganizationId()))
                        .userId(workspaceRequestDTO.getOwnerId())
                        .build()
                , "CREATE_WORKSPACE"))
            throw new Exception("CREATE WORKSPACE ACTION NOT PERMITTED FOR THIS USER");
        if(user==null)
            throw new Exception("No user with this User Id Found "+workspaceRequestDTO.getOwnerId());
        workspace.setOwner(user);
        workspace.setWorkspaceName(workspaceRequestDTO.getName());
        Organization organization = organizationDAO.get(workspaceRequestDTO.getOrganizationId());
        if(organization==null)
            throw new Exception("No organization with this organization Id Found "+workspaceRequestDTO.getOrganizationId());
        workspace.setOrganization(organization);
        workspaceDAO.save(workspace);
        utilService.logEvents(null,log,"Created Workspace With Id "+ workspace.getWorkspaceId());
        return workspace;
    }

    public Workspace editWorkspace(WorkspaceRequestDTO workspaceRequestDTO) throws Exception{
        Workspace workspace = workspaceDAO.get(workspaceRequestDTO.getId());
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(Collections.singletonList(workspace.getOrganization().getOrganizationId()))
                        .userId(workspace.getOwner().getUserId())
                        .build()
                , "UPDATE_WORKSPACE"))
            throw new Exception("UPDATE WORKSPACE ACTION NOT PERMITTED FOR THIS USER");
        if(workspaceRequestDTO.getOwnerId()!=null&&workspaceRequestDTO.getOrganizationId()!=null) {
            User user = userDAO.get(workspaceRequestDTO.getOwnerId());
            Organization organization = organizationDAO.get(workspaceRequestDTO.getOrganizationId());
        }
        else if(workspaceRequestDTO.getOwnerId()!=null) {
            User user = userDAO.get(workspaceRequestDTO.getOwnerId());
            if(user==null)
                throw new Exception("No user with this User Id Found "+workspaceRequestDTO.getOwnerId());
            workspace.setOwner(user);
        }
        else if(workspaceRequestDTO.getOrganizationId()!=null) {
            Organization organization = organizationDAO.get(workspaceRequestDTO.getOrganizationId());
            if(organization==null)
                throw new Exception("No organization with this organization Id Found "+workspaceRequestDTO.getOrganizationId());
            workspace.setOrganization(organization);
        }
        if(workspaceRequestDTO.getName()!=null) {
            workspace.setWorkspaceName(workspaceRequestDTO.getName());
        }
        workspaceDAO.save(workspace);
        utilService.logEvents(null,log,"Edited Workspace With Id "+ workspace.getWorkspaceId());
        return workspace;
    }


    public void deleteWorkspace(Long id) throws Exception{
        Workspace workspace = workspaceDAO.get(id);
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(Collections.singletonList(workspace.getOrganization().getOrganizationId()))
                        .userId(workspace.getOwner().getUserId())
                        .build()
                , "DELETE_WORKSPACE"))
            throw new Exception("DELETE WORKSPACE ACTION NOT PERMITTED FOR THIS USER");
        utilService.logEvents(null,log,"Deleted Workspace With Id "+ workspace.getWorkspaceId());
        workspaceDAO.delete(workspace);
    }

    public String deleteMultipleWorkspaces(WorkspaceRequestDTO workspaceRequestDTO) throws Exception{
        List<Long> ids = workspaceRequestDTO.getIds();
        StringBuilder exceptions = new StringBuilder();
        StringBuilder successes = new StringBuilder();
        for (long id: ids) {
            Workspace workspace = workspaceDAO.get(id);
            if(!utilService.checkPermission(
                    PermissionDTO.builder()
                            .organizationIds(Collections.singletonList(workspace.getOrganization().getOrganizationId()))
                            .userId(workspace.getOwner().getUserId())
                            .build()
                    , "DELETE_WORKSPACE"))
                exceptions.append("DELETE WORKSPACE ACTION NOT PERMITTED FOR THIS USER FOR ID").append(id);
            try {
                workspaceDAO.delete(workspace);
                utilService.logEvents(null,log,"Deleted Workspace With Id "+ workspace.getWorkspaceId());
                successes.append("Successfully Deleted ").append(id).append("\n");
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                exceptions.append("Unable To Delete id ").append(id).append(" Due To Possible Foreign Key associations\n");
            }
        }

        if(exceptions.toString().length()>0)
            throw new Exception(exceptions.toString());

        return successes.toString();
    }

    public List listWorkspaces() {
        List<Workspace> workspaces = workspaceDAO.getAll();
        workspaces =  workspaces.stream().filter(w->utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(Collections.singletonList(w.getOrganization().getOrganizationId()))
                        .userId(w.getOwner().getUserId())
                        .build(),
                "GET_WORKSPACE")).collect(Collectors.toList());
        utilService.logEvents(null,log,"Listed Workspaces");
        return workspaces;
    }

    public SearchResultDTO searchWorkspaces(SearchRequestDTO searchRequestDTO) {
        SearchResultDTO searchResultDTO = workspaceDAO.search(searchRequestDTO);
        searchResultDTO.setData(
                (List)searchResultDTO.getData().stream().filter(w->utilService.checkPermission(
                        PermissionDTO.builder()
                                .organizationIds(Collections.singletonList(((Workspace)w).getOrganization().getOrganizationId()))
                                .userId(((Workspace)w).getOwner().getUserId())
                                .build()
                        ,
                        "GET_WORKSPACE")).collect(Collectors.toList())
        );
        utilService.logEvents(null,log,"Searched Workspaces");
        return searchResultDTO;
    }

    public void importWorkspaces(MultipartFile multipartFile) throws Exception {
        ExcelUtil excelUtil = new ExcelUtil(multipartFile.getInputStream());
        Object[][] data = excelUtil.readSheet("Workspaces");
        for(int i=1;i< data.length;i++) {
            Workspace workspace = new Workspace();
            for(int j=0;j<data[i].length;j++) {
                Object obj = data[i][j];
                switch (j) {
                    case 0:
                        if(obj!=null) {
                            workspace = workspaceDAO.get((Long)obj);
                            if(workspace==null) {
                                throw new Exception("No workspace with this id found "+obj);
                            }
                        }
                        break;
                    case 1:
                        if(obj!=null) {
                            Organization organization = organizationDAO.get((String)obj);
                            if(organization==null)
                                throw new Exception("No organization with this name Found "+(String)obj);
                            workspace.setOrganization(organization);
                        }
                        break;
                    case 2:
                        if(obj!=null) {
                            User user = userDAO.getByUsername((String)obj);
                            if(user==null)
                                throw new Exception("No user with this name Found "+(String)obj);
                            workspace.setOwner(user);
                        }
                        break;
                    case 3:
                        if(obj!=null) {
                            workspace.setWorkspaceName((String)obj);
                        }
                        break;
                }
            }
            if(workspace.getOrganization()==null)
                throw new Exception("Username needed");
            if(workspace.getOwner()==null)
                throw new Exception("Organization name");
            if(workspace.getWorkspaceName()==null)
                throw new Exception("Workspace name");
            if(workspace.getWorkspaceId()==0L) {
                if(!utilService.checkPermission(
                        PermissionDTO.builder()
                                .organizationIds(Collections.singletonList(workspace.getOrganization().getOrganizationId()))
                                .userId(workspace.getOwner().getUserId())
                                .build()
                        , "CREATE_WORKSPACE"))
                    throw new Exception("CREATE WORKSPACE ACTION NOT PERMITTED FOR THIS USER");
            } else {
                if(!utilService.checkPermission(
                        PermissionDTO.builder()
                                .organizationIds(Collections.singletonList(workspace.getOrganization().getOrganizationId()))
                                .userId(workspace.getOwner().getUserId())
                                .build()
                        , "UPDATE_WORKSPACE"))
                throw new Exception("UPDATE WORKSPACE ACTION NOT PERMITTED FOR THIS USER FOR WORKSPACE ID "+workspace.getWorkspaceId());
            }

            workspaceDAO.save(workspace);
        }
        utilService.logEvents(null,log,"Imported Workspaces");
    }

    public void exportWorkspaces(HttpServletResponse response, List<Long> ids) throws Exception {
        Object[][] list = new Object[ids.size()+1][];
        list[0] = new Object[]{"WorkspaceId",
                "OrganizationName",
                "OwnerName",
                "WorkspaceName",
                "WorkspaceCreatedOn",
                "WorkspaceUpdatedOn"};
        for(int i=0;i<ids.size();i++) {
            Workspace workspace = workspaceDAO.get(ids.get(i));
            if(!utilService.checkPermission(
                    PermissionDTO.builder()
                            .organizationIds(Collections.singletonList(workspace.getOrganization().getOrganizationId()))
                            .userId(workspace.getOwner().getUserId())
                            .build()
                    , "GET_WORKSPACE"))
                throw new Exception("GET WORKSPACE ACTION NOT PERMITTED FOR THIS USER FOR WORKSPACE ID "+ids.get(i));
            list[i+1] = new Object[]{
                    workspace.getWorkspaceId(),
                    workspace.getOrganization().getOrganizationName(),
                    workspace.getOwner().getUserName(),
                    workspace.getWorkspaceName(),
                    workspace.getWorkspaceCreatedOn(),
                    workspace.getWorkspaceUpdatedOn()};
        }
        ExcelUtil excelUtil = new ExcelUtil();
        XSSFWorkbook xssfWorkbook = excelUtil.createSheet("Workspaces",list);
        response.setHeader("Content-Disposition", "attachment; filename=workspaces.xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        xssfWorkbook.write(outputStream);
        xssfWorkbook.close();
        outputStream.close();
        utilService.logEvents(null,log,"Exported Workspaces With Ids "+ids.toString());
    }
}
