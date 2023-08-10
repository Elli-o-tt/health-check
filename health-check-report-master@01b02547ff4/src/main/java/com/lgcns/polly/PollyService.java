package com.lgcns.polly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.lgcns.common.DateUtil;
import com.lgcns.http.PollyRestTemplateSerivce;

import ch.qos.logback.classic.Logger;
import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
@ConfigurationProperties("config.api.webhook.url")
public class PollyService {

    private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private PollyRestTemplateSerivce pollyRestTemplateSerivce;

    private String pollyApiKey;

    private String awsServiceScale;
    private String awsServiceTechnology;
    private String awsApplication;
    private String amDiscovery;
    private String awsDevOps;
    private String devOpsAutomation;
    private String amScale;
    private String peter;
    private String elliot;

    public void sendHealthCheckReport(Date date, JSONObject apiResponse, String version, String receiver){
        
        if(date == null) date = new Date();

        String sendAtFrom = DateUtil.addDay(date, -3) + " 09:00:00";
        String sendAtTo = DateUtil.addDay(date, -1) + " 09:00:00";

        logger.info(">>>>> Starting to retrieve Polly Survey (" + sendAtFrom + " - " + sendAtTo + ")");

        HttpHeaders requestHeader = new HttpHeaders();
        requestHeader.set("Content-Type", "application/json");
        requestHeader.set("accept", "application/json");
        requestHeader.set("X-API-TOKEN", pollyApiKey);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("token", pollyApiKey);
        requestBody.put("send_at_from", sendAtFrom);
        requestBody.put("send_at_to", sendAtTo);

        JSONArray response = pollyRestTemplateSerivce.retrievePollySurveyInfo(requestHeader, requestBody);
        
        if(response.length() < 1 ){
            apiResponse.put("message", "해당 날짜에 설문 데이터가 없습니다.");
        }else if(response.length() == 1 && response.getJSONObject(0).has("fail")){
            apiResponse.put("message", response.getJSONObject(0).getString("fail"));
        }else {
            switch(version){
                case "v1" : 
                createSlackMessageFormatV1(response, apiResponse, receiver);
                break;
                
                case "v2":
                createSlackMessageFormatV2(response, apiResponse, receiver);
                break;
            }
        }
        
        logger.info(">>>>> Ending to retrieve Polly Survey (" + sendAtFrom + " - " + sendAtTo);
    }

    private void createSlackMessageFormatV1(JSONArray response, JSONObject apiResponse, String receiver) {

        logger.debug(">>>>> Starting to create Polly Message");

        JSONObject divider = new JSONObject();
        divider.put("type", "divider");
        
        for(int teamIndex = 0; teamIndex < response.length(); teamIndex++){
            if(!response.getJSONObject(teamIndex).getString("appeal").contains("이번 주 나의 컨디션을 체크해보세요"))
                continue;
            
            JSONArray slackMessageFormat = new JSONArray();
            JSONObject teamData = response.getJSONObject(teamIndex);
            String teamName = teamData.getString("title").split("팀")[0].replace(" ", "").trim() + "팀";
            String createdAt = teamData.getString("created_at").split("T")[0].trim();

            JSONArray questions = teamData.getJSONArray("questions");
            for(int questionIndex = 0; questionIndex < questions.length(); questionIndex++){
                String questionTitle = questions.getJSONObject(questionIndex).getString("text");

                createSlackMessageHeaderV1(slackMessageFormat, createdAt, teamName, questionTitle);

                JSONObject bodySection = new JSONObject();
                bodySection.put("type", "section");
                bodySection.put("fields", new JSONArray());
                JSONArray questionResults = questions.getJSONObject(questionIndex).getJSONArray("results");

                List<JSONObject> tmpJsonList = new ArrayList<>();
                for(Object questionResult : questionResults){
                    tmpJsonList.add((JSONObject) questionResult);
                }
                // inventory.sort(Comparator.comparing(Apple::getWeight));
                tmpJsonList.sort(Comparator.comparing((JSONObject o) -> o.getInt("choice_id")));
                questionResults = new JSONArray(tmpJsonList);

                for(int resultIndex = 0; resultIndex < questionResults.length(); resultIndex++){
                    
                    String userName = questionResults.getJSONObject(resultIndex).getString("user_name");
                    String text = questionResults.getJSONObject(resultIndex).getString("text");

                    JSONObject resultName = new JSONObject();
                    resultName.put("type", "plain_text");
                    resultName.put("text", userName);

                    JSONObject resultText = new JSONObject();
                    resultText.put("type", "plain_text");
                    resultText.put("text", text);

                    bodySection.getJSONArray("fields").put(resultName);
                    bodySection.getJSONArray("fields").put(resultText);

                    if((resultIndex + 1) % 5 == 0) {
                        slackMessageFormat.put(bodySection);

                        if((resultIndex + 1) != questionResults.length()){
                            bodySection = new JSONObject();
                            bodySection.put("type", "section");
                            bodySection.put("fields", new JSONArray());
                        }
                    }
                }

                if(bodySection != null && bodySection.length() > 0 && questionResults.length() % 5 != 0) 
                    slackMessageFormat.put(bodySection);
                if(questionIndex != questions.length() - 1)
                    slackMessageFormat.put(divider);
            }
            createSlackMessageFooter(slackMessageFormat);
            logger.debug(">>>>> Starting to create Polly Message");
            String slackResponse = sendSlackWebhookMessage(teamName, slackMessageFormat, receiver);
            apiResponse.put(teamName, slackResponse);
        }
    }

