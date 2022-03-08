package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.StackDAO;
import com.appmodz.executionmodule.dao.StackGroupDAO;
import com.appmodz.executionmodule.dto.*;
import com.appmodz.executionmodule.model.Organization;
import com.appmodz.executionmodule.model.Stack;
import com.appmodz.executionmodule.model.StackGroup;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StackGroupService {
    @Autowired
    StackGroupDAO stackGroupDAO;

    @Autowired
    StackDAO stackDAO;

    @Autowired
    UtilService utilService;

    public StackGroup getStackGroupById(long stackGroupId) throws Exception{
        StackGroup stackGroup = stackGroupDAO.get(stackGroupId);
        if(stackGroup==null)
            throw new Exception("No stack group with this stack group id exists");
        utilService.logEvents(null,log,"Fetched Stack Group With Id "+ stackGroupId);
        return stackGroup;
    }

    public List<Stack> getStackGroupWithStacksById(long stackGroupId) throws Exception{
        StackGroup stackGroup = stackGroupDAO.getStacksOfStackGroup(stackGroupId);
        if(stackGroup==null)
            throw new Exception("No stack group with this stack group id exists");
        utilService.logEvents(null,log,"Fetched Stack Group With Id "+ stackGroupId);
        return stackGroup.getStacks();
    }

    public StackGroup createStackGroup(StackGroupRequestDTO stackGroupRequestDTO) throws Exception{
        StackGroup stackGroup = new StackGroup();
        stackGroup.setStackGroupName(stackGroupRequestDTO.getStackGroupName());
        stackGroupDAO.save(stackGroup);
        if(stackGroupRequestDTO.getStackIds()!=null) {
            for(long id: stackGroupRequestDTO.getStackIds()) {
                Stack stack = stackDAO.get(id);
                stack.setStackGroup(stackGroup);
                stackDAO.save(stack);
            }
        }
        return stackGroup;
    }

    public StackGroup updateStackGroup(StackGroupRequestDTO stackGroupRequestDTO) throws Exception{
        StackGroup stackGroup = stackGroupDAO.get(stackGroupRequestDTO.getStackGroupId());
        if(stackGroupRequestDTO.getStackGroupName()!=null)
        stackGroup.setStackGroupName(stackGroupRequestDTO.getStackGroupName());

        stackGroupDAO.save(stackGroup);
        if(stackGroupRequestDTO.getStackIds()!=null) {
            stackDAO.setStackGroupNull(stackGroupRequestDTO.getStackGroupId());
            for(long id: stackGroupRequestDTO.getStackIds()) {
                Stack stack = stackDAO.get(id);
                stack.setStackGroup(stackGroup);
                stackDAO.save(stack);
            }
        }

        return stackGroup;
    }

    public Object searchComponent(StackGroupRequestDTO stackGroupRequestDTO) throws Exception{
        List<Stack> stacks = stackDAO.getByStackGroupId(stackGroupRequestDTO.getStackGroupId());
        Map<String,ArrayList<JsonNode>> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        for(Stack stack:stacks) {
            JsonNode resources = mapper.valueToTree(stack.getStackDeployedComponents());
            for (JsonNode res:resources) {
                String type = res.get("type").textValue();
                String[] componentNameArr = type.split(":");
                String componentName = componentNameArr[componentNameArr.length-1].strip();

                for (int i=0; i<stackGroupRequestDTO.getComponents().size();i++) {
                    String component = stackGroupRequestDTO.getComponents().get(i);
                    System.out.println(component);

                    if(component.equalsIgnoreCase(componentName)) {
                        ArrayList<JsonNode> components = new ArrayList<>();
                        if(map.get(component)!=null) {
                            components = map.get(component);
                        }
                        components.add(res);
                        map.put(component,components);
                    }
                }
            }
        }
        return map;
    }

    public String deleteMultipleStackGroups(StackGroupRequestDTO stackGroupRequestDTO) throws Exception{
        List<Long> ids = stackGroupRequestDTO.getIds();
        StringBuilder exceptions = new StringBuilder();
        StringBuilder successes = new StringBuilder();
        for (long id: ids) {
            StackGroup stackGroup = stackGroupDAO.get(id);
            try {
                stackGroupDAO.delete(stackGroup);
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

    public List listStackGroups() {
        List<StackGroup> stackGroups = stackGroupDAO.getAll();
        utilService.logEvents(null,log,"Listed StackGroups");
        return stackGroups;
    }

    public SearchResultDTO searchStackGroups(SearchRequestDTO searchRequestDTO) {
        SearchResultDTO searchResultDTO = stackGroupDAO.search(searchRequestDTO);
        utilService.logEvents(null,log,"Searched stackGroups");
        return searchResultDTO;
    }
}
