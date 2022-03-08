package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.*;
import com.appmodz.executionmodule.dto.*;
import com.appmodz.executionmodule.model.*;
import com.appmodz.executionmodule.util.ExcelUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StackService {

    @Autowired
    StackDAO stackDAO;

    @Autowired
    UserDAO userDAO;

    @Autowired
    WorkspaceDAO workspaceDAO;

    @Autowired
    OrganizationDAO organizationDAO;

    @Autowired
    TerraformBackendDAO terraformBackendDAO;

    @Autowired
    UserOrganizationDAO userOrganizationDAO;

    @Autowired
    UtilService utilService;

    @Autowired
    PulumiService pulumiService;

    @Autowired
    TemplateDAO templateDAO;

    @Autowired
    ComponentDAO componentDAO;

    @Autowired
    ConfigurationProfileDAO configurationProfileDAO;

    @Autowired
    StackGroupDAO stackGroupDAO;

    @Autowired
    private Environment env;

    private Boolean PULUMI = true;

    public Stack getStackById(long stackId) throws Exception{
        Stack stack = stackDAO.get(stackId);
        if(stack==null)
            throw new Exception("No stack with this stack id exists");
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .workspaceId(stack.getWorkspace().getWorkspaceId())
                        .organizationIds(Collections.singletonList(stack.getWorkspace().getOrganization().getOrganizationId()))
                        .userId(stack.getWorkspace().getOwner().getUserId())
                        .build()
                , "GET_STACK"))
            throw new Exception("GET STACK ACTION NOT PERMITTED FOR THIS USER");
        utilService.logEvents(null,log,"Fetched Stack With Id "+ stack.getStackId());
        return stack;
    }

    public StackLogsResponseDTO getStackLogsById(long stackId) throws Exception{
        Stack stack = stackDAO.get(stackId);
        if(stack==null)
            throw new Exception("No stack with this stack id exists");
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .workspaceId(stack.getWorkspace().getWorkspaceId())
                        .organizationIds(Collections.singletonList(stack.getWorkspace().getOrganization().getOrganizationId()))
                        .userId(stack.getWorkspace().getOwner().getUserId())
                        .build()
                , "GET_STACK"))
            throw new Exception("GET STACK ACTION NOT PERMITTED FOR THIS USER");
        utilService.logEvents(null,log,"Fetched Stack With Id "+ stack.getStackId());
        StackLogsResponseDTO stackLogsResponseDTO = new StackLogsResponseDTO();
        stackLogsResponseDTO.setMessage(stack.getStackMessages());
        stackLogsResponseDTO.setState(stack.getStackButtonState());
        return stackLogsResponseDTO;
    }

    public Stack saveState(CanvasRequestDTO canvasRequestDTO) throws Exception{
        Stack stack = this.getStackById(canvasRequestDTO.getStackId());
        if(stack.getStackIsWizardStack()!=null&&stack.getStackIsWizardStack()) {
            stack.setStackWizardState(canvasRequestDTO.getDraftState());
        }
        stack.setStackDraftState(canvasRequestDTO.getDraftState());
        stackDAO.save(stack);
        return stack;
    }


    public Stack copyStack(StackRequestDTO stackRequestDTO) throws Exception {
        Stack copyStack = new Stack();
        Stack origStack = stackDAO.get(stackRequestDTO.getId());
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .workspaceId(origStack.getWorkspace().getWorkspaceId())
                        .organizationIds(Collections.singletonList(origStack.getWorkspace().getOrganization().getOrganizationId()))
                        .build()
                , "CREATE_STACK"))
            throw new Exception("CREATE STACK ACTION NOT PERMITTED FOR THIS USER");
        copyStack.setCloudPlatform(copyStack.getCloudPlatform());
        copyStack.setAwsRegion(stackRequestDTO.getAwsRegion());
        copyStack.setAwsSecretAccessKey(stackRequestDTO.getAwsSecretAccessKey());
        copyStack.setAwsAccessKey(stackRequestDTO.getAwsAccessKey());
        if(stackRequestDTO.getConfigurationProfileId()!=null)
            copyStack.setConfigurationProfile(configurationProfileDAO.get(
                    stackRequestDTO.getConfigurationProfileId()));
        copyStack.setStackState(origStack.getStackState());
        copyStack.setWorkspace(origStack.getWorkspace());
        copyStack.setOwner(origStack.getOwner());
        copyStack.setStackDraftState(origStack.getStackDraftState());
        copyStack.setStackState(origStack.getStackDraftState());
        TerraformBackend terraformBackend = new TerraformBackend();
        terraformBackend.setName(stackRequestDTO.getName());
        terraformBackendDAO.save(terraformBackend);
        copyStack.setTerraformBackend(terraformBackend);
        stackDAO.save(copyStack);
        if(PULUMI) {

            File workspace_folder = new File(env.getProperty("WORKING_DIR_2")+copyStack.getWorkspace().getWorkspaceId());
            if (!workspace_folder.exists()) {
                workspace_folder.mkdir();
            }

            File file = new File(env.getProperty("WORKING_DIR_2")+copyStack.getWorkspace().getWorkspaceId()+"/"+copyStack.getStackId());
            if (!file.exists()) {
                if (file.mkdir()) {
                    copyStack.setStackLocation(env.getProperty("WORKING_DIR_2")+
                            copyStack.getWorkspace().getWorkspaceId()+"/"+copyStack.getStackId());
                    stackDAO.save(copyStack);
                    utilService.logEvents(null,log,"Fetched Copied Stack With Id "+
                            stackRequestDTO.getId()+" To "+copyStack.getStackId());
                    return copyStack;
                } else {
                    throw new Exception("Error in folder creation");
                }
            } else {
                throw new Exception("Already Exists");
            }


        } else {
            File workspace_folder = new File(env.getProperty("WORKING_DIR_2")+copyStack.getWorkspace().getWorkspaceId());
            if (!workspace_folder.exists()) {
                workspace_folder.mkdir();
            }
            File file = new File(env.getProperty("WORKING_DIR_2")+copyStack.getWorkspace().getWorkspaceId()+"/"+copyStack.getStackId());
            if (!file.exists()) {
                if (file.mkdir()) {

                    File source = new File(env.getProperty("WORKING_DIR_2")+"basic_template");
                    File dest = new File(env.getProperty("WORKING_DIR_2")+
                            copyStack.getWorkspace().getWorkspaceId()+"/"+copyStack.getStackId());
                    try {
                        FileUtils.copyDirectory(source, dest);
                        copyStack.setStackLocation(env.getProperty("WORKING_DIR_2")+
                                copyStack.getWorkspace().getWorkspaceId()+"/"+copyStack.getStackId());
                        stackDAO.save(copyStack);
                        utilService.logEvents(null,log,"Fetched Copied Stack With Id "+
                                stackRequestDTO.getId()+" To "+copyStack.getStackId());
                        return copyStack;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("Error in folder creation");
                    }
                } else {
                    throw new Exception("Error in folder creation");
                }
            } else {
                throw new Exception("Already Exists");
            }
        }
    }



    public Stack createStack(StackRequestDTO stackRequestDTO) throws Exception{
        Stack stack = new Stack();
        User user = userDAO.get(stackRequestDTO.getOwnerId());
        if(user==null)
            throw new Exception("No user with this User Id Found "+stackRequestDTO.getOwnerId());
        Workspace workspace = workspaceDAO.get(stackRequestDTO.getWorkspaceId());
        if(workspace==null)
            throw new Exception("No workspace with this Workspace Id Found "+stackRequestDTO.getWorkspaceId());
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .workspaceId(stackRequestDTO.getWorkspaceId())
                        .organizationIds(Collections.singletonList(workspace.getOrganization().getOrganizationId()))
                        .userId(stackRequestDTO.getOwnerId())
                        .build()
                , "CREATE_STACK"))
            throw new Exception("CREATE STACK ACTION NOT PERMITTED FOR THIS USER");
        stack.setOwner(user);
        stack.setWorkspace(workspace);
        TerraformBackend terraformBackend = new TerraformBackend();
        terraformBackend.setName(stackRequestDTO.getName());
        terraformBackendDAO.save(terraformBackend);
        stack.setTerraformBackend(terraformBackend);
        if(stackRequestDTO.getCloudPlatformId()!=null)
        stack.setCloudPlatform(componentDAO.getCloudPlatform(stackRequestDTO.getCloudPlatformId()));
        stack.setAwsAccessKey(stackRequestDTO.getAwsAccessKey());
        stack.setAwsRegion(stackRequestDTO.getAwsRegion());
        stack.setAwsSecretAccessKey(stackRequestDTO.getAwsSecretAccessKey());
        if(stackRequestDTO.getConfigurationProfileId()!=null)
        stack.setConfigurationProfile(configurationProfileDAO.get(stackRequestDTO.getConfigurationProfileId()));
        if(stackRequestDTO.getStackGroupId()!=null)
            stack.setStackGroup(stackGroupDAO.get(stackRequestDTO.getStackGroupId()));
        if(stackRequestDTO.getTemplateId()!=null) {
            Template template = templateDAO.get(stackRequestDTO.getTemplateId());
            if (template == null)
                throw new Exception("Template with this id not found");
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode root = objectMapper.valueToTree(template.getState());
            root.set("message",objectMapper.convertValue(Collections.emptyList(), JsonNode.class));
            root.set("state",objectMapper.convertValue(Collections.emptyList(), JsonNode.class));
            stack.setStackDraftState(root);
            stack.setStackState(root);
        }

        if(stackRequestDTO.getIsWizard()!=null && stackRequestDTO.getIsWizard()) {
            stack.setStackIsWizardStack(true);
            stack.setStackWizardType(stackRequestDTO.getStackWizardType());
            stack.setStackWizardState(stackRequestDTO.getWizardState());
        }

        stackDAO.save(stack);

        if(PULUMI) {

            File workspace_folder = new File(env.getProperty("WORKING_DIR_2")+stack.getWorkspace().getWorkspaceId());
            if (!workspace_folder.exists()) {
                workspace_folder.mkdir();
            }

            File file = new File(env.getProperty("WORKING_DIR_2")+stack.getWorkspace().getWorkspaceId()+"/"+stack.getStackId());
            if (!file.exists()) {
                if (file.mkdir()) {
                    stack.setStackLocation(env.getProperty("WORKING_DIR_2")+
                            stack.getWorkspace().getWorkspaceId()+"/"+stack.getStackId());
                    stackDAO.save(stack);
                    utilService.logEvents(null,log,"Created Stack With Id "+
                            stackRequestDTO.getId());
                    return stack;
                } else {
                    throw new Exception("Error in folder creation");
                }
            } else {
                throw new Exception("Already Exists");
            }


        } else {
            File workspace_folder = new File(env.getProperty("WORKING_DIR_2")+stack.getWorkspace().getWorkspaceId());
            if (!workspace_folder.exists()) {
                workspace_folder.mkdir();
            }
            File file = new File(env.getProperty("WORKING_DIR_2")+stack.getWorkspace().getWorkspaceId()+"/"+stack.getStackId());
            if (!file.exists()) {
                if (file.mkdir()) {

                    File source = new File(env.getProperty("WORKING_DIR_2")+"basic_template");
                    File dest = new File(env.getProperty("WORKING_DIR_2")+
                            stack.getWorkspace().getWorkspaceId()+"/"+stack.getStackId());
                    try {
                        FileUtils.copyDirectory(source, dest);
                        stack.setStackLocation(env.getProperty("WORKING_DIR_2")+
                                stack.getWorkspace().getWorkspaceId()+"/"+stack.getStackId());
                        stackDAO.save(stack);
                        utilService.logEvents(null,log,"Created Stack With Id "+
                                stackRequestDTO.getId());
                        return stack;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("Error in folder creation");
                    }
                } else {
                    throw new Exception("Error in folder creation");
                }
            } else {
                throw new Exception("Already Exists");
            }
        }
    }

    public Stack editStack(StackRequestDTO stackRequestDTO) throws Exception{
        Stack stack = stackDAO.get(stackRequestDTO.getId());
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .workspaceId(stack.getWorkspace().getWorkspaceId())
                        .organizationIds(Collections.singletonList(stack.getWorkspace().getOrganization().getOrganizationId()))
                        .userId(stack.getOwner().getUserId())
                        .build()
                , "UPDATE_STACK"))
            throw new Exception("UPDATE STACK ACTION NOT PERMITTED FOR THIS USER");
        if(stackRequestDTO.getOwnerId()!=null&&stackRequestDTO.getWorkspaceId()!=null) {
            User user = userDAO.get(stackRequestDTO.getOwnerId());
            Workspace workspace = workspaceDAO.get(stackRequestDTO.getWorkspaceId());
            stack.setWorkspace(workspace);
            stack.setOwner(user);
        }
        else if(stackRequestDTO.getOwnerId()!=null) {
            User user = userDAO.get(stackRequestDTO.getOwnerId());
            if(user==null)
                throw new Exception("No user with this User Id Found "+stackRequestDTO.getOwnerId());
            stack.setOwner(user);
        }
        else if(stackRequestDTO.getWorkspaceId()!=null) {
            Workspace workspace = workspaceDAO.get(stackRequestDTO.getWorkspaceId());
            if(workspace==null)
                throw new Exception("No workspace with this Workspace Id Found "+stackRequestDTO.getWorkspaceId());
            stack.setWorkspace(workspace);
        }
        if (stackRequestDTO.getTerraformBackendId()!=null){
            TerraformBackend terraformBackend = terraformBackendDAO.get(stackRequestDTO.getTerraformBackendId());
            if(stackRequestDTO.getName()!=null) {
                terraformBackend.setName(stackRequestDTO.getName());
                terraformBackendDAO.save(terraformBackend);
            }

            stack.setTerraformBackend(terraformBackend);
        }
        if(stackRequestDTO.getTemplateId()!=null) {
            Template template = templateDAO.get(stackRequestDTO.getTemplateId());
            if (template == null)
                throw new Exception("Template with this id not found");
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode root = objectMapper.valueToTree(template.getState());

            root.set("message",objectMapper.convertValue(Collections.emptyList(), JsonNode.class));
            root.set("state",objectMapper.convertValue(Collections.emptyList(), JsonNode.class));
            stack.setStackDraftState(root);
            stack.setStackState(root);
        }
        if(stackRequestDTO.getConfigurationProfileId()!=null)
            stack.setConfigurationProfile(
                    configurationProfileDAO.get(stackRequestDTO.getConfigurationProfileId()));
        if(stackRequestDTO.getStackGroupId()!=null)
            stack.setStackGroup(stackGroupDAO.get(stackRequestDTO.getStackGroupId()));
        if(stackRequestDTO.getAwsAccessKey()!=null)
            stack.setAwsAccessKey(stackRequestDTO.getAwsAccessKey());
        if(stackRequestDTO.getAwsRegion()!=null)
            stack.setAwsRegion(stackRequestDTO.getAwsRegion());
        if(stackRequestDTO.getAwsSecretAccessKey()!=null)
            stack.setAwsSecretAccessKey(stackRequestDTO.getAwsSecretAccessKey());
        if(stackRequestDTO.getIsWizard()!=null && stackRequestDTO.getIsWizard()) {
            stack.setStackIsWizardStack(true);
            stack.setStackWizardType(stackRequestDTO.getStackWizardType());
            stack.setStackWizardState(stackRequestDTO.getWizardState());
        }
        if(stackRequestDTO.getCloudPlatformId()!=null) {
            stack.setCloudPlatform(componentDAO.getCloudPlatform(stackRequestDTO.getCloudPlatformId()));
        }
        stackDAO.save(stack);
        return stack;
    }


    public void deleteStack(Long id) throws Exception{
        Stack stack = stackDAO.get(id);
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .workspaceId(stack.getWorkspace().getWorkspaceId())
                        .organizationIds(Collections.singletonList(stack.getWorkspace().getOrganization().getOrganizationId()))
                        .userId(stack.getOwner().getUserId())
                        .build()
                , "DELETE_STACK"))
            throw new Exception("DELETE STACK ACTION NOT PERMITTED FOR THIS USER");
        pulumiService.pulumiDestroy(stack);
        utilService.logEvents(null,log,"Deleted Stack With Id "+
                id);
    }

    public String deleteMultipleStacks(StackRequestDTO stackRequestDTO) throws Exception{
        List<Long> ids = stackRequestDTO.getIds();
        StringBuilder exceptions = new StringBuilder();
        StringBuilder successes = new StringBuilder();
        for (long id: ids) {
            Stack stack = stackDAO.get(id);
            if(!utilService.checkPermission(
                    PermissionDTO.builder()
                            .workspaceId(stack.getWorkspace().getWorkspaceId())
                            .organizationIds(Collections.singletonList(stack.getWorkspace().getOrganization().getOrganizationId()))
                            .userId(stack.getOwner().getUserId())
                            .build()
                    , "DELETE_STACK"))
                exceptions.append("DELETE STACK ACTION NOT PERMITTED FOR THIS USER FOR ID ").append(id);
            try {
                pulumiService.pulumiDestroy(stack);
//                utilService.logEvents(null,log,"Deleted Stack With Id "+
//                        id);
                successes.append("Successfully Deleted ").append(id).append("\n");
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                exceptions.append("Unable To Delete id ").append(id).append(" Due To Possible Foreign Key associations").append("\n");
            }
        }

        if(exceptions.toString().length()>0)
            throw new Exception(exceptions.toString());

        return successes.toString();
    }

    public List listStacks() {
        List<Stack> stacks = stackDAO.getAll();
        stacks =  stacks.stream().filter(s->utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(Collections.singletonList(s.getWorkspace().getOrganization().getOrganizationId()))
                        .workspaceId(s.getWorkspace().getWorkspaceId())
                        .userId(s.getOwner().getUserId())
                        .build(),
                "GET_STACK")).collect(Collectors.toList());
        utilService.logEvents(null,log,"Listed Stacks");
        return stacks;
    }

    public List listStacksByWorkspaceId(long workspaceId) {
        List<Stack> stacks = stackDAO.getByWorkspaceId(workspaceId);
        stacks =  stacks.stream().filter(s->utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(Collections.singletonList(s.getWorkspace().getOrganization().getOrganizationId()))
                        .workspaceId(s.getWorkspace().getWorkspaceId())
                        .userId(s.getOwner().getUserId())
                        .build(),
                "GET_STACK")).collect(Collectors.toList());
        utilService.logEvents(null,log,"Listed Stacks Of Workspace With Id "+
                workspaceId);
        return stacks;
    }

    public SearchResultDTO searchStacks(SearchRequestDTO searchRequestDTO) {
        SearchResultDTO searchResultDTO = stackDAO.search(searchRequestDTO);
        searchResultDTO.setData(
                (List)searchResultDTO.getData().stream().filter(s->utilService.checkPermission(
                        PermissionDTO.builder()
                                .organizationIds(Collections.singletonList(((Stack)s).getWorkspace().getOrganization().getOrganizationId()))
                                .workspaceId(((Stack)s).getWorkspace().getWorkspaceId())
                                .userId(((Stack)s).getOwner().getUserId())
                                .build()
                        ,
                        "GET_STACK")).collect(Collectors.toList())
        );
        utilService.logEvents(null,log,"Searched Stacks");
        return searchResultDTO;
    }

    private Workspace getWorkspaceFromNameAllowedToUser(String workspaceName) throws Exception {
        List<Workspace> workspaces = workspaceDAO.getWorkspacesByName(workspaceName);
        if(workspaces.size()==0)
            return null;
        if(workspaces.size()==1)
            return workspaces.get(0);

        UserOrganization userOrganization = utilService.getUserOrganization();

        for(Workspace workspace: workspaces) {
            if(workspace.getOrganization().getOrganizationId()==userOrganization.getOrganization().getOrganizationId())
                return workspace;
        }

        return workspaces.get(0);
    }

    public void importStacks(MultipartFile multipartFile) throws Exception {
        ExcelUtil excelUtil = new ExcelUtil(multipartFile.getInputStream());
        Object[][] data = excelUtil.readSheet("Stacks");
        for(int i=1;i< data.length;i++) {
            Stack stack = new Stack();
            for(int j=0;j<data[i].length;j++) {
                Object obj = data[i][j];
                switch (j) {
                    case 0:
                        if(obj!=null) {
                            stack = stackDAO.get((Long)obj);
                            if(stack==null) {
                                throw new Exception("No stack with this id found "+obj);
                            }
                        }
                        break;
                    case 1:
                        if(obj!=null) {
                            Workspace workspace = this.getWorkspaceFromNameAllowedToUser((String)obj);
                            if(workspace==null)
                                throw new Exception("Workspace with this name not found "+(String)obj);
                            stack.setWorkspace(workspace);
                        }
                        break;
                    case 2:
                        if(obj!=null) {
                            User user = userDAO.getByUsername((String)obj);
                            if(user==null)
                                throw new Exception("User with this username not found "+(String)obj);
                            stack.setOwner(user);
                        }
                        break;
                    case 3:
                        if(obj!=null) {
                            TerraformBackend terraformBackend = new TerraformBackend();
                            terraformBackend.setName((String)obj);
                            terraformBackendDAO.save(terraformBackend);
                            stack.setTerraformBackend(terraformBackend);
                        }
                        break;
                    case 4:
                        if(obj!=null) {
                            stack.setAwsRegion((String)obj);
                        }
                        break;
                    case 5:
                        if(obj!=null) {
                            stack.setAwsAccessKey((String)obj);
                        }
                        break;
                    case 6:
                        if(obj!=null) {
                            stack.setAwsSecretAccessKey((String)obj);
                        }
                        break;
                }
            }
            if(stack.getWorkspace()==null)
                throw new Exception("Workspace Name not present");
            if(stack.getOwner()==null)
                throw new Exception("Owner Name not present");
            if(stack.getTerraformBackend()==null)
                throw new Exception("Stack Name not present");
            if(stack.getStackId()==0L) {
                if(!utilService.checkPermission(
                        PermissionDTO.builder()
                                .workspaceId(stack.getWorkspace().getWorkspaceId())
                                .organizationIds(Collections.singletonList(stack.getWorkspace().getOrganization().getOrganizationId()))
                                .userId(stack.getWorkspace().getOwner().getUserId())
                                .build()
                        , "CREATE_STACK"))
                    throw new Exception("CREATE STACK ACTION NOT PERMITTED FOR THIS USER");
            } else {
                if(!utilService.checkPermission(
                        PermissionDTO.builder()
                                .workspaceId(stack.getWorkspace().getWorkspaceId())
                                .organizationIds(Collections.singletonList(stack.getWorkspace().getOrganization().getOrganizationId()))
                                .userId(stack.getWorkspace().getOwner().getUserId())
                                .build()
                        , "UPDATE_STACK"))
                    throw new Exception("UPDATE STACK ACTION NOT PERMITTED FOR THIS USER FOR STACK ID "+stack.getStackId());
            }
            stackDAO.save(stack);
        }
        utilService.logEvents(null,log,"Imported Stacks");
    }

    public void exportStacks(HttpServletResponse response, List<Long> ids) throws Exception {
        Object[][] list = new Object[ids.size()+1][];
        list[0] = new Object[]{"StackId",
                "WorkspaceName","OwnerName","StackName",
                "AwsRegion",
                "AwsAccessKey",
                "AwsSecretAccessKey",
                "StackCreatedOn"
                ,"StackUpdatedOn"};
        for(int i=0;i<ids.size();i++) {
            Stack stack = stackDAO.get(ids.get(i));
            if(!utilService.checkPermission(
                    PermissionDTO.builder()
                            .workspaceId(stack.getWorkspace().getWorkspaceId())
                            .organizationIds(Collections.singletonList(stack.getWorkspace().getOrganization().getOrganizationId()))
                            .userId(stack.getWorkspace().getOwner().getUserId())
                            .build()
                    , "GET_STACK"))
                throw new Exception("GET STACK ACTION NOT PERMITTED FOR THIS USER FOR STACK ID "+stack.getStackId());
            list[i+1] = new Object[]{stack.getStackId(),
                    stack.getWorkspace().getWorkspaceName(),
                    stack.getOwner().getUserName(),
                    stack.getTerraformBackend().getName()
                    ,stack.getAwsRegion(),
                    stack.getAwsAccessKey(),
                    stack.getAwsSecretAccessKey(),
                    stack.getStackCreatedOn(),
                    stack.getStackUpdatedOn()
            };
        }
        ExcelUtil excelUtil = new ExcelUtil();
        XSSFWorkbook xssfWorkbook = excelUtil.createSheet("Stacks",list);
        response.setHeader("Content-Disposition", "attachment; filename=stacks.xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        xssfWorkbook.write(outputStream);
        xssfWorkbook.close();
        outputStream.close();
        utilService.logEvents(null,log,"Exported Stacks With Ids "+ids.toString());
    }

}
