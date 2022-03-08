package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.ComponentDAO;
import com.appmodz.executionmodule.dao.PropertyDAO;
import com.appmodz.executionmodule.dto.*;
import com.appmodz.executionmodule.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CanvasService {

    @Autowired
    ComponentDAO componentDAO;

    @Autowired
    private Environment env;

    @Autowired
    private PulumiService pulumiService;

    @Autowired
    UtilService utilService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PropertyDAO propertyDAO;

    public List listComponents(CanvasRequestDTO canvasRequestDTO) {
        List<AppmodzCategory> parents= componentDAO.getAppmodzCategories();
        List <CanvasComponent> canvasComponents = new ArrayList<>();
        for(AppmodzCategory parent: parents) {
            CanvasComponent parentCanvasComponent = new CanvasComponent();
            parentCanvasComponent.setId(parent.getAppmodzCategoryId());
            parentCanvasComponent.setName(parent.getAppmodzCategoryName());
            List<Component> components = componentDAO.getComponentsByCategoryId(parent.getAppmodzCategoryId());
            if(canvasRequestDTO.getCloudPlatformId()==0)
                parentCanvasComponent.setChildren(components);
            else {
                components=components
                        .stream()
                        .filter(c -> c.getComponentCategory().getCloudPlatform().getCloudPlatformId() == canvasRequestDTO.getCloudPlatformId())
                        .collect(Collectors.toList());
                parentCanvasComponent.setChildren(components);
            }
            if(components.size()>0)
            canvasComponents.add(parentCanvasComponent);
        }
        utilService.logEvents(null,log,"Exported Canvas Components");
        return canvasComponents;
    }

    private static FileOrFolder traverse(File file) {
        if(!file.exists())
            return null;
        FileOrFolder fileOrFolder = new FileOrFolder();
        fileOrFolder.setName(file.getName());
        if (file.isDirectory()) {
            fileOrFolder.setIsFile(false);
            File[] children = file.listFiles();
            List<FileOrFolder> data = new ArrayList<>();
            assert children != null;
            for (File child : children) {
                data.add(traverse(child));
            }
            fileOrFolder.setData(data);
        }else {
            fileOrFolder.setIsFile(true);
        }
        return fileOrFolder;
    }

    private static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public FileOrFolder getFolderStructure(String path) throws Exception {
        if(!utilService.checkLicense("CODE_EDITOR"))
            throw new Exception("User's License Doesnt Allow This Operation Or Resource Limit Reached For This Operation");
        System.out.println(path);
        System.out.println(env.getProperty("WORKING_DIR_2")+path);
        File file = new File(env.getProperty("WORKING_DIR_2")+path);
        if(!file.exists())
            throw new Exception("File or Folder Not Present");
        return traverse(file);
    }

    public String getFileContent(String path) throws Exception {
        if(!utilService.checkLicense("CODE_EDITOR"))
            throw new Exception("User's License Doesnt Allow This Operation Or Resource Limit Reached For This Operation");
        File file = new File(env.getProperty("WORKING_DIR_2")+path);
        if(!file.exists())
            throw new Exception("File or Folder Not Present");
        return readFile(env.getProperty("WORKING_DIR_2")+path, StandardCharsets.UTF_8);
    }

    public Object getComponentList(CanvasRequestDTO canvasRequestDTO) throws Exception{
        CloudPlatform cloudPlatform = componentDAO.getCloudPlatform(canvasRequestDTO.getCloudPlatformId());
        return pulumiService.getComponentList(cloudPlatform);
    }

    public Object getProperties(CanvasRequestDTO canvasRequestDTO) throws Exception{
        return pulumiService.getPropertiesList(canvasRequestDTO.getComponentFullName());
    }

    public Object createComponent(CanvasRequestDTO canvasRequestDTO) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        String result = pulumiService.getPropertiesList(canvasRequestDTO.getComponentFullName());
        ResponseDTO responseDTO = mapper.readValue(result, new TypeReference<ResponseDTO>() {
        });
        if (responseDTO.getStatus().equals("success")) {
            String[] parts = canvasRequestDTO.getComponentFullName().split("\\.");
            String compCategory = parts[1];
            String compName = parts[2];
            String cloudPlatformName = parts[0];
            List<Property>  props = mapper.convertValue(responseDTO.getData(),new TypeReference<List<Property>>() {});
            if(props.size()==0)
                throw new Exception("Component Not Found. Please ensure that the spelling is correct.");
            if(compCategory.equals("ec2")&&compName.equals("Instance")) {
                for (Property prop:props) {
                    if(prop.getName().equals("userData")||prop.getName().equals("userDataBase64")) {
                        prop.setType("textarea");
                    }
                }
            }
            Property property = new Property();
            property.setAppmodzName("name");
            property.setName("name");
            property.setIsVisible(true);
            property.setIsConnectable(false);
            property.setType("string");
            props.add(property);
            ComponentCategory componentCategory = componentDAO.getComponentCategory(compCategory,cloudPlatformName);
            if(componentCategory==null) {
                CloudPlatform cloudPlatform = componentDAO.getCloudPlatform(cloudPlatformName);
                componentCategory = new ComponentCategory();
                componentCategory.setComponentCategoryName(compCategory);
                componentCategory.setCloudPlatform(cloudPlatform);
                componentDAO.save(componentCategory);
            }
            Component component = componentDAO.get(compName,componentCategory.getComponentCategoryId());
            if(component==null) {
                component = new Component();
                component.setName(compName);
                if(canvasRequestDTO.getAppmodzName()!=null)
                    component.setAppmodzName(canvasRequestDTO.getAppmodzName());
                else
                    component.setAppmodzName(compName);
                component.setComponentCategory(componentCategory);
                component.setProperties(props);
                component.setIsVisible(true);
                component.setIacType(1);
            } else {
                if(canvasRequestDTO.getReplaceIfExists()==null || !canvasRequestDTO.getReplaceIfExists())
                    throw new Exception("Component Already Exists. Please send replaceIfExists flag true to replace properties");
                else {
                    component.setProperties(props);
                    if(canvasRequestDTO.getAppmodzName()!=null)
                        component.setAppmodzName(canvasRequestDTO.getAppmodzName());
                }
            }

            componentDAO.save(component);

            AppmodzComponentCategory appmodzComponentCategory =
                    componentDAO.getAppmodzComponentCategoryByAppmodzCategoryId(canvasRequestDTO.getAppmodzCategoryId());

            List<Component> components = appmodzComponentCategory.getComponents();
            if(components == null)
                components = new ArrayList<>();

            components.add(component);

            componentDAO.save(appmodzComponentCategory);
            return component;
        } else{
            throw new Exception(responseDTO.getMessage());
        }
    }

    public Object createMultipleComponents(CanvasRequestDTO canvasRequestDTO) throws Exception{
        List<Component> createdComponents = new ArrayList<>();
        for(String componentFullName:canvasRequestDTO.getComponentFullNames()) {
            ObjectMapper mapper = new ObjectMapper();
            String result = pulumiService.getPropertiesList(componentFullName);
            ResponseDTO responseDTO = mapper.readValue(result, new TypeReference<ResponseDTO>() {
            });
            if (responseDTO.getStatus().equals("success")) {
                String[] parts = componentFullName.split("\\.");
                String compCategory = parts[1];
                String compName = parts[2];
                String cloudPlatformName = parts[0];
                List<Property>  props = mapper.convertValue(responseDTO.getData(),new TypeReference<List<Property>>() {});
                if(props.size()==0)
                    throw new Exception("Component Not Found. Please ensure that the spelling is correct.");
                Property property = new Property();
                property.setAppmodzName("name");
                property.setName("name");
                property.setIsVisible(true);
                property.setIsConnectable(false);
                property.setType("string");
                props.add(property);
                ComponentCategory componentCategory = componentDAO.getComponentCategory(compCategory,cloudPlatformName);
                if(componentCategory==null) {
                    CloudPlatform cloudPlatform = componentDAO.getCloudPlatform(cloudPlatformName);
                    componentCategory = new ComponentCategory();
                    componentCategory.setComponentCategoryName(compCategory);
                    componentCategory.setCloudPlatform(cloudPlatform);
                    componentDAO.save(componentCategory);
                }
                Component component = componentDAO.get(compName,componentCategory.getComponentCategoryId());
                if(component==null) {
                    component = new Component();
                    component.setName(compName);
                    if(canvasRequestDTO.getAppmodzName()!=null)
                        component.setAppmodzName(canvasRequestDTO.getAppmodzName());
                    else
                        component.setAppmodzName(compName);
                    component.setComponentCategory(componentCategory);
                    component.setProperties(props);
                    component.setIsVisible(true);
                    component.setIacType(1);
                } else {
                    if(canvasRequestDTO.getReplaceIfExists()==null || !canvasRequestDTO.getReplaceIfExists())
                        throw new Exception("Component Already Exists. Please send replaceIfExists flag true to replace properties");
                    else {
                        component.setProperties(props);
                        if(canvasRequestDTO.getAppmodzName()!=null)
                            component.setAppmodzName(canvasRequestDTO.getAppmodzName());
                    }
                }

                componentDAO.save(component);

                AppmodzComponentCategory appmodzComponentCategory =
                        componentDAO.getAppmodzComponentCategoryByAppmodzCategoryId(canvasRequestDTO.getAppmodzCategoryId());

                List<Component> components = appmodzComponentCategory.getComponents();
                if(components == null)
                    components = new ArrayList<>();

                components.add(component);

                componentDAO.save(appmodzComponentCategory);
                createdComponents.add(component);
            } else{
                throw new Exception(responseDTO.getMessage());
            }
        }
        return createdComponents;
    }


    public Object createComponentFromProperties(CanvasRequestDTO canvasRequestDTO) throws Exception{
            String[] parts = canvasRequestDTO.getComponentFullName().split("\\.");
            String compCategory = parts[1];
            String compName = parts[2];
            String cloudPlatformName = parts[0];
            List<Property>  props = canvasRequestDTO.getComponentProperties();
            if(props.size()==0)
                throw new Exception("Component Not Found. Please ensure that the spelling is correct.");
            ComponentCategory componentCategory = componentDAO.getComponentCategory(compCategory,cloudPlatformName);
            if(componentCategory==null) {
                CloudPlatform cloudPlatform = componentDAO.getCloudPlatform(cloudPlatformName);
                componentCategory = new ComponentCategory();
                componentCategory.setComponentCategoryName(compCategory);
                componentCategory.setCloudPlatform(cloudPlatform);
                componentDAO.save(componentCategory);
            }
            Component component = componentDAO.get(compName,componentCategory.getComponentCategoryId());
            if(component==null) {
                component = new Component();
                component.setName(compName);
                component.setAppmodzName(compName);
                component.setComponentCategory(componentCategory);
                component.setProperties(props);
                component.setIsVisible(true);
                component.setIacType(1);
            } else {
                if(canvasRequestDTO.getReplaceIfExists()==null || !canvasRequestDTO.getReplaceIfExists())
                    throw new Exception("Component Already Exists. Please send replaceIfExists flag true to replace properties");
                else {
                    component.setProperties(props);
                }
            }

            componentDAO.save(component);

            AppmodzComponentCategory appmodzComponentCategory =
                    componentDAO.getAppmodzComponentCategoryByAppmodzCategoryId(canvasRequestDTO.getAppmodzCategoryId());

            List<Component> components = appmodzComponentCategory.getComponents();
            if(components == null)
                components = new ArrayList<>();

            components.add(component);

            componentDAO.save(appmodzComponentCategory);
            return component;
    }


    public Object getAppmodzCategories() throws Exception{
        return componentDAO.getAppmodzCategories();
    }

    public Object createAppmodzCategories(CanvasRequestDTO canvasRequestDTO) throws Exception{
        AppmodzCategory appmodzCategory = new AppmodzCategory();
        appmodzCategory.setAppmodzCategoryName(canvasRequestDTO.getAppmodzCategoryName());
        componentDAO.save(appmodzCategory);

        AppmodzComponentCategory appmodzComponentCategory = new AppmodzComponentCategory();
        appmodzComponentCategory.setAppmodzCategory(appmodzCategory);
        componentDAO.save(appmodzComponentCategory);
        return appmodzCategory;
    }

    public Object getCloudPlatforms() throws Exception{
        return componentDAO.getCloudPlatforms();
    }

    public Object getCloudPlatformConfigParameters(CanvasRequestDTO canvasRequestDTO) throws Exception{
        return componentDAO.getCloudPlatformConfigParameters(canvasRequestDTO.getCloudPlatformId());
    }

    public Object createOrUpdateComponent(ComponentDTO componentDTO) throws Exception {
        Component component = componentDAO.get(componentDTO.getId());
        modelMapper.map(componentDTO,component);
        componentDAO.save(component);
        return component;
    }

    public Object createOrUpdateProperty(PropertyDTO propertyDTO) throws Exception {
        switch (propertyDTO.getLevel()) {
            case 0:
                Property property = propertyDAO.get(propertyDTO.getId());
                modelMapper.map(propertyDTO,property);
                propertyDAO.save(property);
                return property;
            case 1:
                Property propertyl1 = propertyDAO.get(propertyDTO.getId());
                modelMapper.map(propertyDTO,propertyl1);
                propertyDAO.save(propertyl1);
                return propertyl1;
            case 2:
                PropertyL2 propertyl2 = propertyDAO.getL2(propertyDTO.getId());
                modelMapper.map(propertyDTO,propertyl2);
                propertyDAO.save(propertyl2);
                return propertyl2;
            case 3:
                PropertyL3 propertyl3 = propertyDAO.getL3(propertyDTO.getId());
                modelMapper.map(propertyDTO,propertyl3);
                propertyDAO.save(propertyl3);
                return propertyl3;
            case 4:
                PropertyL4 propertyl4 = propertyDAO.getL4(propertyDTO.getId());
                modelMapper.map(propertyDTO,propertyl4);
                propertyDAO.save(propertyl4);
                return propertyl4;
            case 5:
                PropertyL5 propertyl5 = propertyDAO.getL5(propertyDTO.getId());
                modelMapper.map(propertyDTO,propertyl5);
                propertyDAO.save(propertyl5);
                return propertyl5;
        }
        throw new Exception("Required Parameters Not Present");
    }

    public String deleteMultipleComponents(ComponentDTO componentDTO) throws Exception{
        List<Long> ids = componentDTO.getIds();
        StringBuilder exceptions = new StringBuilder();
        StringBuilder successes = new StringBuilder();
        for (long id: ids) {
            Component component = componentDAO.get(id);
            try {
                componentDAO.delete(component);
                utilService.logEvents(null,log,"Deleted Component With Id "+ component.getId());
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

    public String deleteMultipleProperties(PropertyDTO propertyDTO) throws Exception{
        List<Long> ids = propertyDTO.getIds();
        StringBuilder exceptions = new StringBuilder();
        StringBuilder successes = new StringBuilder();
        for (long id: ids) {
            switch (propertyDTO.getLevel()) {
                case 0:
                    Property property = propertyDAO.get(id);
                    try {
                        propertyDAO.delete(property);
                        utilService.logEvents(null,log,"Deleted Property With Id "+ property.getId());
                        successes.append("Successfully Deleted ").append(id).append("\n");
                    } catch (DataIntegrityViolationException e) {
                        e.printStackTrace();
                        exceptions.append("Unable To Delete id ").append(id).append(" Due To Possible Foreign Key associations").append("\n");
                    }
                    break;
                case 1:
                    Property propertyl1 = propertyDAO.get(id);
                    try {
                        propertyDAO.delete(propertyl1);
                        utilService.logEvents(null,log,"Deleted Property With Id "+ id);
                        successes.append("Successfully Deleted ").append(id).append("\n");
                    } catch (DataIntegrityViolationException e) {
                        e.printStackTrace();
                        exceptions.append("Unable To Delete id ").append(id).append(" Due To Possible Foreign Key associations").append("\n");
                    }
                    break;
                case 2:
                    PropertyL2 propertyl2 = propertyDAO.getL2(id);
                    try {
                        propertyDAO.delete(propertyl2);
                        utilService.logEvents(null,log,"Deleted Property With Id "+ id);
                        successes.append("Successfully Deleted ").append(id).append("\n");
                    } catch (DataIntegrityViolationException e) {
                        e.printStackTrace();
                        exceptions.append("Unable To Delete id ").append(id).append(" Due To Possible Foreign Key associations").append("\n");
                    }
                    break;
                case 3:
                    PropertyL3 propertyl3 = propertyDAO.getL3(id);
                    try {
                        propertyDAO.delete(propertyl3);
                        utilService.logEvents(null,log,"Deleted Property With Id "+ id);
                        successes.append("Successfully Deleted ").append(id).append("\n");
                    } catch (DataIntegrityViolationException e) {
                        e.printStackTrace();
                        exceptions.append("Unable To Delete id ").append(id).append(" Due To Possible Foreign Key associations").append("\n");
                    }
                    break;
                case 4:
                    PropertyL4 propertyl4 = propertyDAO.getL4(id);
                    try {
                        propertyDAO.delete(propertyl4);
                        utilService.logEvents(null,log,"Deleted Property With Id "+ id);
                        successes.append("Successfully Deleted ").append(id).append("\n");
                    } catch (DataIntegrityViolationException e) {
                        e.printStackTrace();
                        exceptions.append("Unable To Delete id ").append(id).append(" Due To Possible Foreign Key associations").append("\n");
                    }
                    break;
                case 5:
                    PropertyL5 propertyl5 = propertyDAO.getL5(id);
                    try {
                        propertyDAO.delete(propertyl5);
                        utilService.logEvents(null,log,"Deleted Property With Id "+ id);
                        successes.append("Successfully Deleted ").append(id).append("\n");
                    } catch (DataIntegrityViolationException e) {
                        e.printStackTrace();
                        exceptions.append("Unable To Delete id ").append(id).append(" Due To Possible Foreign Key associations").append("\n");
                    }
                    break;
            }
        }

        if(exceptions.toString().length()>0)
            throw new Exception(exceptions.toString());

        return successes.toString();
    }

    public SearchResultDTO searchComponents(SearchRequestDTO searchRequestDTO) {
        SearchResultDTO searchResultDTO = componentDAO.search(searchRequestDTO);
        return searchResultDTO;
    }

}
