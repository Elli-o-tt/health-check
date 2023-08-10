package com.lgcns.http;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.classic.Logger;
import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
@ConfigurationProperties("config.api.webhook.url")
public class PollyRestTemplateSerivce {

    private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    RestTemplate restTemplate;

    private String pollySurvey;

    public JSONArray retrievePollySurveyInfo(HttpHeaders requestHeader, JSONObject requestBody) {
        
        try{
            logger.info(">>>>>> Request data about retrievePollySurvey");
            logger.info("# url : " + pollySurvey);
            logger.info("# body : " + requestBody.toString());
        
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(),requestHeader);

            ResponseEntity<String> responseEntity = restTemplate.exchange(pollySurvey, HttpMethod.POST, entity, String.class);
            JSONArray response = new JSONArray(responseEntity.getBody());

            logger.info(">>>>>> Response data about retrievePollySurvey");
            logger.info(response.toString());
            
            return response;

        }catch(Exception e){
            e.printStackTrace();
            logger.error(e.toString());
            return new JSONArray().put(new JSONObject().put("fail", e.toString()));
        }
        
    }   

    public String sendPollyMessage(HttpHeaders requestHeader, JSONObject requestBody, String webhookUrl) {
        
        try{
            logger.info(">>>>>> Request data about sendPollyMessage");
            logger.info("# url : " + webhookUrl);
            logger.info("# body : " + requestBody.toString());

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(),requestHeader);

            ResponseEntity<String> responseEntity = restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, String.class);
            String response = responseEntity.getBody();
            
            logger.info(">>>>>> Response data about sendPollyMessage");
            logger.info(response.toString());
            
            return response;

        }catch(Exception e){
            e.printStackTrace();
            logger.error(e.toString());
            return e.toString();
        }
    }

    public JSONArray testPolly(String date, String receiver) {
        
        String url = "localhost:8080/polly/v2?date=" + date + "&receiver=" + receiver;
        
        try{
            logger.info(">>>>>> Request data about health check");
            logger.info("# url : " + url);
        
            HttpEntity<String> entity = new HttpEntity<>("",null);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JSONArray response = new JSONArray(responseEntity.getBody());

            logger.info(">>>>>> Response data about health check");
            logger.info(response.toString());
            
            return response;

        }catch(Exception e){
            e.printStackTrace();
            logger.error(e.toString());
            return new JSONArray().put(new JSONObject().put("fail", e.toString()));
        }
        
    }   
}
