package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.StackDAO;
import com.appmodz.executionmodule.dao.TemplateDAO;
import com.appmodz.executionmodule.dto.TemplateRequestDTO;
import com.appmodz.executionmodule.model.Stack;
import com.appmodz.executionmodule.model.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TemplateService {

    @Autowired
    StackDAO stackDAO;

    @Autowired
    TemplateDAO templateDAO;

    @Autowired
    UtilService utilService;

    public Template saveStackAsTemplate(TemplateRequestDTO templateRequestDTO) throws Exception{
        Stack stack = stackDAO.get(templateRequestDTO.getStackId());
        Template template = new Template();
        template.setName(templateRequestDTO.getName());
        template.setState(stack.getStackDraftState());
        template.setOwner(utilService.getAuthenticatedUser());
        templateDAO.save(template);
        utilService.logEvents(null,log,"Fetched Template With Id "+ template.getId());
        return template;
    }

    public List listAllTemplates() throws Exception{
        utilService.logEvents(null,log,"Fetched Templates");
        return templateDAO.getAll();
    }

    public Template getTemplate(TemplateRequestDTO templateRequestDTO) throws Exception{
        utilService.logEvents(null,log,"Fetched Template With Id "+ templateRequestDTO.getId());
        return templateDAO.get(templateRequestDTO.getId());
    }

    public Template updateTemplate(TemplateRequestDTO templateRequestDTO) throws Exception{
        Template template = templateDAO.get(templateRequestDTO.getId());
        if(templateRequestDTO.getName()!=null)
            template.setName(templateRequestDTO.getName());
        if(templateRequestDTO.getState()!=null)
            template.setState(templateRequestDTO.getState());
        templateDAO.save(template);
        utilService.logEvents(null,log,"Updated Template With Id "+ template.getId());
        return template;
    }

    public void deleteTemplate(TemplateRequestDTO templateRequestDTO) throws Exception{
        Template template = templateDAO.get(templateRequestDTO.getId());
        templateDAO.delete(template);
        utilService.logEvents(null,log,"Deleted Template With Id "+ template.getId());
    }

    public void deleteMultipleTemplates(TemplateRequestDTO templateRequestDTO) throws Exception{
        List<Long> ids = templateRequestDTO.getIds();
        for(Long id:ids) {
            Template template = templateDAO.get(id);
            templateDAO.delete(template);
            utilService.logEvents(null,log,"Deleted Template With Id "+ template.getId());
        }
    }
}
