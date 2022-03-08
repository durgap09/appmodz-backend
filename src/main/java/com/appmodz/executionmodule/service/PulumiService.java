package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.ApplicationConfigurationDAO;
import com.appmodz.executionmodule.dao.StackDAO;
import com.appmodz.executionmodule.dto.*;
import com.appmodz.executionmodule.model.*;
import com.appmodz.executionmodule.model.Stack;
import com.appmodz.executionmodule.util.Processes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PulumiService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private StackDAO stackDAO;

    @Autowired
    UtilService utilService;

    @Autowired
    ApplicationConfigurationDAO applicationConfigurationDAO;

    @Autowired
    AsyncService asyncService;

    @Transactional
    public void workspaceInit(Stack stack) throws Exception{
        File file = new File(env.getProperty("WORKING_DIR")+stack.getWorkspace().getWorkspaceId()+"/"+stack.getStackId());
        FileUtils.cleanDirectory(file);
        File source = new File(env.getProperty("WORKING_DIR")+"pulumi_stack_package.json");
        File dest = new File(env.getProperty("WORKING_DIR")+
                stack.getWorkspace().getWorkspaceId()+"/"+stack.getStackId());
        StackConfigDTO stackConfigDTO = new StackConfigDTO();
        stackConfigDTO.setStackName(stack.getTerraformBackend().getName());
        stackConfigDTO.setProjectName(stack.getWorkspace().getWorkspaceName());
        stackConfigDTO.setAwsRegion(stack.getAwsRegion());
        stackConfigDTO.setAwsAccessKey(stack.getAwsAccessKey());
        stackConfigDTO.setAwsSecretKey(stack.getAwsSecretAccessKey());
        if(stack.getConfigurationProfile()!=null)
        stackConfigDTO.setStackConfig(stack.getConfigurationProfile().getConfig());
        stackConfigDTO.setCloudPlatform(stack.getCloudPlatform());
        ObjectMapper mapper = new ObjectMapper();

        mapper.writeValue(new File(env.getProperty("WORKING_DIR")+stack.getWorkspace().getWorkspaceId()+"/"+stack.getStackId()+"/config.json"),
                stackConfigDTO);
    }

    @Transactional
    private StackConfigDTO createStackConfig(Stack stack) {
        StackConfigDTO stackConfigDTO = new StackConfigDTO();
        stackConfigDTO.setStackName(stack.getTerraformBackend().getName());
        stackConfigDTO.setProjectName(stack.getWorkspace().getWorkspaceName());
        stackConfigDTO.setAwsRegion(stack.getAwsRegion());
        stackConfigDTO.setAwsAccessKey(stack.getAwsAccessKey());
        stackConfigDTO.setAwsSecretKey(stack.getAwsSecretAccessKey());
        if(stack.getConfigurationProfile()!=null)
            stackConfigDTO.setStackConfig(stack.getConfigurationProfile().getConfig());
        stackConfigDTO.setCloudPlatform(stack.getCloudPlatform());
        return stackConfigDTO;
    }

    @Transactional
    public Object pulumiStatewiseMovement(Stack stack) throws Exception {
        //this.workspaceInit(stack);
        PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
        pulumiRequestDTO.setDraftState(stack.getStackDraftState());
        pulumiRequestDTO.setStackPath(stack.getStackLocation());
        pulumiRequestDTO.setConfig(this.createStackConfig(stack));
        if(stack.getStackIsWizardStack()!=null&&stack.getStackIsWizardStack()) {
            pulumiRequestDTO.setStackWizardType(stack.getStackWizardType());
            pulumiRequestDTO.setDraftState(stack.getStackWizardState());
        }
        String reqUrl = env.getProperty("PULUMI_BASE_URL")+"/pulumi-save";

        System.out.println(reqUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        System.out.println(pulumiRequestDTO.toString());

        HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
        String result = restTemplate.postForObject(reqUrl, entity, String.class);
        return this.pulumiInit(stack);
    };

    @Transactional
    private Object pulumiInit(Stack stack) throws Exception{
        List<StackButtonStateDTO> stackButtonStateDTOS = stack.getStackButtonState();
        List<StackMessageDTO> stackMessageDTOS = stack.getStackMessages();
        if(stackButtonStateDTOS==null)
            stackButtonStateDTOS = new ArrayList<>();
        if(stackMessageDTOS==null)
            stackMessageDTOS = new ArrayList<>();
        StackMessageDTO stackMessageDTO = new StackMessageDTO();
        StackButtonStateDTO stackButtonStateDTO = new StackButtonStateDTO();
        try {
            stackButtonStateDTO.setName("Save");
            stackButtonStateDTO.setAction("warning");
            stackButtonStateDTOS.add(0,stackButtonStateDTO);
            stack.setStackButtonState(stackButtonStateDTOS);
            stackDAO.save(stack);
            PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
            pulumiRequestDTO.setDraftState(stack.getStackDraftState());
            pulumiRequestDTO.setStackPath(stack.getStackLocation());
            pulumiRequestDTO.setConfig(this.createStackConfig(stack));
            if(stack.getStackIsWizardStack()!=null&&stack.getStackIsWizardStack()) {
                pulumiRequestDTO.setStackWizardType(stack.getStackWizardType());
                pulumiRequestDTO.setDraftState(stack.getStackWizardState());
            }
            String reqUrl = env.getProperty("PULUMI_BASE_URL") + "/pulumi-init";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
            String result = restTemplate.postForObject(reqUrl, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            ResponseDTO responseDTO = mapper.readValue(result, new TypeReference<ResponseDTO>() {
            });
            if (responseDTO.getStatus().equals("success")) {
                stackMessageDTO.setType("success");
                stackMessageDTO.setText("Save Executed");
                stackMessageDTO.setTime(new Date());
                stackMessageDTO.setContent(responseDTO.getData());
                stackButtonStateDTO= new StackButtonStateDTO();
                stackButtonStateDTO.setName("Save");
                stackButtonStateDTO.setAction("success");
                if(stack.getStackButtonState()!=null)
                stackButtonStateDTOS = stack.getStackButtonState();
                if(stack.getStackMessages()!=null)
                stackMessageDTOS = stack.getStackMessages();
                stackMessageDTOS.add(0,stackMessageDTO);
                stackButtonStateDTOS.add(0,stackButtonStateDTO);
                stack.setStackButtonState(stackButtonStateDTOS);
                stack.setStackMessages(stackMessageDTOS);
                stackDAO.save(stack);
                utilService.logEvents(null, log, responseDTO.getData().toString(),"debug");
            } else {
                stackMessageDTO.setType("error");
                stackMessageDTO.setText("Error Occurred while Saving");
                stackMessageDTO.setTime(new Date());
                stackMessageDTO.setContent(responseDTO.getData());
                stackButtonStateDTO= new StackButtonStateDTO();
                stackButtonStateDTO.setName("Save");
                stackButtonStateDTO.setAction("error");
                if(stack.getStackButtonState()!=null)
                    stackButtonStateDTOS = stack.getStackButtonState();
                if(stack.getStackMessages()!=null)
                    stackMessageDTOS = stack.getStackMessages();
                stackMessageDTOS.add(0,stackMessageDTO);
                stackButtonStateDTOS.add(0,stackButtonStateDTO);
                stack.setStackButtonState(stackButtonStateDTOS);
                stack.setStackMessages(stackMessageDTOS);
                stackDAO.save(stack);
                utilService.logEvents(null, log, responseDTO.getData().toString(),"debug");
            }

            utilService.logEvents(null, log, "Initiated Pulumi Stack With Id " + stack.getStackId());
            return result;
        } catch (Exception e) {
            stackMessageDTO.setType("error");
            stackMessageDTO.setText("Error Occurred while Saving");
            stackMessageDTO.setTime(new Date());
            stackMessageDTO.setContent(e.getMessage());
            stackButtonStateDTO= new StackButtonStateDTO();
            stackButtonStateDTO.setName("Save");
            stackButtonStateDTO.setAction("error");
            if(stack.getStackButtonState()!=null)
                stackButtonStateDTOS = stack.getStackButtonState();
            if(stack.getStackMessages()!=null)
                stackMessageDTOS = stack.getStackMessages();
            stackMessageDTOS.add(0,stackMessageDTO);
            stackButtonStateDTOS.add(0,stackButtonStateDTO);
            stack.setStackButtonState(stackButtonStateDTOS);
            stack.setStackMessages(stackMessageDTOS);
            stackDAO.save(stack);
            utilService.logEvents(null, log, e.getMessage(),"error");
            throw e;
        }
    }

    @Transactional
    public Object pulumiValidate(Stack stack) throws Exception{
        List<StackButtonStateDTO> stackButtonStateDTOS = stack.getStackButtonState();
        List<StackMessageDTO> stackMessageDTOS = stack.getStackMessages();
        if(stackButtonStateDTOS==null)
            stackButtonStateDTOS = new ArrayList<>();
        if(stackMessageDTOS==null)
            stackMessageDTOS = new ArrayList<>();
        StackMessageDTO stackMessageDTO = new StackMessageDTO();
        StackButtonStateDTO stackButtonStateDTO = new StackButtonStateDTO();
        try {
            stackButtonStateDTO.setName("Validate");
            stackButtonStateDTO.setAction("warning");
            stackButtonStateDTOS.add(0,stackButtonStateDTO);
            stack.setStackButtonState(stackButtonStateDTOS);
            stackDAO.save(stack);
            PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
            pulumiRequestDTO.setDraftState(stack.getStackDraftState());
            pulumiRequestDTO.setStackPath(stack.getStackLocation());
            pulumiRequestDTO.setConfig(this.createStackConfig(stack));
            if(stack.getStackIsWizardStack()!=null&&stack.getStackIsWizardStack()) {
                pulumiRequestDTO.setStackWizardType(stack.getStackWizardType());
                pulumiRequestDTO.setDraftState(stack.getStackWizardState());
            }
            String reqUrl = env.getProperty("PULUMI_BASE_URL") + "/pulumi-validate";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
            String result = restTemplate.postForObject(reqUrl, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            ResponseDTO responseDTO = mapper.readValue(result, new TypeReference<ResponseDTO>() {
            });
            if (responseDTO.getStatus().equals("success")) {
                stackMessageDTO.setType("success");
                stackMessageDTO.setText("Validate Executed");
                stackMessageDTO.setTime(new Date());
                stackMessageDTO.setContent(responseDTO.getData());
                stackButtonStateDTO= new StackButtonStateDTO();
                stackButtonStateDTO.setName("Validate");
                stackButtonStateDTO.setAction("success");
                if(stack.getStackButtonState()!=null)
                    stackButtonStateDTOS = stack.getStackButtonState();
                if(stack.getStackMessages()!=null)
                    stackMessageDTOS = stack.getStackMessages();
                stackMessageDTOS.add(0,stackMessageDTO);
                stackButtonStateDTOS.add(0,stackButtonStateDTO);
                stack.setStackButtonState(stackButtonStateDTOS);
                stack.setStackMessages(stackMessageDTOS);
                stackDAO.save(stack);
                utilService.logEvents(null, log, responseDTO.getData().toString(),"debug");
            } else {
                stackMessageDTO.setType("error");
                stackMessageDTO.setText("Error Occurred while Validating");
                stackMessageDTO.setTime(new Date());
                stackMessageDTO.setContent(responseDTO.getData());
                stackButtonStateDTO= new StackButtonStateDTO();
                stackButtonStateDTO.setName("Validate");
                stackButtonStateDTO.setAction("error");
                if(stack.getStackButtonState()!=null)
                    stackButtonStateDTOS = stack.getStackButtonState();
                if(stack.getStackMessages()!=null)
                    stackMessageDTOS = stack.getStackMessages();
                stackMessageDTOS.add(0,stackMessageDTO);
                stackButtonStateDTOS.add(0,stackButtonStateDTO);
                stack.setStackButtonState(stackButtonStateDTOS);
                stack.setStackMessages(stackMessageDTOS);
                stackDAO.save(stack);
                utilService.logEvents(null, log, responseDTO.getData().toString(),"debug");
            }
            utilService.logEvents(null, log, "Validated Pulumi Stack With Id " + stack.getStackId());
            return result;
        } catch (Exception e) {
            stackMessageDTO.setType("error");
            stackMessageDTO.setText("Error Occurred while Validating");
            stackMessageDTO.setTime(new Date());
            stackMessageDTO.setContent(e.getMessage());
            stackButtonStateDTO= new StackButtonStateDTO();
            stackButtonStateDTO.setName("Save");
            stackButtonStateDTO.setAction("error");
            if(stack.getStackButtonState()!=null)
                stackButtonStateDTOS = stack.getStackButtonState();
            if(stack.getStackMessages()!=null)
                stackMessageDTOS = stack.getStackMessages();
            stackMessageDTOS.add(0,stackMessageDTO);
            stackButtonStateDTOS.add(0,stackButtonStateDTO);
            stack.setStackButtonState(stackButtonStateDTOS);
            stack.setStackMessages(stackMessageDTOS);
            stackDAO.save(stack);
            utilService.logEvents(null, log, e.getMessage(),"error");
            throw e;
        }
    }

    @Transactional
    public Object pulumiWhoAmI() throws Exception{
        PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
        String reqUrl = env.getProperty("PULUMI_BASE_URL")+"/pulumi-whoami";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
        String result = restTemplate.postForObject(reqUrl, entity, String.class);
        //utilService.logEvents(null,log,"Validated Pulumi Stack With Id "+stack.getStackId());
        return result;
    }

    @Transactional
    public Object pulumiSetKey(String token) throws Exception{
        String prevToken = null;
        ApplicationConfiguration applicationConfiguration = applicationConfigurationDAO.getByKey("PULUMI_ACCESS_TOKEN");
        if(applicationConfiguration!=null)
            prevToken = applicationConfiguration.getValue();
        PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
        pulumiRequestDTO.setToken(token);
        pulumiRequestDTO.setPrev_token(prevToken);
        Stack stack = stackDAO.get();
        if(stack == null)
            pulumiRequestDTO.setIsStackListEmpty(true);
        else
            pulumiRequestDTO.setIsStackListEmpty(false);
        String reqUrl = env.getProperty("PULUMI_BASE_URL")+"/pulumi-set-token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
        String result = restTemplate.postForObject(reqUrl, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        ResponseDTO responseDTO = mapper.readValue(result, new TypeReference<ResponseDTO>(){});
        if(responseDTO.getStatus().equals("success")) {
           if(applicationConfiguration!=null) {
               applicationConfiguration.setValue(token);
               applicationConfigurationDAO.save(applicationConfiguration);
           } else {
               applicationConfiguration = new ApplicationConfiguration();
               applicationConfiguration.setKey("PULUMI_ACCESS_TOKEN");
               applicationConfiguration.setDatatype("string");
               applicationConfiguration.setValue(token);
               applicationConfigurationDAO.save(applicationConfiguration);
           }
        }
        //utilService.logEvents(null,log,"Changed Pulumi Tok"+stack.getStackId());
        return result;
    }

    @Transactional
    public Object pulumiPreview(Stack stack) throws Exception{
        List<StackButtonStateDTO> stackButtonStateDTOS = stack.getStackButtonState();
        List<StackMessageDTO> stackMessageDTOS = stack.getStackMessages();
        if(stackButtonStateDTOS==null)
            stackButtonStateDTOS = new ArrayList<>();
        if(stackMessageDTOS==null)
            stackMessageDTOS = new ArrayList<>();
        StackMessageDTO stackMessageDTO = new StackMessageDTO();
        StackButtonStateDTO stackButtonStateDTO = new StackButtonStateDTO();
        try {
            stackButtonStateDTO.setName("Plan");
            stackButtonStateDTO.setAction("warning");
            stackButtonStateDTOS.add(0,stackButtonStateDTO);
            stack.setStackButtonState(stackButtonStateDTOS);
            stackDAO.save(stack);
            PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
            pulumiRequestDTO.setDraftState(stack.getStackDraftState());
            pulumiRequestDTO.setStackPath(stack.getStackLocation());
            pulumiRequestDTO.setConfig(this.createStackConfig(stack));
            if(stack.getStackIsWizardStack()!=null&&stack.getStackIsWizardStack()) {
                pulumiRequestDTO.setStackWizardType(stack.getStackWizardType());
                pulumiRequestDTO.setDraftState(stack.getStackWizardState());
            }
            String reqUrl = env.getProperty("PULUMI_BASE_URL") + "/pulumi-preview";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
            String result = restTemplate.postForObject(reqUrl, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            ResponseDTO responseDTO = mapper.readValue(result, new TypeReference<ResponseDTO>() {
            });
            if (responseDTO.getStatus().equals("success")) {
                stackMessageDTO.setType("success");
                stackMessageDTO.setText("Plan Executed");
                stackMessageDTO.setTime(new Date());
                stackMessageDTO.setContent(responseDTO.getData());
                stackButtonStateDTO= new StackButtonStateDTO();
                stackButtonStateDTO.setName("Plan");
                stackButtonStateDTO.setAction("success");
                if(stack.getStackButtonState()!=null)
                    stackButtonStateDTOS = stack.getStackButtonState();
                if(stack.getStackMessages()!=null)
                    stackMessageDTOS = stack.getStackMessages();
                stackMessageDTOS.add(0,stackMessageDTO);
                stackButtonStateDTOS.add(0,stackButtonStateDTO);
                stack.setStackButtonState(stackButtonStateDTOS);
                stack.setStackMessages(stackMessageDTOS);
                stackDAO.save(stack);
                utilService.logEvents(null, log, responseDTO.getData().toString(),"debug");
            } else {
                stackMessageDTO.setType("error");
                stackMessageDTO.setText("Error Occurred while Planning");
                stackMessageDTO.setTime(new Date());
                stackMessageDTO.setContent(responseDTO.getData());
                stackButtonStateDTO= new StackButtonStateDTO();
                stackButtonStateDTO.setName("Plan");
                stackButtonStateDTO.setAction("error");
                if(stack.getStackButtonState()!=null)
                    stackButtonStateDTOS = stack.getStackButtonState();
                if(stack.getStackMessages()!=null)
                    stackMessageDTOS = stack.getStackMessages();
                stackMessageDTOS.add(0,stackMessageDTO);
                stackButtonStateDTOS.add(0,stackButtonStateDTO);
                stack.setStackButtonState(stackButtonStateDTOS);
                stack.setStackMessages(stackMessageDTOS);
                stackDAO.save(stack);
                utilService.logEvents(null, log, responseDTO.getData().toString(),"debug");
            }
            utilService.logEvents(null, log, "Previewed Pulumi Stack With Id " + stack.getStackId());
            return result;
        } catch (Exception e) {
            stackMessageDTO.setType("error");
            stackMessageDTO.setText("Error Occurred while Planning");
            stackMessageDTO.setTime(new Date());
            stackMessageDTO.setContent(e.getMessage());
            stackButtonStateDTO= new StackButtonStateDTO();
            stackButtonStateDTO.setName("Plan");
            stackButtonStateDTO.setAction("error");
            if(stack.getStackButtonState()!=null)
                stackButtonStateDTOS = stack.getStackButtonState();
            if(stack.getStackMessages()!=null)
                stackMessageDTOS = stack.getStackMessages();
            stackMessageDTOS.add(0,stackMessageDTO);
            stackButtonStateDTOS.add(0,stackButtonStateDTO);
            stack.setStackButtonState(stackButtonStateDTOS);
            stack.setStackMessages(stackMessageDTOS);
            stackDAO.save(stack);
            utilService.logEvents(null, log, e.getMessage(),"error");
            throw e;
        }
    }

    @Transactional
    public Object pulumiUp(Stack stack) throws Exception{
        List<StackButtonStateDTO> stackButtonStateDTOS = stack.getStackButtonState();
        List<StackMessageDTO> stackMessageDTOS = stack.getStackMessages();
        if(stackButtonStateDTOS==null)
            stackButtonStateDTOS = new ArrayList<>();
        if(stackMessageDTOS==null)
            stackMessageDTOS = new ArrayList<>();
        StackMessageDTO stackMessageDTO = new StackMessageDTO();
        StackButtonStateDTO stackButtonStateDTO = new StackButtonStateDTO();
        try {
            stackButtonStateDTO.setName("Apply");
            stackButtonStateDTO.setAction("warning");
            stackButtonStateDTOS.add(0,stackButtonStateDTO);
            stack.setStackButtonState(stackButtonStateDTOS);
            stackDAO.save(stack);
            PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
            pulumiRequestDTO.setDraftState(stack.getStackDraftState());
            pulumiRequestDTO.setStackPath(stack.getStackLocation());
            pulumiRequestDTO.setConfig(this.createStackConfig(stack));
            if(stack.getStackIsWizardStack()!=null&&stack.getStackIsWizardStack()) {
                pulumiRequestDTO.setStackWizardType(stack.getStackWizardType());
                pulumiRequestDTO.setDraftState(stack.getStackWizardState());
            }
            String reqUrl = env.getProperty("PULUMI_BASE_URL") + "/pulumi-up";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
            String result = restTemplate.postForObject(reqUrl, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            ResponseDTO responseDTO = mapper.readValue(result, new TypeReference<ResponseDTO>() {
            });
            if (responseDTO.getStatus().equals("success")) {
                stackMessageDTO.setType("success");
                stackMessageDTO.setText("Apply Executed");
                stackMessageDTO.setTime(new Date());
                stackMessageDTO.setContent(responseDTO.getData());
                stackButtonStateDTO= new StackButtonStateDTO();
                stackButtonStateDTO.setName("Apply");
                stackButtonStateDTO.setAction("success");
                if(stack.getStackButtonState()!=null)
                    stackButtonStateDTOS = stack.getStackButtonState();
                if(stack.getStackMessages()!=null)
                    stackMessageDTOS = stack.getStackMessages();
                stackMessageDTOS.add(0,stackMessageDTO);
                stackButtonStateDTOS.add(0,stackButtonStateDTO);
                stack.setStackButtonState(stackButtonStateDTOS);
                stack.setStackMessages(stackMessageDTOS);
                stack.setStackIsDeployed(true);
                try{
                    this.pulumiExport(stack);
                    ResponseDTO responseDTO1 = this.pulumiModifyCanvasState(stack);
                    if(responseDTO1.getStatus().equals("success")) {
                        stack.setStackDraftState(responseDTO1.getData());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stackDAO.save(stack);
                utilService.logEvents(null, log, responseDTO.getData().toString(),"debug");
            } else {
                stackMessageDTO.setType("error");
                stackMessageDTO.setText("Error Occurred while Applying");
                stackMessageDTO.setTime(new Date());
                stackMessageDTO.setContent(responseDTO.getData());
                stackButtonStateDTO= new StackButtonStateDTO();
                stackButtonStateDTO.setName("Apply");
                stackButtonStateDTO.setAction("error");
                if(stack.getStackButtonState()!=null)
                    stackButtonStateDTOS = stack.getStackButtonState();
                if(stack.getStackMessages()!=null)
                    stackMessageDTOS = stack.getStackMessages();
                stackMessageDTOS.add(0,stackMessageDTO);
                stackButtonStateDTOS.add(0,stackButtonStateDTO);
                stack.setStackButtonState(stackButtonStateDTOS);
                stack.setStackMessages(stackMessageDTOS);
                stack.setStackIsDeployed(false);
                try{
                    this.pulumiExport(stack);
                    ResponseDTO responseDTO1 = this.pulumiModifyCanvasState(stack);
                    if(responseDTO1.getStatus().equals("success")) {
                        stack.setStackDraftState(responseDTO1.getData());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stackDAO.save(stack);
                utilService.logEvents(null, log, responseDTO.getData().toString(),"debug");
            }
            utilService.logEvents(null, log,
                    "Ran Updated Command On Pulumi Stack With Id " + stack.getStackId());
            return result;
        } catch (Exception e) {
            stackMessageDTO.setType("error");
            stackMessageDTO.setText("Error Occurred while Applying");
            stackMessageDTO.setTime(new Date());
            stackMessageDTO.setContent(e.getMessage());
            stackButtonStateDTO= new StackButtonStateDTO();
            stackButtonStateDTO.setName("Apply");
            stackButtonStateDTO.setAction("error");
            if(stack.getStackButtonState()!=null)
                stackButtonStateDTOS = stack.getStackButtonState();
            if(stack.getStackMessages()!=null)
                stackMessageDTOS = stack.getStackMessages();
            stackMessageDTOS.add(0,stackMessageDTO);
            stackButtonStateDTOS.add(0,stackButtonStateDTO);
            stack.setStackButtonState(stackButtonStateDTOS);
            stack.setStackMessages(stackMessageDTOS);
            stack.setStackIsDeployed(false);
            try{
                this.pulumiExport(stack);
                ResponseDTO responseDTO1 = this.pulumiModifyCanvasState(stack);
                if(responseDTO1.getStatus().equals("success")) {
                    stack.setStackDraftState(responseDTO1.getData());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            stackDAO.save(stack);
            utilService.logEvents(null, log, e.getMessage(),"error");
            throw e;
        }
    }


    private Object doPulumiExport(Stack stack) throws Exception{
        PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
        pulumiRequestDTO.setDraftState(stack.getStackDraftState());
        pulumiRequestDTO.setStackPath(stack.getStackLocation());
        pulumiRequestDTO.setConfig(this.createStackConfig(stack));

        String reqUrl = env.getProperty("PULUMI_BASE_URL") + "/pulumi-export";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
        String result = restTemplate.postForObject(reqUrl, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        ResponseDTO responseDTO = mapper.readValue(result, new TypeReference<ResponseDTO>() {
        });
        if (responseDTO.getStatus().equals("success")) {
            ObjectNode root = mapper.valueToTree(responseDTO.getData());
            JsonNode deploymentNode = root.get("deployment");
            List<JsonNode> actualResources = new ArrayList<JsonNode>();
            Map<String, Integer> countMap = new HashMap<String, Integer>();
            if(deploymentNode!=null) {
                JsonNode resources = deploymentNode.get("resources");
                if(resources!=null) {
                    for (JsonNode res:resources) {
                        if(!res.get("type").textValue().contains("pulumi:pulumi:Stack") &&
                                !res.get("type").textValue().contains("pulumi:providers")){
                            actualResources.add(res);
                            Integer count = countMap.get(res.get("type").textValue());
                            countMap.put(res.get("type").textValue(), (count == null) ? 1 : count + 1);
                        }
                    }
                }
            }
            responseDTO.setData(actualResources);
            stack.setStackDeployedComponents(actualResources);
            stack.setStackDeployedComponentsWithCount(countMap);
            stackDAO.save(stack);
            return responseDTO;
        }

//        utilService.logEvents(null,log,
//                "Ran E Command On Pulumi Stack With Id "+stack.getStackId());
        return result;
    }

    @Transactional
    public Object pulumiExport(Stack stack) throws Exception{
        int count=0;
        while(count<5) {
            try {
                return this.doPulumiExport(stack);
            } catch (Exception e) {
                e.printStackTrace();
                count=count+1;
                TimeUnit.SECONDS.sleep(5);
            }
        }
        throw new Exception("Max Retries Exceeded");
    }


    public ResponseDTO pulumiModifyCanvasState(Stack stack) throws Exception{
        PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
        pulumiRequestDTO.setDraftState(stack.getStackDraftState());
        pulumiRequestDTO.setDeployedComponents(stack.getStackDeployedComponents());
        String reqUrl = env.getProperty("PULUMI_BASE_URL") + "/pulumi-modify-canvas-state";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
        String result = restTemplate.postForObject(reqUrl, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        ResponseDTO responseDTO = mapper.readValue(result, new TypeReference<ResponseDTO>() {
        });
        return responseDTO;
    }

    public void pulumiDestroy(Stack stack) throws Exception{
        stack.setStackIsDeleting(true);
        stackDAO.save(stack);
        User user = utilService.getAuthenticatedUser();
        asyncService.pulumiDeleter(user.getUserName(),stack);
    }

    public Object getComponentList(CloudPlatform cloudPlatform) throws Exception{
        PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
        pulumiRequestDTO.setCloudPlatform(cloudPlatform);
        String reqUrl = env.getProperty("PULUMI_BASE_URL") + "/pulumi-get-components";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
        return restTemplate.postForObject(reqUrl, entity, String.class);
    }

    public String getPropertiesList(String component_full_name) throws Exception{
        PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
        pulumiRequestDTO.setComponent_full_name(component_full_name);
        String reqUrl = env.getProperty("PULUMI_BASE_URL") + "/pulumi-get-component-properties";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
        return restTemplate.postForObject(reqUrl, entity, String.class);
    }

}
