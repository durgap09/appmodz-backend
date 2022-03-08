package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.*;
import com.appmodz.executionmodule.dto.*;
import com.appmodz.executionmodule.model.*;
import com.appmodz.executionmodule.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    @Autowired
    UserDAO userDAO;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TemplateDAO templateDAO;

    @Autowired
    UserOrganizationDAO userOrganizationDAO;

    @Autowired
    OrganizationDAO organizationDAO;

    @Autowired
    RoleDAO roleDAO;

    @Autowired
    UtilService utilService;

    @Autowired
    MailService mailService;

    @Autowired
    RoleService roleService;

    @Autowired
    TenDukeService tenDukeService;

    @Autowired
    LicenseDAO licenseDAO;

    @Autowired
    ProductPackageDAO productPackageDAO;

    public UserResponseDTO getUserById(long userId) throws Exception{
        User user = userDAO.get(userId);
        if(user==null)
            throw new Exception("No user with this user id exists");
        else {
            if(!utilService.checkPermission(

                    PermissionDTO.builder()
                            .organizationIds(userOrganizationDAO.getOrganizationIds(user.getUserId()))
                            .userId(userId).build()

                    ,"GET_USER"))
                throw new Exception("GET USER ACTION NOT PERMITTED FOR THIS USER");
        }
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUser(user);
        userResponseDTO.setUserOrganizations(userOrganizationDAO.getUserOrganizations(user.getUserId()));
        utilService.logEvents(null,log,"Fetched User With Username "+ user.getUserName());
        return userResponseDTO;
    }

    public User getUserByUsername(String userName) throws Exception{
        User user = userDAO.getByUsername(userName);
        if(user==null)
            throw new Exception("No user with this user id exists");
        else {
            //utilService.logEvents(null,log,"Fetched User With UserName "+ userName);
            return user;
        }
    }

    public User createUser(UserRequestDTO userRequestDTO) throws Exception{
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(userRequestDTO.getOrganizationIds())
                        .build(),"CREATE_USER"))
            throw new Exception("CREATE USER ACTION NOT PERMITTED FOR THIS USER");
        User user = new User();
        user.setUserFirstName(userRequestDTO.getFirstName());
        user.setUserLastName(userRequestDTO.getLastName());
        user.setUserName(userRequestDTO.getUserName());
        user.setUserPhoneNumber(userRequestDTO.getPhoneNumber());
        user.setUserAddress1(userRequestDTO.getAddress1());
        user.setUserAddress2(userRequestDTO.getAddress2());
        user.setUserCountry(userRequestDTO.getCountry());
        user.setUserCountryCode(userRequestDTO.getCountryCode());
        user.setUserEmail(userRequestDTO.getEmailId());
        User checkUser = userDAO.getByUsername(userRequestDTO.getUserName());
        if (checkUser!=null)
            throw new Exception("User with this username already exists");
        user.setUserPasswordHash(passwordEncoder.encode(userRequestDTO.getPassword()));
        userDAO.save(user);

        List<Long> organizationIds = userRequestDTO.getOrganizationIds();
        List<Long> roleIds = userRequestDTO.getRoleIds();

        if(organizationIds.size() != roleIds.size())
            throw new Exception("Size of Organization And Role doesnt match");


        for(int i=0;i<organizationIds.size();i++) {
            Organization organization = organizationDAO.get(organizationIds.get(i));
            if (organization==null)
                throw new Exception("Organization with this id not found "+organizationIds.get(i));
            Role role = roleDAO.get(roleIds.get(i));
            if (role==null)
                throw new Exception("Role with this id not found "+roleIds.get(i));

            UserOrganization userOrganization = new UserOrganization();
            userOrganization.setUser(user);
            userOrganization.setRole(role);
            userOrganization.setOrganization(organization);
            userOrganizationDAO.save(userOrganization);
        }
        utilService.logEvents(null,log,"Created User With Username "+ userRequestDTO.getUserName());
        return user;
    }


    public User createFirst(UserRequestDTO userRequestDTO) throws Exception{
        User user = new User();
        user.setUserFirstName(userRequestDTO.getFirstName());
        user.setUserLastName(userRequestDTO.getLastName());
        user.setUserName(userRequestDTO.getUserName());
        user.setUserPhoneNumber(userRequestDTO.getPhoneNumber());
        user.setUserAddress1(userRequestDTO.getAddress1());
        user.setUserAddress2(userRequestDTO.getAddress2());
        user.setUserCountry(userRequestDTO.getCountry());
        user.setUserCountryCode(userRequestDTO.getCountryCode());
        user.setUserEmail(userRequestDTO.getEmailId());
        User checkUser = userDAO.getByUsername(userRequestDTO.getUserName());
        if (checkUser!=null)
            throw new Exception("User with this username already exists");
        //user.setUserPasswordHash(passwordEncoder.encode(userRequestDTO.getPassword()));
        userDAO.save(user);

        UserOrganization userOrganization = new UserOrganization();
        Role role = roleService.createSuperAdminRole();
        roleService.createOrgAdminRole();
        Organization organization = new Organization();
        organization.setOrganizationName("Common");
        organizationDAO.save(organization);
        userOrganization.setUser(user);
        userOrganization.setRole(role);
        userOrganization.setOrganization(organization);
        userOrganizationDAO.save(userOrganization);
        this.sendRegistrationMail(user);
        return user;
    }


    public User inviteUser(UserRequestDTO userRequestDTO) throws Exception{
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(userRequestDTO.getOrganizationIds())
                        .build(),"CREATE_USER"))
            throw new Exception("CREATE USER ACTION NOT PERMITTED FOR THIS USER");
        User user = new User();
        user.setUserFirstName(userRequestDTO.getFirstName());
        user.setUserLastName(userRequestDTO.getLastName());
        user.setUserName(userRequestDTO.getUserName());
        user.setUserPhoneNumber(userRequestDTO.getPhoneNumber());
        user.setUserAddress1(userRequestDTO.getAddress1());
        user.setUserAddress2(userRequestDTO.getAddress2());
        user.setUserCountry(userRequestDTO.getCountry());
        user.setUserCountryCode(userRequestDTO.getCountryCode());
        user.setUserEmail(userRequestDTO.getEmailId());
        User checkUser = userDAO.getByUsername(userRequestDTO.getUserName());
        if (checkUser!=null)
            throw new Exception("User with this username already exists");
        boolean sendEmail=true;
        if(userRequestDTO.getTenDukeAccessToken()!=null&&userRequestDTO.getProductPackageId()!=null) {
            sendEmail = false;
            ProductPackage productPackage = productPackageDAO.get(userRequestDTO.getProductPackageId());
            if(productPackage==null)
                throw new Exception("Product package with this id not found");
            tenDukeService.createUserAndAttachLicense(userRequestDTO);
            user.setProductPackage(productPackage);
        }
        userDAO.save(user);

        List<Long> organizationIds = userRequestDTO.getOrganizationIds();
        List<Long> roleIds = userRequestDTO.getRoleIds();

        if(organizationIds.size() != roleIds.size())
            throw new Exception("Size of Organization And Role doesnt match");


        for(int i=0;i<organizationIds.size();i++) {
            Organization organization = organizationDAO.get(organizationIds.get(i));
            if (organization==null)
                throw new Exception("Organization with this id not found " + organizationIds.get(i));
            Role role = roleDAO.get(roleIds.get(i));
            if (role==null)
                throw new Exception("Role with this id not found "+ roleIds.get(i));

            UserOrganization userOrganization = new UserOrganization();
            userOrganization.setUser(user);
            userOrganization.setRole(role);
            userOrganization.setOrganization(organization);
            userOrganizationDAO.save(userOrganization);
        }
        if(sendEmail)
            this.sendRegistrationMail(user);
        utilService.logEvents(null,log,"Invited User With Username "+ userRequestDTO.getUserName());
        return user;
    }

    public User updateUser(UserRequestDTO userRequestDTO) throws Exception{
        User user = userDAO.get(userRequestDTO.getId());
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(userOrganizationDAO.getOrganizationIds(user.getUserId()))
                        .userId(user.getUserId()).build()
                ,"UPDATE_USER"))
            throw new Exception("UPDATE USER ACTION NOT PERMITTED FOR THIS USER");
        if(userRequestDTO.getFirstName()!=null)
        user.setUserFirstName(userRequestDTO.getFirstName());
        if(userRequestDTO.getLastName()!=null)
        user.setUserLastName(userRequestDTO.getLastName());
        if(userRequestDTO.getUserName()!=null) {
            User checkUser = userDAO.getByUsername(userRequestDTO.getUserName());
            if (checkUser != null)
                throw new Exception("User with this username already exists");
            user.setUserName(userRequestDTO.getUserName());
        }
        if(userRequestDTO.getPassword()!=null)
        user.setUserPasswordHash(passwordEncoder.encode(userRequestDTO.getPassword()));
        if(userRequestDTO.getPhoneNumber()!=null)
        user.setUserPhoneNumber(userRequestDTO.getPhoneNumber());
        if(userRequestDTO.getAddress1()!=null)
        user.setUserAddress1(userRequestDTO.getAddress1());
        if(userRequestDTO.getAddress2()!=null)
        user.setUserAddress2(userRequestDTO.getAddress2());
        if(userRequestDTO.getCountry()!=null)
        user.setUserCountry(userRequestDTO.getCountry());
        if(userRequestDTO.getCountryCode()!=null)
        user.setUserCountryCode(userRequestDTO.getCountryCode());
        if (userRequestDTO.getEmailId()!=null)
        user.setUserEmail(userRequestDTO.getEmailId());

        if(userRequestDTO.getTenDukeAccessToken()!=null&&userRequestDTO.getProductPackageId()!=null) {
            ProductPackage productPackage = productPackageDAO.get(userRequestDTO.getProductPackageId());
            if(productPackage==null)
                throw new Exception("Product package with this id not found");
            String userId = tenDukeService.getTenDukeUserIdByEmail(userRequestDTO.getTenDukeAccessToken(),user.getUserName());
            tenDukeService.attachLicenseToUser(userRequestDTO.getTenDukeAccessToken(),productPackage.getProductPackageName(),userId);
            user.setProductPackage(productPackage);
        }

        userDAO.save(user);

        if(userRequestDTO.getOrganizationIds()!=null&&userRequestDTO.getRoleIds()!=null) {

            if(userRequestDTO.getOrganizationIds().size() != userRequestDTO.getRoleIds().size())
                throw new Exception("Size of Organization And Role doesnt match");

            List<UserOrganization> currentUserOrganizations = userOrganizationDAO.getUserOrganizations(user.getUserId());
            for(UserOrganization u: currentUserOrganizations) {
                long orgId = u.getOrganization().getOrganizationId();
                long roleId = u.getRole().getRoleId();
                int ind = userRequestDTO.getOrganizationIds().indexOf(orgId);
                if(ind!=-1) {
                    if (userRequestDTO.getRoleIds().get(ind) != roleId) {
                        Role role = roleDAO.get(userRequestDTO.getRoleIds().get(ind));
                        if (role==null)
                            throw new Exception("Role with this id not found "+ userRequestDTO.getRoleIds().get(ind));
                        u.setRole(role);
                        userOrganizationDAO.save(u);
                    }
                    userRequestDTO.getOrganizationIds().remove(ind);
                    userRequestDTO.getRoleIds().remove(ind);
                }
                else {
                    userOrganizationDAO.deleteByUserOrganizationId(u.getUserOrganizationId());
                }
            }

            List<Long> organizationIds = userRequestDTO.getOrganizationIds();
            List<Long> roleIds = userRequestDTO.getRoleIds();

            for(int i=0;i<organizationIds.size();i++) {
                Organization organization = organizationDAO.get(organizationIds.get(i));
                if (organization==null)
                    throw new Exception("Organization with this id not found "+organizationIds.get(i));
                Role role = roleDAO.get(roleIds.get(i));
                if (role==null)
                    throw new Exception("Role with this id not found "+roleIds.get(i));

                UserOrganization userOrganization = new UserOrganization();
                userOrganization.setUser(user);
                userOrganization.setRole(role);
                userOrganization.setOrganization(organization);
                userOrganizationDAO.save(userOrganization);
            }
        }
        utilService.logEvents(null,log,"Updated User With Username "+ user.getUserName());
        return user;
    }

    public void deleteUser(Long id) throws Exception {
        User user = userDAO.get(id);
        if(!utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(userOrganizationDAO.getOrganizationIds(user.getUserId()))
                        .userId(user.getUserId()).build()
                ,"DELETE_USER"))
            throw new Exception("DELETE USER ACTION NOT PERMITTED FOR THIS USER");
        UserOrganization authUserOrganization = utilService.getUserOrganization();
        List<Long> organizationIds = userOrganizationDAO.getOrganizationIds(user.getUserId());
        if(utilService.getPermissionScope("DELETE_USER")!=null&&utilService.getPermissionScope("DELETE_USER").equals("GLOBAL")) {
            templateDAO.updateUserToNull(user.getUserId());
            userOrganizationDAO.deleteByUserId(user.getUserId());
            userDAO.delete(user);
            utilService.logEvents(null,log,"Deleted User With Username "+user.getUserName());
        } else if(organizationIds.contains(authUserOrganization.getOrganization().getOrganizationId())) {
            userOrganizationDAO.deleteByUserIdAndOrganizationId(user.getUserId(),
                    authUserOrganization.getOrganization().getOrganizationId());
            utilService.logEvents(null,log,"Removed User With Username "+user.getUserName()
                    +" From Organization "+authUserOrganization.getOrganization().getOrganizationId()+
                    " "+authUserOrganization.getOrganization().getOrganizationName());
            if(organizationIds.size()==1) {
                templateDAO.updateUserToNull(user.getUserId());
                userDAO.delete(user);
                utilService.logEvents(null,log,"Deleted User With Username "+user.getUserName());
            }
        }
    }

    public String deleteMultipleUsers(UserRequestDTO userRequestDTO) throws Exception{
        List<Long> ids = userRequestDTO.getIds();
        StringBuilder exceptions = new StringBuilder();
        StringBuilder successes = new StringBuilder();
        UserOrganization authUserOrganization = utilService.getUserOrganization();
        for (long id: ids) {
            User user = userDAO.get(id);
            if(!utilService.checkPermission(
                    PermissionDTO.builder()
                            .organizationIds(userOrganizationDAO.getOrganizationIds(user.getUserId()))
                            .userId(user.getUserId()).build()
                    ,"DELETE_USER"))
                exceptions.append("DELETE USER ACTION NOT PERMITTED FOR THIS USER FOR ID").append(id);
            try {
                List<Long> organizationIds = userOrganizationDAO.getOrganizationIds(user.getUserId());
                if(utilService.getPermissionScope("DELETE_USER")!=null&&utilService.getPermissionScope("DELETE_USER").equals("GLOBAL")) {
                    userOrganizationDAO.deleteByUserId(user.getUserId());
                    templateDAO.updateUserToNull(user.getUserId());
                    userDAO.delete(user);
                    utilService.logEvents(null,log,"Deleted User With Username "+user.getUserName());
                    successes.append("Successfully Completely Deleted User ").append(id).append("\n");
                }else if(organizationIds.contains(authUserOrganization.getOrganization().getOrganizationId())) {
                    userOrganizationDAO.deleteByUserIdAndOrganizationId(user.getUserId(),authUserOrganization.getOrganization().getOrganizationId());
                    utilService.logEvents(null,log,"Removed User With Username "+user.getUserName()
                            +" From Organization "+authUserOrganization.getOrganization().getOrganizationId()+
                            " "+authUserOrganization.getOrganization().getOrganizationName());
                    if(organizationIds.size()==1) {
                        templateDAO.updateUserToNull(user.getUserId());
                        userDAO.delete(user);
                        utilService.logEvents(null,log,"Deleted User With Username "+user.getUserName());
                        successes.append("Successfully Completely Deleted User ").append(id).append("\n");
                    } else {
                        successes.append("Successfully Removed User ").append(id).append("\n");
                    }
                }

            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                exceptions.append("Unable To Delete id ").append(id).append(" Due To Possible Foreign Key associations").append("\n");
            }
        }

        if(exceptions.toString().length()>0)
            throw new Exception(exceptions.toString());

        return successes.toString();
    }

    public List listUsers() throws Exception{
        List<User> users = userDAO.getAll();
        List<UserResponseDTO> userResponseDTOS = this.transformUserList(users);
        userResponseDTOS = userResponseDTOS.stream().filter(u->utilService.checkPermission(
                PermissionDTO.builder()
                        .organizationIds(userOrganizationDAO.getOrganizationIds(u.getUser().getUserId()))
                        .userId(u.getUser().getUserId()).build(),
                "GET_USER")).collect(Collectors.toList());
        utilService.logEvents(null,log,"Listed Users");
        return userResponseDTOS;
    }

    private List transformUserList(List users) {
        List<UserResponseDTO> userResponseDTOS = new ArrayList<>();
        User authUser = utilService.getAuthenticatedUser();
        for(int i=0; i<users.size(); i++) {
            User user = (User) users.get(i);
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            userResponseDTO.setUser(user);
            userResponseDTO.setUserOrganizations(userOrganizationDAO.getUserOrganizations(user.getUserId()));
            if(user.getUserId()==authUser.getUserId())
                userResponseDTO.setIsEditable(false);
            else
                userResponseDTO.setIsEditable(true);
            userResponseDTOS.add(userResponseDTO);
        }
        return userResponseDTOS;
    }

    public SearchResultDTO searchUsers(SearchRequestDTO searchRequestDTO) {
        SearchResultDTO users = userDAO.search(searchRequestDTO);
        List<UserResponseDTO> userResponseDTOS = this.transformUserList(users.getData());
        users.setData(userResponseDTOS.stream()
                .filter(u->utilService.checkPermission(
                        PermissionDTO.builder()
                                .organizationIds(
                                        userOrganizationDAO.getOrganizationIds(u.getUser().getUserId()))
                                .userId(u.getUser().getUserId()).build()
                      ,
                        "GET_USER")).collect(Collectors.toList()));
        utilService.logEvents(null,log,"Searched Users");
        return users;
    }

    public void importUsers(MultipartFile multipartFile) throws Exception {
        ExcelUtil excelUtil = new ExcelUtil(multipartFile.getInputStream());
        Object[][] data = excelUtil.readSheet("Users");
        for(int i=1;i< data.length;i++) {
            User user = new User();
            UserOrganization userOrganization = new UserOrganization();
            for(int j=0;j<data[i].length;j++) {
                Object obj = data[i][j];
                switch (j) {
                    case 0:
                        if(obj!=null) {
                            user = userDAO.get((Long) obj);
                            if(user==null) {
                                throw new Exception("No user with this id found "+obj);
                            }
                        }
                        break;
                    case 1:
                        if(obj!=null) {
                            user.setUserName((String)obj);
                        }
                        break;
                    case 2:
                        if(obj!=null) {
                            user.setUserFirstName((String)obj);
                        }
                        break;
                    case 3:
                        if(obj!=null) {
                            user.setUserLastName((String)obj);
                        }
                        break;
                    case 4:
                        if(obj!=null) {
                            user.setUserCountry((String)obj);
                        }
                        break;
                    case 5:
                        if(obj!=null) {
                            user.setUserCountryCode((String)obj);
                        }
                        break;
                    case 6:
                        if(obj!=null) {
                            user.setUserPhoneNumber((String)obj);
                        }
                        break;
                    case 7:
                        if(obj!=null) {
                            Organization organization = organizationDAO.get((String) obj);
                            if(organization==null)
                                throw new Exception("Organization with this name not found "+(String) obj);
                            userOrganization.setOrganization(organization);
                        }
                        break;
                    case 8:
                        if(obj!=null) {
                            Role role = roleDAO.get((String)obj);
                            if(role==null)
                                throw new Exception("Role with this name not found "+(String) obj);
                            userOrganization.setRole(role);
                        }
                        break;
                    case 9:
                        if(obj!=null) {
                            user.setUserAddress1((String)obj);
                        }
                        break;
                    case 10:
                        if(obj!=null) {
                            user.setUserAddress2((String)obj);
                        }
                        break;
                }
            }
            if(user.getUserId()!=0L) {
                if(userOrganization.getOrganization()==null)
                    throw new Exception("Organization Name needed for user creation");
                if(userOrganization.getRole()==null)
                    throw new Exception("Role Name needed for user creation");
                if(user.getUserName()==null)
                    throw new Exception("UserName needed for user creation");
                if(!utilService.checkPermission(
                        PermissionDTO.builder()
                                .organizationIds(Collections.singletonList(userOrganization.getOrganization().getOrganizationId()))
                                .build(),"CREATE_USER"))
                    throw new Exception("CREATE USER ACTION NOT PERMITTED FOR THIS USER");
                User checkUser = userDAO.getByUsername(user.getUserName());
                if (checkUser!=null)
                    throw new Exception("User with this username already exists");
                if(!utilService.checkPermission( PermissionDTO.builder().build(), "GET_ROLE"))
                    throw new Exception("GET ROLE PERMISSION NOT ALLOWED FOR USER");
            } else {
                if(userOrganization.getOrganization()==null)
                    throw new Exception("Organization Name needed");
                if(userOrganization.getRole()==null)
                    throw new Exception("Role Name needed");
                if(user.getUserName()==null)
                    throw new Exception("UserName needed");
                if(!utilService.checkPermission( PermissionDTO.builder().build(), "GET_ROLE"))
                    throw new Exception("GET ROLE PERMISSION NOT ALLOWED FOR USER");
                if(!utilService.checkPermission(
                        PermissionDTO.builder()
                                .organizationIds(Collections.singletonList(userOrganization.getOrganization().getOrganizationId()))
                                .build(),"UPDATE_USER"))
                    throw new Exception("UPDATE USER ACTION NOT PERMITTED FOR THIS USER");
                User checkUser = userDAO.getByUsername(user.getUserName());
                if(checkUser!=null&&user.getUserId()!=checkUser.getUserId()) {
                    throw new Exception("User with this username already exists");
                }
            }
            userDAO.save(user);
            userOrganization.setUser(user);
            userOrganizationDAO.save(userOrganization);
        }
        utilService.logEvents(null,log,"Imported Users");
    }

    public void exportUsers(HttpServletResponse response, List<Long> ids) throws Exception {

        ArrayList<ArrayList<Object>> mainList = new ArrayList();
        mainList.add(new ArrayList<>(){{
            add("UserId");
            add("UserName");
            add("UserFirstName");
            add("UserLastName");
            add("UserCountry");
            add("UserCountryCode");
            add("UserPhoneNumber");
            add("UserOrganization");
            add("UserRole");
            add("UserAddress1");
            add("UserAddress2");
            add("UserCreatedOn");
            add("UserUpdatedOn");
        }});
        for(int i=0;i<ids.size();i++) {
            User user = userDAO.get(ids.get(i));
            if(!utilService.checkPermission(

                    PermissionDTO.builder()
                            .organizationIds(userOrganizationDAO.getOrganizationIds(user.getUserId()))
                            .userId(user.getUserId()).build()

                    ,"GET_USER"))
                throw new Exception("GET USER ACTION NOT PERMITTED FOR THIS USER");
            List<UserOrganization> userOrganizations = userOrganizationDAO.getUserOrganizations(user.getUserId());
            for(UserOrganization userOrganization: userOrganizations) {
                mainList.add(new ArrayList<>(){{
                            add(user.getUserId());
                            add(user.getUserName());
                            add(user.getUserFirstName());
                                    add(user.getUserLastName());
                                    add(user.getUserCountry());
                                    add(user.getUserCountryCode());
                                    add(user.getUserPhoneNumber());
                                    add(userOrganization.getOrganization().getOrganizationName());
                                    add(userOrganization.getRole().getRoleName());
                                    add(user.getUserAddress1());
                                    add(user.getUserAddress2());
                                    add(user.getUserCreatedOn());
                                    add(user.getUserUpdatedOn());
                }});
            }
        }
        Object[][] list = mainList.stream().map(u -> u.toArray(new Object[0])).toArray(Object[][]::new);
        ExcelUtil excelUtil = new ExcelUtil();
        XSSFWorkbook xssfWorkbook = excelUtil.createSheet("Users",list);
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        xssfWorkbook.write(outputStream);
        xssfWorkbook.close();
        outputStream.close();
        utilService.logEvents(null,log,"Exported Users with Ids "+ids.toString());
    }

    private Date addHoursToJavaUtilDate(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    private void sendRegistrationMail(User user) throws Exception{
        String input = ""+user.getUserId();
        final String password = "I AM SHERLOCKED";
        final String salt = "5bf0e4ab0244ed8585b6a0b0990b5edb";

        TextEncryptor encryptor = Encryptors.text(password, salt);

        String encryptedText = encryptor.encrypt(input);
        user.setPasswordChange(true);
        user.setPasswordLinkExpiry(this.addHoursToJavaUtilDate(new Date(),24));
        userDAO.save(user);
        mailService.sendRegistrationMail(user.getUserName(),encryptedText);
        utilService.logEvents(user.getUserName(),log,"Sent Registration Mail To User "+user.getUserName());
    }

    public void sendForgotMail(User user) throws Exception{
        String input = ""+user.getUserId();

        final String password = "I AM SHERLOCKED";
        final String salt = "5bf0e4ab0244ed8585b6a0b0990b5edb";

        TextEncryptor encryptor = Encryptors.text(password, salt);

        String encryptedText = encryptor.encrypt(input);

        user.setPasswordChange(true);
        user.setPasswordLinkExpiry(this.addHoursToJavaUtilDate(new Date(),24));
        userDAO.save(user);
        mailService.sendForgotPasswordMail(user.getUserName(),encryptedText);
        utilService.logEvents(user.getUserName(),log,"Sent Forgot Password Mail To User "+user.getUserName());
    }

}
