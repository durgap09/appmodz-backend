package com.appmodz.executionmodule.controller.v1;

import com.appmodz.executionmodule.dto.ResponseDTO;
import com.appmodz.executionmodule.dto.StackGroupRequestDTO;
import com.appmodz.executionmodule.dto.StackRequestDTO;
import com.appmodz.executionmodule.service.StackGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController("v1StackGroupsController")
@RequestMapping("/v1/stack-groups")
public class StackGroupController {

    @Autowired
    StackGroupService stackGroupService;


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handle(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();
        ResponseDTO responseDTO = new ResponseDTO("failure",ex.getMessage(),null);
        return responseDTO;
    }

    @RequestMapping(value="/",method= RequestMethod.GET)
    @ResponseBody
    public Object getStacks(@RequestParam(required = false) String format,
                            @RequestParam(required = false) List<Long> ids,
                            HttpServletResponse response) throws Exception{
        if(format!=null) {
            return new ResponseDTO("success",null,null);
        } else
            return new ResponseDTO("success",null,
                    stackGroupService.listStackGroups());
    }

    @RequestMapping(value="/{id}",method= RequestMethod.GET, produces="application/json")
    @ResponseBody
    public Object getStack(@PathVariable Long id,@RequestParam(required = false) String data) throws Exception{
        if (id!=null) {
            return new ResponseDTO("success", null,
                    stackGroupService.getStackGroupById(id));
        }
        else {
            return new ResponseDTO("failure","Required parameters not present",null);
        }
    }

    @RequestMapping(value="/{id}/stacks",method= RequestMethod.GET, produces="application/json")
    @ResponseBody
    public Object getStacksOfStackGroup(@PathVariable Long id,@RequestParam(required = false) String data) throws Exception{
        if (id!=null) {
            return new ResponseDTO("success", null,
                    stackGroupService.getStackGroupWithStacksById(id));
        }
        else {
            return new ResponseDTO("failure","Required parameters not present",null);
        }
    }

    @RequestMapping(value="/",method= RequestMethod.POST, produces="application/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object createAndSearchStacks(@RequestBody StackGroupRequestDTO stackGroupRequestDTO) throws Exception{
        if(stackGroupRequestDTO!=null&&stackGroupRequestDTO.getSearch()!=null) {
            return new ResponseDTO("success", null,
                    stackGroupService.searchStackGroups(stackGroupRequestDTO));
        }
        else if (stackGroupRequestDTO!=null&& stackGroupRequestDTO.getStackGroupName()!=null) {
            return new ResponseDTO("success",null,stackGroupService.createStackGroup(stackGroupRequestDTO));
        }
        return new ResponseDTO("failure","Required parameters not present",null);
    }

    @RequestMapping(value="/components",method= RequestMethod.POST, produces="application/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object searchComponents(@RequestBody StackGroupRequestDTO stackGroupRequestDTO) throws Exception{
            return new ResponseDTO("success", null,
                    stackGroupService.searchComponent(stackGroupRequestDTO));
    }

    @RequestMapping(value="/{id}",method= RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public Object updateStack(@PathVariable Long id,@RequestBody StackGroupRequestDTO stackGroupRequestDTO) throws Exception{
        stackGroupRequestDTO.setStackGroupId(id);
        return new ResponseDTO("success",null,
                stackGroupService.updateStackGroup(stackGroupRequestDTO));
    }

    @RequestMapping(value="/",method= RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Object deleteStacks(@RequestBody StackGroupRequestDTO stackGroupRequestDTO) throws Exception{
        if (stackGroupRequestDTO.getIds()!=null) {
            return new ResponseDTO("success",null, stackGroupService.deleteMultipleStackGroups(stackGroupRequestDTO));
        }
        else {
            return new ResponseDTO("failure","Required parameters not present",null);
        }
    }
}
