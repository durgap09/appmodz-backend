package com.appmodz.executionmodule.controller.v1;

import com.appmodz.executionmodule.dto.ResponseDTO;
import com.appmodz.executionmodule.service.LicenseService;
import com.appmodz.executionmodule.service.ProductPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController("v1ProductPackageController")
@RequestMapping("/v1/product-packages")
public class ProductPackageController {
    @Autowired
    ProductPackageService productPackageService;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handle(Exception ex, HttpServletRequest request) {
        ResponseDTO responseDTO = new ResponseDTO("failure",ex.getMessage(),null);
        return responseDTO;
    }

    @RequestMapping(value="/",method= RequestMethod.GET)
    @ResponseBody
    public Object getProductPackages(@RequestParam(required = false) String format,
                              @RequestParam(required = false) List<Long> ids,
                              HttpServletResponse response)
            throws Exception{
        return new ResponseDTO("success",null,
                productPackageService.listProductPackages());
    }
}
