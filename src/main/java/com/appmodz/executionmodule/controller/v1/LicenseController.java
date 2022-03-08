package com.appmodz.executionmodule.controller.v1;

import com.appmodz.executionmodule.dto.ResponseDTO;
import com.appmodz.executionmodule.service.LicenseService;
import com.appmodz.executionmodule.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController("v1LicenseController")
@RequestMapping("/v1/licenses")
public class LicenseController {

    @Autowired
    LicenseService licenseService;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handle(Exception ex, HttpServletRequest request) {
        ResponseDTO responseDTO = new ResponseDTO("failure",ex.getMessage(),null);
        return responseDTO;
    }

    @RequestMapping(value="/",method= RequestMethod.GET)
    @ResponseBody
    public Object getLicenses(@RequestParam(required = false) String format,
                                   @RequestParam(required = false) List<Long> ids,
                                   HttpServletResponse response)
            throws Exception{
        return new ResponseDTO("success",null,
                licenseService.listLicenses());
    }
}
