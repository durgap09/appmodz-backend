package com.appmodz.executionmodule.service;

import com.appmodz.executionmodule.dao.ProductPackageDAO;
import com.appmodz.executionmodule.model.ProductPackage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProductPackageService {
    @Autowired
    private Environment env;

    @Autowired
    private ProductPackageDAO productPackageDAO;

    @Autowired
    private UtilService utilService;

    public List listProductPackages() {
        List<ProductPackage> productPackages = productPackageDAO.getAll();
        utilService.logEvents(null,log,"Listed Product Packages");
        return productPackages;
    }
}
