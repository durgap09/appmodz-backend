package com.appmodz.executionmodule.controller.v1;

import com.appmodz.executionmodule.dao.*;
import com.appmodz.executionmodule.dto.AuthRequestDTO;
import com.appmodz.executionmodule.dto.AuthResponseDTO;
import com.appmodz.executionmodule.dto.ResponseDTO;
import com.appmodz.executionmodule.dto.UserRequestDTO;
import com.appmodz.executionmodule.model.*;
import com.appmodz.executionmodule.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController("v1AuthController")
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtilService jwtTokenUtil;

    @Autowired
    private UserOrganizationDAO userOrganizationDAO;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UtilService utilService;

    @Autowired
    private RolePermissionDAO rolePermissionDAO;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TenDukeService tenDukeService;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private RoleService roleService;

    @Autowired
    private OrganizationDAO organizationDAO;

    @Autowired
    PulumiService pulumiService;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handle(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();
        ResponseDTO responseDTO = new ResponseDTO("failure",ex.getMessage(),null);
        return responseDTO;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequestDTO authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
                            authenticationRequest.getPassword())
            );
        }
        catch (Exception e) {
            throw new Exception("Incorrect username or password", e);
        }
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);
        User user = userDAO.getByUsername(authenticationRequest.getUsername());
        List<UserOrganization> userOrganizations = userOrganizationDAO.getUserOrganizations(user.getUserId());
        AuthResponseDTO authenticationResponse = new AuthResponseDTO();
        authenticationResponse.setJwt(jwt);
        authenticationResponse.setUser(user);
        authenticationResponse.setUserOrganizations(userOrganizations);
        utilService.logEvents(user.getUserName(),log,"Logged In");
        return ResponseEntity.ok(new ResponseDTO("success", null, authenticationResponse));
    }

    @RequestMapping(value = "/tenDuke", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationTokenTenDuke(@RequestBody AuthRequestDTO authenticationRequest) throws Exception {
        Map<String,Object> payload = utilService.getPayloadFromJwt(authenticationRequest.getIdToken());
        String username = payload.get("email").toString();
        String name = payload.get("given_name").toString();
        String familyName = payload.get("family_name").toString();
        try {
            UserDetails userDetails = userDetailsService
                    .loadUserByUsername(username);
        } catch (Exception e) {
            if(e.getMessage().equals("User Not Found")) {
                User user = new User();
                user.setUserName(username);
                user.setUserFirstName(name);
                user.setUserLastName(familyName);
                Role role = roleDAO.getDefaultRoleByName("OrgAdmin");
                Organization organization = organizationDAO.get("common");
                if(organization==null) {
                    organization = new Organization();
                    organization.setOrganizationName("common");
                    organizationDAO.save(organization);
                }
                if(role==null) {
                    role = roleService.createOrgAdminRole();
                }
                userDAO.save(user);
                UserOrganization userOrganization = new UserOrganization();
                userOrganization.setUser(user);
                userOrganization.setRole(role);
                userOrganization.setOrganization(organization);
                userOrganizationDAO.save(userOrganization);
            } else
                throw e;
        }
        UserDetails userDetails = userDetailsService
                .loadUserByUsername(username);
        final String jwt = jwtTokenUtil.generateToken(userDetails);
        User user = userDAO.getByUsername(username);
        List<UserOrganization> userOrganizations = userOrganizationDAO.getUserOrganizations(user.getUserId());
        AuthResponseDTO authenticationResponse = new AuthResponseDTO();
        authenticationResponse.setJwt(jwt);
        authenticationResponse.setUser(user);
        authenticationResponse.setUserOrganizations(userOrganizations);
        authenticationResponse.setTenDukeResponse(
                tenDukeService.verifyLicense(authenticationRequest.getAccessToken(),user));
        utilService.logEvents(user.getUserName(),log,"Logged In");
        return ResponseEntity.ok(new ResponseDTO("success", null, authenticationResponse));
    }

    @RequestMapping(value = "/verify-ten-duke-license-with-id-token", method = RequestMethod.POST)
    public ResponseEntity<?> verifyTenDukeLicenseWithIdToken(@RequestBody AuthRequestDTO authenticationRequest) throws Exception {
        Map<String,Object> payload = utilService.getPayloadFromJwt(authenticationRequest.getIdToken());
        String username = payload.get("email").toString();
        User user = userDAO.getByUsername(username);
        return ResponseEntity.ok(new ResponseDTO("success", null, tenDukeService.verifyLicense(authenticationRequest.getAccessToken(),user)));
    }

    @RequestMapping(value = "/verify-ten-duke-license", method = RequestMethod.POST)
    public ResponseEntity<?> verifyTenDukeLicense(@RequestBody AuthRequestDTO authenticationRequest) throws Exception {
        User user = utilService.getAuthenticatedUser();
        return ResponseEntity.ok(new ResponseDTO("success", null, tenDukeService.verifyLicense(authenticationRequest.getAccessToken(),user)));
    }

    @RequestMapping(value = "/orgList", method = RequestMethod.POST)
    public ResponseEntity<?> getOrgList(@RequestBody AuthRequestDTO authenticationRequest) throws Exception {
        User user = utilService.getAuthenticatedUser();
        List<UserOrganization> userOrganizations = userOrganizationDAO.getUserOrganizations(user.getUserId());
        AuthResponseDTO authenticationResponse = new AuthResponseDTO();
        authenticationResponse.setUser(user);
        authenticationResponse.setUserOrganizations(userOrganizations);
        utilService.logEvents(user.getUserName(),log,"Fetched Org List");
        return ResponseEntity.ok(new ResponseDTO("success", null, authenticationResponse));
    }


    @RequestMapping(value = "/check-first-user", method = RequestMethod.POST)
    public ResponseEntity<?> checkFirstUser() throws Exception {
        User user = userDAO.get();
        if(user==null) {
            return ResponseEntity.ok(new ResponseDTO("success", null, null));
        } else
            return ResponseEntity.ok(new ResponseDTO("failure", "user already exists", null));
    }

    @RequestMapping(value = "/create-first-user", method = RequestMethod.POST)
    public ResponseEntity<?> createFirstUser(@RequestBody UserRequestDTO userRequestDTO) throws Exception {
        return ResponseEntity.ok(new ResponseDTO("success", null,  userService.createFirst(userRequestDTO)));
    }


    @RequestMapping(value = "/forgot-password", method = RequestMethod.POST)
    public ResponseEntity<?> forgotPassword(@RequestBody AuthRequestDTO authenticationRequest) throws Exception {
        User user = userDAO.getByUsername(authenticationRequest.getUsername());
        if(user==null) {
            throw new Exception("User with this email doesnt exist");
        }
        userService.sendForgotMail(user);
        return ResponseEntity.ok(new ResponseDTO("success", null, null));
    }

    @RequestMapping(value = "/change-password/{token}", method = RequestMethod.POST)
    public ResponseEntity<?> changePassword(@PathVariable String token,@RequestBody AuthRequestDTO authenticationRequest) throws Exception {
        final String password = "I AM SHERLOCKED";
        final String salt = "5bf0e4ab0244ed8585b6a0b0990b5edb";
        TextEncryptor decryptor = Encryptors.text(password, salt);
        String decryptedText = decryptor.decrypt(token);
        long userId = Long.parseLong(decryptedText);
        User user = userDAO.get(userId);
        if(user.getPasswordChange()!=null&&user.getPasswordChange()&&user.getPasswordLinkExpiry().after(new Date())) {
            user.setUserPasswordHash(passwordEncoder.encode(authenticationRequest.getPassword()));
            user.setPasswordChange(false);
            userDAO.save(user);
        } else {
            throw new Exception("Link invalid or expired");
        }
        utilService.logEvents(user.getUserName(),log,"Changed Password");
        return ResponseEntity.ok(new ResponseDTO("success", null, null));
    }

    @RequestMapping(value = "/org", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<?> loginOrg(@RequestBody AuthRequestDTO authenticationRequest) throws Exception {
        User user = utilService.getAuthenticatedUser();
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());
        Role role = userOrganizationDAO.getRole(authenticationRequest.getUserOrganizationId());
        RolePermissions rolePermissions =  rolePermissionDAO.getByRoleId(role.getRoleId());
        final String jwt = jwtTokenUtil.generateToken(userDetails,authenticationRequest.getUserOrganizationId());
        AuthResponseDTO authenticationResponse = new AuthResponseDTO();
        authenticationResponse.setJwt(jwt);
        authenticationResponse.setRoleName(role.getRoleName());
        authenticationResponse.setUser(user);
        authenticationResponse.setPermissions(rolePermissions.getPermissions());
        License license = user.getUserLicense();
        if(license!=null) {
            authenticationResponse.setLicenseName(user.getUserLicense().getLicenseName());
            List<LicensePermission> licensePermissions = license.getLicensePermissions();
            System.out.println(licensePermissions.size());
            authenticationResponse.setLicensePermissions(licensePermissions);
        }
        utilService.logEvents(user.getUserName(),log,"Switched Organization");
        return ResponseEntity.ok(new ResponseDTO("success", null, authenticationResponse));
    }

    @RequestMapping(value = "/change-pulumi-token", method = RequestMethod.POST)
    public Object changePulumiToken(@RequestBody AuthRequestDTO authenticationRequest) throws Exception {
        return  pulumiService.pulumiSetKey(authenticationRequest.getToken());
    }

    @RequestMapping(value = "/pulumi-whoami", method = RequestMethod.POST)
    public Object pulumiWhoAmI() throws Exception {
        return pulumiService.pulumiWhoAmI();
    }
}
