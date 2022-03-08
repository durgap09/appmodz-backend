package com.appmodz.executionmodule.controller.v1;

import com.appmodz.executionmodule.dto.ResponseDTO;
import com.appmodz.executionmodule.dto.StackRequestDTO;
import com.appmodz.executionmodule.dto.TemplateRequestDTO;
import com.appmodz.executionmodule.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("v1TemplatesController")
@RequestMapping("/v1/templates")
public class TemplateController {

    @Autowired
    TemplateService templateService;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handle(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();
        ResponseDTO responseDTO = new ResponseDTO("failure",ex.getMessage(),null);
        return responseDTO;
    }

    @RequestMapping(value="/",method= RequestMethod.GET)
    @ResponseBody
    public Object getTemplates(@RequestParam(required = false) String format, HttpServletResponse response) throws Exception{
            return new ResponseDTO("success",null,
                    templateService.listAllTemplates());
    }


    @RequestMapping(value="/{id}",method= RequestMethod.GET, produces="application/json")
    @ResponseBody
    public Object getTemplate(@PathVariable Long id) throws Exception{
        if (id!=null) {
            TemplateRequestDTO templateRequestDTO = new TemplateRequestDTO();
            templateRequestDTO.setId(id);
            return new ResponseDTO("success", null,
                    templateService.getTemplate(templateRequestDTO));
        }
        else {
            return new ResponseDTO("failure","Required parameters not present",null);
        }
    }

    @RequestMapping(value="/",method= RequestMethod.POST, produces="application/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object createAndSearchTemplates(@RequestBody TemplateRequestDTO templateRequestDTO) throws Exception{
        if(templateRequestDTO.getAction()!=null) {
            if(templateRequestDTO.getAction().equals("save_stack_as_template"))
                return new ResponseDTO("success",null,templateService.saveStackAsTemplate(templateRequestDTO));
        }
        return new ResponseDTO("failure","Required parameters not present",null);
    }


    @RequestMapping(value="/{id}",method= RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public Object updateTemplate(@PathVariable Long id,@RequestBody TemplateRequestDTO templateRequestDTO) throws Exception{
        templateRequestDTO.setId(id);
        return new ResponseDTO("success",null,
                templateService.updateTemplate(templateRequestDTO));
    }

    @RequestMapping(value="/{id}",method= RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Object deleteTemplate(@PathVariable Long id) throws Exception{
        if (id!=null) {
            TemplateRequestDTO templateRequestDTO = new TemplateRequestDTO();
            templateRequestDTO.setId(id);
            templateService.deleteTemplate(templateRequestDTO);
            return new ResponseDTO("success",null, null);
        }
        else {
            return new ResponseDTO("failure","Required parameters not present",null);
        }
    }

    @RequestMapping(value="/",method= RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Object deleteTemplates(@RequestBody TemplateRequestDTO templateRequestDTO) throws Exception{
        if (templateRequestDTO.getIds()!=null) {
            templateService.deleteMultipleTemplates(templateRequestDTO);
            return new ResponseDTO("success",null, null);
        }
        else {
            return new ResponseDTO("failure","Required parameters not present",null);
        }
    }

}
