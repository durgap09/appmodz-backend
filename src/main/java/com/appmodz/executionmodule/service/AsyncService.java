package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.ApplicationConfigurationDAO;
import com.appmodz.executionmodule.dao.StackDAO;
import com.appmodz.executionmodule.dto.PulumiRequestDTO;
import com.appmodz.executionmodule.dto.ResponseDTO;
import com.appmodz.executionmodule.dto.StackConfigDTO;
import com.appmodz.executionmodule.model.Stack;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AsyncService {

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

    @Async
    public void pulumiDeleter(String userName, Stack stack) throws Exception {
        PulumiRequestDTO pulumiRequestDTO = new PulumiRequestDTO();
        pulumiRequestDTO.setDraftState(stack.getStackDraftState());
        pulumiRequestDTO.setStackPath(stack.getStackLocation());
        pulumiRequestDTO.setConfig(this.createStackConfig(stack));

        String reqUrl = env.getProperty("PULUMI_BASE_URL")+"/pulumi-destroy";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(pulumiRequestDTO.toString(), headers);
        String result = restTemplate.postForObject(reqUrl, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        ResponseDTO responseDTO = mapper.readValue(result, new TypeReference<ResponseDTO>(){});
        if(responseDTO.getStatus().equals("failure")) {
            stack.setErrorMessage(responseDTO.getMessage());
            stack.setStackIsDeleting(false);
            stackDAO.save(stack);
            if(responseDTO.getMessage().indexOf("doesnt exist")!=-1) {
                stackDAO.delete(stack);
            }
        } else {
            stackDAO.delete(stack);
        }
        utilService.logEvents(userName,log,
                "Ran Destroy And Delete Command On Pulumi Stack "+
                        stack.getStackId()+". "
                        +stack.getTerraformBackend().getName());
    }
}
