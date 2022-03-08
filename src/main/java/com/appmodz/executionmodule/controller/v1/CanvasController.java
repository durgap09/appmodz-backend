package com.appmodz.executionmodule.controller.v1;

import com.appmodz.executionmodule.dao.ComponentDAO;
import com.appmodz.executionmodule.dto.*;
import com.appmodz.executionmodule.model.Component;
import com.appmodz.executionmodule.model.Stack;
import com.appmodz.executionmodule.service.CanvasService;
import com.appmodz.executionmodule.service.PulumiService;
import com.appmodz.executionmodule.service.StackService;
import com.appmodz.executionmodule.service.TerraformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController("v1CanvasController")
@RequestMapping("/v1/canvas")
public class CanvasController {

    @Autowired
    CanvasService canvasService;

    @Autowired
    StackService stackService;

    @Autowired
    TerraformService terraformService;

    @Autowired
    PulumiService pulumiService;

    @Autowired
    ComponentDAO componentDAO;

    private Boolean PULUMI = true;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handle(Exception ex, HttpServletRequest request) {
        ResponseDTO responseDTO = new ResponseDTO("failure",""+ex,null);
        return responseDTO;
    }

    @RequestMapping(value="/components",method= RequestMethod.GET, produces="application/json")
    @ResponseBody
    public Object getComponents(@RequestParam(required = false) Long cloudPlatformId) throws Exception{
        CanvasRequestDTO canvasRequestDTO = new CanvasRequestDTO();
        if(cloudPlatformId==null)
            canvasRequestDTO.setCloudPlatformId(0);
        else
            canvasRequestDTO.setCloudPlatformId(cloudPlatformId);
        return new ResponseDTO("success",null,
                canvasService.listComponents(canvasRequestDTO));
    }

