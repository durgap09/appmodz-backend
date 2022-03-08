package com.appmodz.executionmodule.controller.v1;

import com.appmodz.executionmodule.dto.ResponseDTO;
import com.appmodz.executionmodule.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("v1AnalyticsController")
@RequestMapping("/v1/analytics")
public class AnalyticsController {

    @Autowired
    AnalyticsService analyticsService;

    @RequestMapping(value="/",method= RequestMethod.GET, produces="application/json")
    @ResponseBody
    public Object getRoles() {
        return new ResponseDTO("success",null,
                analyticsService.getAnalytics());
    }

}
