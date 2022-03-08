package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.LicenseDAO;
import com.appmodz.executionmodule.dao.ProductPackageDAO;
import com.appmodz.executionmodule.dao.UserDAO;
import com.appmodz.executionmodule.dto.TenDukeAddressDTO;
import com.appmodz.executionmodule.dto.TenDukeUserRequestDTO;
import com.appmodz.executionmodule.dto.UserRequestDTO;
import com.appmodz.executionmodule.model.License;
import com.appmodz.executionmodule.model.ProductPackage;
import com.appmodz.executionmodule.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TenDukeService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    UserDAO userDAO;

    @Autowired
    LicenseDAO licenseDAO;

    @Autowired
    ProductPackageDAO productPackageDAO;


    public Object verifyLicense(String accessToken, User user) throws Exception{
        List<License> licenses = licenseDAO.getByRankDesc();
        for(License license:licenses) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer "+accessToken);
            String urlTemplate = UriComponentsBuilder.fromHttpUrl("https://appmodz-evaluation.10duke.net/authz/.json")
                    .queryParam(license.getLicenseName(), "")
                    .queryParam("consumptionMode", "cache")
                    .queryParam("hw", "1")
                    .encode()
                    .toUriString();
            System.out.println(urlTemplate);
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            HttpEntity<String> response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            String responseBody = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseJson = objectMapper.readValue(responseBody, Map.class);
            if(responseJson.get(license.getLicenseName()+"_errorMessage")==null) {
                user.setUserLicense(license);
                userDAO.save(user);
                return responseJson;
            }
        }
        throw new Exception("None of the licenses valid for this user");
    }

    public String createNewUser(String accessToken,User user) throws Exception{
        TenDukeUserRequestDTO tenDukeUserRequestDTO = new TenDukeUserRequestDTO();
        TenDukeAddressDTO addressDTO = new TenDukeAddressDTO();

        UUID addressUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        addressDTO.setId(addressUUID.toString());
        addressDTO.setCountry(user.getUserCountry());
        addressDTO.setStreetAddress(user.getUserAddress1());

        tenDukeUserRequestDTO.setFirstName(user.getUserFirstName());
        tenDukeUserRequestDTO.setLastName(user.getUserLastName());
        tenDukeUserRequestDTO.setEmail(user.getUserEmail());
        tenDukeUserRequestDTO.setPhoneNumber(user.getUserPhoneNumber());
        tenDukeUserRequestDTO.setId(userUUID.toString());
        tenDukeUserRequestDTO.setAddress(addressDTO);

        String reqUrl = "https://appmodz-evaluation.10duke.net:443/api/idp/v1.exp/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(tenDukeUserRequestDTO.toString(), headers);
        String result = restTemplate.postForObject(reqUrl, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode response = mapper.readTree(result);
        String userId = response.get("id").textValue();
        return userId;
    }

    public String createNewUser(String accessToken, UserRequestDTO userRequestDTO) throws Exception{
        TenDukeUserRequestDTO tenDukeUserRequestDTO = new TenDukeUserRequestDTO();
        TenDukeAddressDTO addressDTO = new TenDukeAddressDTO();

        UUID addressUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        addressDTO.setId(addressUUID.toString());
        addressDTO.setCountry(userRequestDTO.getCountry());
        addressDTO.setStreetAddress(userRequestDTO.getAddress1());

        tenDukeUserRequestDTO.setFirstName(userRequestDTO.getFirstName());
        tenDukeUserRequestDTO.setLastName(userRequestDTO.getLastName());
        tenDukeUserRequestDTO.setEmail(userRequestDTO.getUserName());
        tenDukeUserRequestDTO.setPhoneNumber(userRequestDTO.getPhoneNumber());
        tenDukeUserRequestDTO.setId(userUUID.toString());
        tenDukeUserRequestDTO.setAddress(addressDTO);

        String reqUrl = "https://appmodz-evaluation.10duke.net:443/api/idp/v1.exp/users";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer "+accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(tenDukeUserRequestDTO.toString(), headers);
        String result = restTemplate.postForObject(reqUrl, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode response = mapper.readTree(result);
        String userId = response.get("id").textValue();
        System.out.println(userId);
        return userId;
    }

    public Object attachLicenseToUser(String accessToken,String license,String userId) throws Exception {
        String reqUrl = "https://appmodz-evaluation.10duke.net/graph/ProductPackage[@title='"+license+"']";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer "+accessToken);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("operation","InitializePersonalLicenses");
        map.add("licensedItemCount","1");
        map.add("initializeForProfileId",userId);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        String result = restTemplate.postForObject(reqUrl, entity, String.class);
        System.out.println(result);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode response = mapper.readTree(result);
        if(!response.get("resultCode").textValue().equals("Success"))
            throw new Exception("10Duke Response: "+response.get("resultCode").textValue());
        return result;
    }

    public String getTenDukeUserIdByEmail(String accessToken,String email) throws Exception {
        String reqUrl = "https://appmodz-evaluation.10duke.net/graph/emailAndProfile('"+email+"')";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer "+accessToken);


        HttpEntity<String> entity = new HttpEntity<String>(headers);
        HttpEntity<String> response = restTemplate.exchange(
                reqUrl,
                HttpMethod.GET,
                entity,
                String.class
        );
        String responseBody = response.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        for (JsonNode jsonNode : responseJson) {
            return jsonNode.get("Profile_id").asText();
        }

        throw new Exception("User With this Email Id Not Found On 10duke");
    }

    public Object createUserAndAttachLicense(UserRequestDTO userRequestDTO) throws Exception{
        String accessToken = userRequestDTO.getTenDukeAccessToken();
        String userId = this.createNewUser(accessToken,userRequestDTO);
        ProductPackage productPackage = productPackageDAO.get(userRequestDTO.getProductPackageId());
        return this.attachLicenseToUser(accessToken,productPackage.getProductPackageName(),userId);
    }

}
