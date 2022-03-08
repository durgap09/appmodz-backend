package com.appmodz.executionmodule.controller.v1;

import com.appmodz.executionmodule.dto.CanvasRequestDTO;
import com.appmodz.executionmodule.dto.ConfigurationProfileDTO;
import com.appmodz.executionmodule.dto.ResponseDTO;
import com.appmodz.executionmodule.dto.TemplateRequestDTO;
import com.appmodz.executionmodule.service.ConfigurationProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("v1ConfigurationProfileController")
@RequestMapping("/v1/configuration-profiles")
public class ConfigurationProfileController {

    @Autowired
    ConfigurationProfileService configurationProfileService;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handle(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();
        ResponseDTO responseDTO = new ResponseDTO("failure",ex.getMessage(),null);
        return responseDTO;
    }

    @RequestMapping(value="/",method= RequestMethod.GET)
    @ResponseBody
    public Object getConfigurationProfiles(@RequestParam(required = false) Long cloudPlatformId) throws Exception{
        if(cloudPlatformId==null)
            cloudPlatformId = 0L;
        return new ResponseDTO("success",null,
                configurationProfileService.listAllConfigurationProfiles(cloudPlatformId));
    }


    @RequestMapping(value="/{id}",method= RequestMethod.GET, produces="application/json")
    @ResponseBody
    public Object getConfigurationProfile(@PathVariable Long id) throws Exception{
        if (id!=null) {
            ConfigurationProfileDTO configurationProfileDTO = new ConfigurationProfileDTO();
            configurationProfileDTO.setId(id);
            return new ResponseDTO("success", null,
                    configurationProfileService.getConfigurationProfileById(configurationProfileDTO));
        }
        else {
            return new ResponseDTO("failure","Required parameters not present",null);
        }
    }

    @RequestMapping(value="/",method= RequestMethod.POST, produces="application/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object createAndSearchConfigurationProfiles(@RequestBody ConfigurationProfileDTO configurationProfileDTO) throws Exception{
        if(configurationProfileDTO.getSearch()!=null){
            return new ResponseDTO("success", null,
                    configurationProfileService.searchConfigurationProfiles(configurationProfileDTO));
        } else {
            return new ResponseDTO("success", null,
                    configurationProfileService.createConfigurationProfile(configurationProfileDTO));
        }

    }


    @RequestMapping(value="/{id}",method= RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public Object updateConfigurationProfile(@PathVariable Long id,@RequestBody ConfigurationProfileDTO configurationProfileDTO) throws Exception{
        configurationProfileDTO.setId(id);
        return new ResponseDTO("success",null,
                configurationProfileService.editConfigurationProfile(configurationProfileDTO));
    }


    @RequestMapping(value="/",method= RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Object deleteConfigurationProfiles(@RequestBody ConfigurationProfileDTO configurationProfileDTO) throws Exception{
        if (configurationProfileDTO.getIds()!=null) {
            configurationProfileService.deleteMultipleConfigurationProfiles(configurationProfileDTO);
            return new ResponseDTO("success",null, null);
        }
        else {
            return new ResponseDTO("failure","Required parameters not present",null);
        }
    }

    @RequestMapping(value="/cloudplatform-config-parameters",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object getCloudPlatformConfigParameters(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        return new ResponseDTO("success",null,configurationProfileService.getCloudPlatformConfigParameters(canvasRequestDTO));
    }

}
