package com.lgcns.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lgcns.polly.PollyController;

@Component
public class CronScheduler {

    @Autowired
	PollyController controller;
    
    @Scheduled(cron = "0 0 13 * * WED")
    public void printDate() {
        controller.sendPollyMessageV2(null, "all");
    }
}
