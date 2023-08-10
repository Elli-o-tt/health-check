package com.lgcns.polly;

import java.util.Date;

import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lgcns.common.MsgCode;

import ch.qos.logback.classic.Logger;

@RestController
public class PollyController {
    
    private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private PollyService pollyService;

    @RequestMapping("/health")
    public ResponseEntity<String> healthCheck(){
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @RequestMapping("/polly/v1")
    public ResponseEntity<String> sendPollyMessageV1(@RequestParam(required = false) @DateTimeFormat(pattern = MsgCode.DATE_FORMAT) Date date, @RequestParam String receiver){
        logger.info("Calling URL : /polly/v1?date=" + date == null ? "null" : date.toString() + "&receiver=" + receiver);
        JSONObject apiResponse = new JSONObject();
        pollyService.sendHealthCheckReport(date, apiResponse, "v1", receiver);
        logger.info("Response about /polly : " + apiResponse.toString());
        return new ResponseEntity<>(apiResponse.toString(), HttpStatus.OK);
    }

    @RequestMapping("/polly/v2")
    public ResponseEntity<String> sendPollyMessageV2(@RequestParam(required = false) @DateTimeFormat(pattern = MsgCode.DATE_FORMAT) Date date, @RequestParam String receiver){
        logger.info("Calling URL : /polly/v2?date=" + date + "&receiver=" + receiver);
        JSONObject apiResponse = new JSONObject();
        pollyService.sendHealthCheckReport(date, apiResponse, "v2", receiver);
        logger.info("Response about /polly : " + apiResponse.toString());
        return new ResponseEntity<>(apiResponse.toString(), HttpStatus.OK);
    }
}
