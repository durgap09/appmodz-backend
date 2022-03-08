package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.LicenseDAO;
import com.appmodz.executionmodule.dto.PermissionDTO;
import com.appmodz.executionmodule.model.License;
import com.appmodz.executionmodule.model.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LicenseService {

    @Autowired
    private Environment env;

    @Autowired
    private LicenseDAO licenseDAO;

    @Autowired
    private UtilService utilService;

    public List listLicenses() {
        List<License> licenses = licenseDAO.getAll();
        utilService.logEvents(null,log,"Listed Licenses");
        return licenses;
    }

}