    private void createSlackMessageFormatV2(JSONArray response, JSONObject apiResponse, String receiver) {

        logger.debug(">>>>> Starting to create Polly Message");

        JSONObject divider = new JSONObject();
        divider.put("type", "divider");
        
        JSONArray slackMessageFormatAllTeams = new JSONArray();
        StringBuffer sb = new StringBuffer();
        for(int teamIndex = 0; teamIndex < response.length(); teamIndex++){
            if(!response.getJSONObject(teamIndex).getString("appeal").contains("이번 주 나의 컨디션을 체크해보세요"))
                continue;

            JSONArray slackMessageFormat = new JSONArray();
            JSONObject teamData = response.getJSONObject(teamIndex);
            String teamName = teamData.getString("title").split("팀")[0].replace(" ", "").trim() + "팀";
            String createdAt = teamData.getString("created_at").split("T")[0];
            int responseCount = teamData.getJSONArray("questions").getJSONObject(0).getJSONArray("results").length();

            createSlackMessageHeaderV2(slackMessageFormat, teamName, responseCount, createdAt);
            createSlackMessageHeaderV2(slackMessageFormatAllTeams, teamName, responseCount, createdAt);
            
            JSONArray questions = teamData.getJSONArray("questions");
            JSONObject questionTextObject = new JSONObject();
            for(int questionIndex = 0; questionIndex < questions.length(); questionIndex++){
                JSONArray results = questions.getJSONObject(questionIndex).getJSONArray("results");
                for(int resultIndex = 0; resultIndex < results.length(); resultIndex++){
                    String userName = results.getJSONObject(resultIndex).getString("user_name");
                    String text = results.getJSONObject(resultIndex).getString("text").split(":")[2].trim();
                    
                    if(questionTextObject.isNull(userName)){
                        questionTextObject.put(userName, text);
                    }else {
                        questionTextObject.put(userName, questionTextObject.get(userName) + " / " + text);
                    }
                }
            }

            JSONObject bodySection = new JSONObject();
            bodySection.put("type", "section");
            bodySection.put("text", new JSONObject());
            bodySection.getJSONObject("text").put("type", "mrkdwn");
            
            List<String> jsonList = new ArrayList<>();
            questionTextObject.keys().forEachRemaining((key) -> {
                jsonList.add(key + "/" + questionTextObject.getString(key));
            }
            );
            
            jsonList.sort((String str1, String str2) -> {
                if(str2.contains("Bad") || str2.contains("Awful"))
                    return 1;
                else if(!str1.contains("Bad") && !str1.contains("Awful") && !str2.contains("Bad") && !str2.contains("Awful"))
                    return 0;
                else
                    return -1;
            });

            sb = new StringBuffer();
            for(int index = 0; index < jsonList.size(); index++){
                String value = jsonList.get(index);
                
                if(value.contains("Awful") || value.contains("Bad")){
                    sb.append("👉*`").append(value).append("`*").append("\n");
                }else {
                    sb.append(value).append("\n");
                }

                if((index + 1) % 20 == 0){
                    bodySection.getJSONObject("text").put("text", sb.toString());
                    slackMessageFormat.put(bodySection);
                    // slackMessageFormat.put(divider);

                    slackMessageFormatAllTeams.put(bodySection);
                    // slackMessageFormatAllTeams.put(divider);

                    sb = new StringBuffer();

                    if((index + 1) != jsonList.size()){
                        bodySection = new JSONObject();
                        bodySection.put("type", "section");
                        bodySection.put("text", new JSONObject());
                        bodySection.getJSONObject("text").put("type", "mrkdwn");
                    }
                }
            }
            
            if(bodySection != null && bodySection.length() > 0 && jsonList.size() % 20 != 0) {
                bodySection.getJSONObject("text").put("text", sb.toString());
                slackMessageFormat.put(bodySection);    
                slackMessageFormatAllTeams.put(bodySection);
            }

//            createSlackMessageFooter(slackMessageFormat);
            
            logger.debug(">>>>> Starting to create Polly Message");
//            info: 전체 팀 결과 각팀장님들께 보내는 것으로 수정
//            String slackResponse = sendSlackWebhookMessage(teamName, slackMessageFormat, receiver);
//            apiResponse.put(teamName, slackResponse);
        }

        createSlackMessageFooter(slackMessageFormatAllTeams);
        String slackResponse = sendSlackWebhookMessage(slackMessageFormatAllTeams, receiver);
        apiResponse.put("전체팀", slackResponse);
    }