    @RequestMapping(value="/components/{id}",method= RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public Object updateComponent(@PathVariable Long id,
                                      @RequestBody ComponentDTO componentDTO) throws Exception {
        componentDTO.setId(id);
        if (id!=null) {
            return new ResponseDTO("success",null,
                    canvasService.createOrUpdateComponent(componentDTO));
        } else {
            return new ResponseDTO("failure","Required parameters not present",null);
        }
    }

    @RequestMapping(value="/components",method= RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Object deleteComponents(@RequestBody ComponentDTO componentDTO) throws Exception {
        return new ResponseDTO("success",null,
                canvasService.deleteMultipleComponents(componentDTO));
    }

    @RequestMapping(value="/properties/{id}",method= RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public Object updateComponent(@PathVariable Long id,
                                  @RequestBody PropertyDTO propertyDTO) throws Exception {
        propertyDTO.setId(id);
        if (id!=null) {
            return new ResponseDTO("success",null,
                    canvasService.createOrUpdateProperty(propertyDTO));
        } else {
            return new ResponseDTO("failure","Required parameters not present",null);
        }
    }

    @RequestMapping(value="/properties",method= RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Object deleteProperties(@RequestBody PropertyDTO propertyDTO) throws Exception {
        return new ResponseDTO("success",null,
                canvasService.deleteMultipleProperties(propertyDTO));
    }

    @RequestMapping(value="/code/folder",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object getFolderStructure(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        Stack stack = stackService.getStackById(canvasRequestDTO.getStackId());
        String path = ""+stack.getWorkspace().getWorkspaceId()+"/"+stack.getStackId()+canvasRequestDTO.getPath();
        return new ResponseDTO("success",null,
                canvasService.getFolderStructure(path));
    }

    @RequestMapping(value="/code/file",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object getFileContents(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        Stack stack = stackService.getStackById(canvasRequestDTO.getStackId());
        String path = ""+stack.getWorkspace().getWorkspaceId()+"/"+stack.getStackId()+canvasRequestDTO.getPath();
        return new ResponseDTO("success",null,
                canvasService.getFileContent(path));
    }

    @RequestMapping(value="/save",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object saveDraftState(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        if(canvasRequestDTO.getIsDraft() == null || canvasRequestDTO.getIsDraft())
        return new ResponseDTO("success",null,
                stackService.saveState(canvasRequestDTO));
        else {
            Stack stack = stackService.getStackById(canvasRequestDTO.getStackId());
            if(PULUMI)
                return pulumiService.pulumiStatewiseMovement(stack);
            return new ResponseDTO("success", null,
                    terraformService.terraformStateWiseMovement(stack));
        }

    }

    @RequestMapping(value="/validate",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object validate(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
             Stack stack = stackService.getStackById(canvasRequestDTO.getStackId());
            if(PULUMI)
                return pulumiService.pulumiValidate(stack);
            return new ResponseDTO("success",null,
                    terraformService.terraformValidate(stack));

    }

    @RequestMapping(value="/plan",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object plan(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        Stack stack = stackService.getStackById(canvasRequestDTO.getStackId());
        if(PULUMI)
            return pulumiService.pulumiPreview(stack);
        return new ResponseDTO("success",null,
                terraformService.terraformPlan(stack));

    }

    @RequestMapping(value="/publish",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object publish(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        Stack stack = stackService.getStackById(canvasRequestDTO.getStackId());
        if(PULUMI)
            return pulumiService.pulumiUp(stack);
        return new ResponseDTO("success",null,
                terraformService.terraformApply(stack));

    }

    @RequestMapping(value="/deployed-components",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object export(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        Stack stack = stackService.getStackById(canvasRequestDTO.getStackId());
        if(PULUMI)
            return pulumiService.pulumiExport(stack);
        return new ResponseDTO("success",null,
                terraformService.terraformApply(stack));

    }


    @RequestMapping(value="/modify-canvas-state",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object modifyCanvasState(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        Stack stack = stackService.getStackById(canvasRequestDTO.getStackId());
        if(PULUMI)
            return pulumiService.pulumiModifyCanvasState(stack);
        return new ResponseDTO("success",null,
                terraformService.terraformApply(stack));

    }

    @RequestMapping(value="/cloud-platform-components",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object getCloudPlatformComponents(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        return canvasService.getComponentList(canvasRequestDTO);
    }

    @RequestMapping(value="/component-properties",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object getComponentProperties(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        return canvasService.getProperties(canvasRequestDTO);
    }

    @RequestMapping(value="/create-component",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object getCreateComponent(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        return new ResponseDTO("success",null,
                canvasService.createComponent(canvasRequestDTO));
    }

    @RequestMapping(value="/create-multiple-components",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object getCreateMultipleComponents(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        return new ResponseDTO("success",null,
                canvasService.createMultipleComponents(canvasRequestDTO));
    }

    @RequestMapping(value="/create-component-from-properties",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object getCreateComponentFromProperties(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        return new ResponseDTO("success",null,
                canvasService.createComponentFromProperties(canvasRequestDTO));
    }

    @RequestMapping(value="/appmodz-categories",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object getAppmodzCategories(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        return new ResponseDTO("success",null,canvasService.getAppmodzCategories());
    }

    @RequestMapping(value="/cloudplatform-config-parameters",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object getCloudPlatformConfigParameters(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        return new ResponseDTO("success",null,canvasService.getCloudPlatformConfigParameters(canvasRequestDTO));
    }

    @RequestMapping(value="/create-appmodz-category",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object createAppmodzCategory(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        return new ResponseDTO("success",null, canvasService.createAppmodzCategories(canvasRequestDTO));
    }

    @RequestMapping(value="/cloud-platforms",method= RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Object getCloudPlatforms(@RequestBody(required = false) CanvasRequestDTO canvasRequestDTO) throws Exception{
        return new ResponseDTO("success",null,canvasService.getCloudPlatforms());
    }

    @RequestMapping(value="/components",method= RequestMethod.POST, produces="application/json",consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object searchComponents(@RequestBody CanvasRequestDTO canvasRequestDTO) throws Exception {
        if(canvasRequestDTO!=null&&canvasRequestDTO.getSearch()!=null){
            return new ResponseDTO("success",null,
                    canvasService.searchComponents(canvasRequestDTO));
        } else {
            return new ResponseDTO("failure","Required parameters not present",null);
        }
    }
}
