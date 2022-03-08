package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.OrganizationDAO;
import com.appmodz.executionmodule.dto.*;
import com.appmodz.executionmodule.model.Organization;
import com.appmodz.executionmodule.model.Workspace;
import com.appmodz.executionmodule.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrganizationService {

    @Autowired
    OrganizationDAO organizationDAO;

    @Autowired
    UtilService utilService;

    public Organization getOrganizationById(long organizationId) throws Exception{
        Organization organization = organizationDAO.get(organizationId);
        if(organization==null)
            throw new Exception("Organization Not Found");
        else if (!utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(Collections.singletonList(organization.getOrganizationId()))
                        .build()
               , "GET_ORGANIZATION"))
            throw new Exception("GET ORGANIZATION ACTION NOT PERMITTED FOR THIS USER");
        utilService.logEvents(null,log,"Fetched Organization With Id "+ organizationId);
        return organizationDAO.get(organizationId);
    }

    public Organization createOrganization(OrganizationRequestDTO organizationRequestDTO) throws Exception{
        if(!utilService.checkLicense("CREATE_ORGANIZATION"))
            throw new Exception("User's License Doesnt Allow This Operation Or Resource Limit Reached For This Operation");
        if (!utilService.checkPermission(PermissionDTO.builder().build(), "CREATE_ORGANIZATION"))
            throw new Exception("CREATE ORGANIZATION ACTION NOT PERMITTED FOR THIS USER");
        Organization organization = new Organization();
        organization.setOrganizationName(organizationRequestDTO.getName());
        organization.setOrganizationDescription(organizationRequestDTO.getDescription());
        organization.setOrganizationTags(organizationRequestDTO.getTags());
        organizationDAO.save(organization);
        utilService.logEvents(null,log,"Created Organization With Id "+ organization.getOrganizationId());
        return organization;
    }

    public Organization updateOrganization(OrganizationRequestDTO organizationRequestDTO) throws Exception {
        Organization organization = organizationDAO.get(organizationRequestDTO.getId());
        if (!utilService.checkPermission(PermissionDTO.builder()
                .organizationIds(Collections.singletonList(organization.getOrganizationId()))
                .build(), "UPDATE_ORGANIZATION"))
            throw new Exception("UPDATE ORGANIZATION ACTION NOT PERMITTED FOR THIS USER");
        if(organizationRequestDTO.getName()!=null)
        organization.setOrganizationName(organizationRequestDTO.getName());
        if(organizationRequestDTO.getDescription()!=null)
            organization.setOrganizationDescription(organizationRequestDTO.getDescription());
        if(organizationRequestDTO.getTags()!=null)
            organization.setOrganizationTags(organizationRequestDTO.getTags());
        organizationDAO.save(organization);
        utilService.logEvents(null,log,"Updated Organization With Id "+ organization.getOrganizationId());
        return organization;
    }

    public void deleteOrganization(long id) throws Exception{
        if (!utilService.checkPermission(PermissionDTO.builder()
                .organizationIds(Collections.singletonList(id))
                .build(), "DELETE_ORGANIZATION"))
            throw new Exception("DELETE ORGANIZATION ACTION NOT PERMITTED FOR THIS USER");
        Organization organization = organizationDAO.get(id);
        organizationDAO.delete(organization);
        utilService.logEvents(null,log,"Deleted Organization With Id "+ organization.getOrganizationId());
    }

    public String deleteMultipleOrganizations(OrganizationRequestDTO organizationRequestDTO) throws Exception{
        List<Long> ids = organizationRequestDTO.getIds();
        StringBuilder exceptions = new StringBuilder();
        StringBuilder successes = new StringBuilder();
        for (long id: ids) {
            Organization organization = organizationDAO.get(id);
            if (!utilService.checkPermission(PermissionDTO.builder()
                    .organizationIds(Collections.singletonList(id))
                    .build(), "DELETE_ORGANIZATION"))
                exceptions.append("DELETE ORGANIZATION ACTION NOT PERMITTED FOR THIS USER FOR ID").append(id);
            try {
                organizationDAO.delete(organization);
                utilService.logEvents(null,log,"Deleted Organization With Id "+ organization.getOrganizationId());
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

    public List listOrganizations() {
        List<Organization> organizations = organizationDAO.getAll();
        organizations =  organizations.stream().filter(o->utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(Collections.singletonList(o.getOrganizationId()))
                        .build(),
                "GET_ORGANIZATION")).collect(Collectors.toList());
        utilService.logEvents(null,log,"Listed Organizations");
        return organizations;
    }

    public SearchResultDTO searchOrganizations(SearchRequestDTO searchRequestDTO) {
        SearchResultDTO searchResultDTO = organizationDAO.search(searchRequestDTO);
        searchResultDTO.setData(
                (List)searchResultDTO.getData().stream().filter(o->utilService.checkPermission(
                        PermissionDTO.builder()
                                .organizationIds(Collections.singletonList(((Organization)o).getOrganizationId()))
                                .build(),
                        "GET_ORGANIZATION")).collect(Collectors.toList())
        );
        utilService.logEvents(null,log,"Searched Organizations");
        return searchResultDTO;
    }

    public void importOrganizations(MultipartFile multipartFile) throws Exception {
        ExcelUtil excelUtil = new ExcelUtil(multipartFile.getInputStream());
        Object[][] data = excelUtil.readSheet("Organizations");
        for(int i=1;i< data.length;i++) {
            Organization organization = new Organization();
            for(int j=0;j<data[i].length;j++) {
                Object obj = data[i][j];
                switch (j) {
                    case 0:
                        if(obj!=null) {
                            organization = organizationDAO.get((Long) obj);
                            if(organization==null) {
                                throw new Exception("No organization with this id found "+obj);
                            }
                        }
                        break;
                    case 1:
                        if(obj!=null) {
                           organization.setOrganizationName((String) obj);
                        }
                        break;
                    case 2:
                        if(obj!=null) {
                            organization.setOrganizationDescription((String) obj);
                        }
                        break;
                    case 3:
                        if(obj!=null) {
                            organization.setOrganizationTags((String) obj);
                        }
                        break;
                }
            }
            if(organization.getOrganizationName()==null)
                throw new Exception("Organization Name needed");
            if(organization.getOrganizationId()==0L) {
                if (!utilService.checkPermission(PermissionDTO.builder().build(), "CREATE_ORGANIZATION"))
                    throw new Exception("CREATE ORGANIZATION ACTION NOT PERMITTED FOR THIS USER");
            } else {
                if (!utilService.checkPermission(PermissionDTO.builder()
                        .organizationIds(Collections.singletonList(organization.getOrganizationId()))
                        .build(), "UPDATE_ORGANIZATION"))
                    throw new Exception("UPDATE ORGANIZATION ACTION NOT PERMITTED FOR THIS USER FOR ID "+organization.getOrganizationId());
            }
            organizationDAO.save(organization);
        }
        utilService.logEvents(null,log,"Imported Organizations");
    }

    public void exportOrganizations(HttpServletResponse response, List<Long> ids) throws Exception {
        Object[][] list = new Object[ids.size()+1][];
        list[0] = new Object[]{"OrganizationId",
                "OrganizationName",
                "OrganizationDescription",
                "OrganizationTags",
                "OrganizationCreatedOn"
                ,"OrganizationUpdatedOn"};
        for(int i=0;i<ids.size();i++) {
            Organization organization = organizationDAO.get(ids.get(i));
            if(!utilService.checkPermission(
                    PermissionDTO.builder()
                            .organizationIds(Collections.singletonList(organization.getOrganizationId()))
                            .build()
                    , "GET_ORGANIZATION"))
            throw new Exception("GET ORGANIZATION ACTION NOT PERMITTED FOR THIS USER");
            list[i+1] = new Object[]{
                    organization.getOrganizationId(),
                    organization.getOrganizationName(),
                    organization.getOrganizationDescription(),
                    organization.getOrganizationTags(),
                    organization.getOrganizationCreatedOn(),
                    organization.getOrganizationUpdatedOn()
            };
        }
        ExcelUtil excelUtil = new ExcelUtil();
        XSSFWorkbook xssfWorkbook = excelUtil.createSheet("Organizations",list);
        response.setHeader("Content-Disposition", "attachment; filename=organizations.xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        xssfWorkbook.write(outputStream);
        xssfWorkbook.close();
        outputStream.close();
        utilService.logEvents(null,log,"Exported Organizations With Ids "+ids.toString());
    }
}