    private void createSlackMessageHeaderV1(JSONArray slackMessageFormat, String createdAt, String teamName, String questionTitle){
        JSONObject headerSection = new JSONObject();
        headerSection.put("type", "section");
        headerSection.put("text", new JSONObject());
        headerSection.getJSONObject("text").put("type", "plain_text");
        headerSection.getJSONObject("text").put("text", "[" + createdAt + "][" + teamName + "] " + questionTitle);
        slackMessageFormat.put(headerSection);
    }

    private void createSlackMessageHeaderV2(JSONArray slackMessageFormat, String teamName, int responseCount, String createdAt){
        JSONObject headerSection = new JSONObject();
        headerSection.put("type", "section");
        headerSection.put("text", new JSONObject());
        headerSection.getJSONObject("text").put("type", "mrkdwn");
        headerSection.getJSONObject("text").put("text", "👥  *" + teamName + " (" + responseCount + ")*\n📅  *" + createdAt + "*\n💬  프로젝트 진행 / 의미있는 일 / 나의 상태");
        slackMessageFormat.put(headerSection);
    }

    private void createSlackMessageFooter(JSONArray slackMessageFormat){
        JSONObject footerSection = new JSONObject();
        footerSection.put("type", "section");
        footerSection.put("text", new JSONObject());
        footerSection.getJSONObject("text").put("type", "mrkdwn");
        footerSection.getJSONObject("text").put("text", "🚀 Health Check Report by 미래위");
        slackMessageFormat.put(footerSection);
    }

    private String sendSlackWebhookMessage(String teamName, JSONArray slackMessageFormat, String receiver) {

        logger.debug(">>>>> Starting to send Polly Message");

        HttpHeaders requestHeader = new HttpHeaders();
        requestHeader.set("Content-Type", "application/json");
        requestHeader.set("accept", "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("blocks", slackMessageFormat);

        Map<String, String> mapTeamAndWebhookUrl = new HashMap<>();
        mapTeamAndWebhookUrl.put("AWS서비스Scale팀", awsServiceScale); // Danny(정상혁)
        mapTeamAndWebhookUrl.put("AWS데브옵스팀", awsDevOps); // Riley(손은영)
        mapTeamAndWebhookUrl.put("AMDiscovery팀", amDiscovery); // Asher(조솔)
        mapTeamAndWebhookUrl.put("AWS애플리케이션팀", awsApplication); // LeeA(이영아)
        mapTeamAndWebhookUrl.put("데브옵스자동화팀", devOpsAutomation); // Kane(이명진)
        mapTeamAndWebhookUrl.put("AMScale팀", amScale); // Seven(이원준)

        String response = "";
        logger.info(">>>>> receiver : " + receiver);
        switch(receiver){
            case "elliot":
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, elliot);
            break;

            case "peter":
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, peter);
            break;

            case "all":
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, mapTeamAndWebhookUrl.get(teamName));
            break;

            default:
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, elliot);
            break;
        }
        
        logger.debug(">>>>> Ending to send Polly Message");

        return response;
    }

    private String sendSlackWebhookMessage(JSONArray slackMessageFormatAllTeams, String receiver) {

        logger.debug(">>>>> Starting to send Polly Message");

        HttpHeaders requestHeader = new HttpHeaders();
        requestHeader.set("Content-Type", "application/json");
        requestHeader.set("accept", "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("blocks", slackMessageFormatAllTeams);

        String response = "";
        logger.info(">>>>> receiver : " + receiver);
        switch(receiver){
            case "elliot":
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, elliot);
            break;

            case "peter": 
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, peter);
            break;

            case "all":
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, peter);
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, elliot);
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, awsServiceScale);
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, awsServiceTechnology);
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, awsApplication);
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, amDiscovery);
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, awsDevOps);
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, devOpsAutomation);
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, amScale);

            break;

            default:
            response = pollyRestTemplateSerivce.sendPollyMessage(requestHeader, requestBody, elliot);
            break;
        }
        
        logger.debug(">>>>> Ending to send Polly Message");

        return response;
    }
}