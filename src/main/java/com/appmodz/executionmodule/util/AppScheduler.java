package com.appmodz.executionmodule.util;

import com.appmodz.executionmodule.dao.ApplicationLogDAO;
import com.appmodz.executionmodule.model.ApplicationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@EnableScheduling
public class AppScheduler {

    @Autowired
    ApplicationLogDAO applicationLogDAO;

    @Scheduled(cron = "0 0 23 * * *")
    public void delete7dayOldEntries() throws Exception {
        applicationLogDAO.deleteBefore7Days();
        ApplicationLog applicationLog = new ApplicationLog();
        applicationLog.setLog("Ran Function To Delete Logs Before 7 Days From Now");
        applicationLog.setTime(new Date());
        applicationLogDAO.save(applicationLog);
    }
}
