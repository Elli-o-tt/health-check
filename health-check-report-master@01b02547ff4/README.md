# LaunchCenterHealthCheck

## 개요

매주 수요일에 센터원 상태를 체크하기 위해서 Polly를 사용한 Health Check 설문조사를 하고 있다.

Heath Check의 결과는 각 팀장님들에게 전달되는데 기존에는 담당자가 `Polly Survey 접속` → `팀의 결과 조회` → `Excel Export` → `취합` → `wiki에 업로드` 하는 방식으로 내용을 취합했고 팀 단위로 작업해야해서 6번을 반복했다.

그리고 각 팀장님들도 결과를 보기 위해서 wiki에 접속해서 봐야만 했다. 이를 개선하고자 Polly의 오픈 API와 Slack Webhook API를 활용해서 자동화를 했다.

## Polly와 Slack 설정

- ### Polly

1. app.polly.ai 접속 후 로그인
2. `</>API` → `Web API Tokens` → `Create Token`
3. Polly API 호출할 때 발급받은 token 사용

- ### Slack

1. Channel에 Slack Webhook 생성 (https://slack.com/intl/ko-kr/help/articles/115005265063-Slack%EC%9A%A9-%EC%88%98%EC%8B%A0-%EC%9B%B9%ED%9B%84%ED%81%AC)
2. 수신할 대상 설정

- https://api.slack.com/apps/
- App 선택 → `Add features and functionality` → `Incoming Webhooks` → `Add New Webhook to Workspace` → 사용자 추가

## Program Process

1. Polly 오픈 API를 통해서 지난주 Health Check Survey 결과 조회
2. Slack Webhook API Format에 맞도록 JSON Parsing
3. 데이터 정렬 및 마킹 처리
4. 팀 단위로 각 팀장님들께 전송
5. Peter님에게는 전체팀 결과 전송

## Sample Data

- #### Polly 설문 조사 결과

  <details>
    <summary> url : https://api.polly.ai/v1/surveys.list </summary>
  </details>
  <details>
    <summary> request </summary>
      
  ```json
  {
    "send_at_from": "2022-05-31 09:00:00",
    "send_at_to": "2022-06-02 09:00:00"
  }
  ```
	</details>
  <details>
    <summary> response </summary>
    	
  ```json
  [
  	{
  		"results_visibility": "hidden",
  		"audience": [
  			{
  				"name": "woo.choi@lgcns.com",
  				"type": "user",
  				"channel_id": "D02TZRG4FNU"
  			},
  			{
  				"name": "jykim123@lgcns.com",
  				"type": "user",
  				"channel_id": "D02TCCMH8SY"
  			},
  			{
  				"name": "chaeyoon.lim@lgcns.com",
  				"type": "user",
  				"channel_id": "D02SVF5HBHV"
  			}
  		],
  		"metadata": null,
  		"author": "U02UAU157NU",
  		"days_between_reminders": null,
  		"questions": [
  			{
  				"comments": [],
  				"allow_adding_choices": false,
  				"allow_multiple_answers": false,
  				"allow_comments": false,
  				"id": "f27TmYYdf77XKcHFa",
  				"text": "프로젝트는 잘 진행되고 있나요?",
  				"type": "five_emoji_rank",
  				"choices": [
  					{
  						"votes": 0,
  						"id": "0",
  						"text": ":cry: Awful"
  					},
  					{
  						"votes": 1,
  						"id": "1",
  						"text": ":slightly_frowning_face: Bad"
  					},
  					{
  						"votes": 8,
  						"id": "2",
  						"text": ":neutral_face: Neutral"
  					},
  					{
  						"votes": 4,
  						"id": "3",
  						"text": ":slightly_smiling_face: Good"
  					},
  					{
  						"votes": 5,
  						"id": "4",
  						"text": ":heart_eyes: Great"
  					}
  				],
  				"results": [
  					{
  						"user_email": "sshyuun@gmail.com",
  						"deleted": false,
  						"user_id": "U02S2BT8BHU",
  						"user_name": "@HYUN(송성현)",
  						"created_at": "2022-06-14T05:17:50.843Z",
  						"id": "8yo6fwiG3t3CJybKG",
  						"text": ":neutral_face: Neutral",
  						"choice_id": 2
  					},
  					{
  						"user_email": "seungjaechoi@lgcns.com",
  						"deleted": false,
  						"user_id": "U02RPSDLFCM",
  						"user_name": "@KAIN(카인) (KAIN(최승재))",
  						"created_at": "2022-06-14T01:33:37.531Z",
  						"id": "y4e9zk4bKHZp7QFep",
  						"text": ":slightly_smiling_face: Good",
  						"choice_id": 3
  					},
  					{
  						"user_email": "chaeyoon.lim@lgcns.com",
  						"deleted": false,
  						"user_id": "U02S6HWNMPF",
  						"user_name": "@Harry(임채윤)",
  						"created_at": "2022-06-09T06:24:58.291Z",
  						"id": "a3z2fJkwXPdPWx6jq",
  						"text": ":neutral_face: Neutral",
  						"choice_id": 2
  					},
  					{
  						"user_email": "yeunjin1@naver.com",
  						"deleted": false,
  						"user_id": "U02S96P24KC",
  						"user_name": "@Sarah (사연진)",
  						"created_at": "2022-06-08T12:48:22.956Z",
  						"id": "YbarhkPswBe5urDaB",
  						"text": ":heart_eyes: Great",
  						"choice_id": 4
  					},
  					{
  						"user_email": "ddmczp@naver.com",
  						"deleted": false,
  						"user_id": "U02SB9TPE4U",
  						"user_name": "@Dean(이상헌)",
  						"created_at": "2022-06-08T07:27:34.733Z",
  						"id": "hfKo3NM6bT5LKQRLg",
  						"text": ":slightly_smiling_face: Good",
  						"choice_id": 3
  					}
  				],
  				"comment_prompt": null
  			},
  			{
  				"comments": [],
  				"allow_adding_choices": false,
  				"allow_multiple_answers": false,
  				"allow_comments": false,
  				"id": "NuBqCS5cEHdojDob7",
  				"text": "프로젝트에서 의미있는 일을 하고 있나요?",
  				"type": "five_emoji_rank",
  				"choices": [
  					{
  						"votes": 0,
  						"id": "0",
  						"text": ":cry: Awful"
  					},
  					{
  						"votes": 0,
  						"id": "1",
  						"text": ":slightly_frowning_face: Bad"
  					},
  					{
  						"votes": 5,
  						"id": "2",
  						"text": ":neutral_face: Neutral"
  					},
  					{
  						"votes": 8,
  						"id": "3",
  						"text": ":slightly_smiling_face: Good"
  					},
  					{
  						"votes": 5,
  						"id": "4",
  						"text": ":heart_eyes: Great"
  					}
  				],
  				"results": [
  					{
  						"user_email": "sshyuun@gmail.com",
  						"deleted": false,
  						"user_id": "U02S2BT8BHU",
  						"user_name": "@HYUN(송성현)",
  						"created_at": "2022-06-14T05:17:50.846Z",
  						"id": "QMXAuiws4x7CQ4Ftr",
  						"text": ":neutral_face: Neutral",
  						"choice_id": 2
  					},
  					{
  						"user_email": "seungjaechoi@lgcns.com",
  						"deleted": false,
  						"user_id": "U02RPSDLFCM",
  						"user_name": "@KAIN(카인) (KAIN(최승재))",
  						"created_at": "2022-06-14T01:33:37.531Z",
  						"id": "FBLRjBeAbPRyR8DbW",
  						"text": ":slightly_smiling_face: Good",
  						"choice_id": 3
  					},
  					{
  						"user_email": "chaeyoon.lim@lgcns.com",
  						"deleted": false,
  						"user_id": "U02S6HWNMPF",
  						"user_name": "@Harry(임채윤)",
  						"created_at": "2022-06-09T06:24:58.294Z",
  						"id": "JRrmkikzJscy2J92f",
  						"text": ":neutral_face: Neutral",
  						"choice_id": 2
  					},
  					{
  						"user_email": "yeunjin1@naver.com",
  						"deleted": false,
  						"user_id": "U02S96P24KC",
  						"user_name": "@Sarah (사연진)",
  						"created_at": "2022-06-08T12:48:22.958Z",
  						"id": "4KXeeHNyE2ito6hFg",
  						"text": ":heart_eyes: Great",
  						"choice_id": 4
  					},
  					{
  						"user_email": "ddmczp@naver.com",
  						"deleted": false,
  						"user_id": "U02SB9TPE4U",
  						"user_name": "@Dean(이상헌)",
  						"created_at": "2022-06-08T07:27:34.733Z",
  						"id": "yd6uG2K3nCNkMoZhj",
  						"text": ":slightly_smiling_face: Good",
  						"choice_id": 3
  					}
  				],
  				"comment_prompt": null
  			},
  			{
  				"comments": [],
  				"allow_adding_choices": false,
  				"allow_multiple_answers": false,
  				"allow_comments": false,
  				"id": "SYz2knfbu34fdCg6A",
  				"text": "이번 주 나의 상태는 어떤가요?",
  				"type": "five_emoji_rank",
  				"choices": [
  					{
  						"votes": 0,
  						"id": "0",
  						"text": ":cry: Awful"
  					},
  					{
  						"votes": 0,
  						"id": "1",
  						"text": ":slightly_frowning_face: Bad"
  					},
  					{
  						"votes": 5,
  						"id": "2",
  						"text": ":neutral_face: Neutral"
  					},
  					{
  						"votes": 8,
  						"id": "3",
  						"text": ":slightly_smiling_face: Good"
  					},
  					{
  						"votes": 5,
  						"id": "4",
  						"text": ":heart_eyes: Great"
  					}
  				],
  				"results": [
  					{
  						"user_email": "sshyuun@gmail.com",
  						"deleted": false,
  						"user_id": "U02S2BT8BHU",
  						"user_name": "@HYUN(송성현)",
  						"created_at": "2022-06-14T05:17:50.846Z",
  						"id": "ta2j3bJBvqkjECbxL",
  						"text": ":neutral_face: Neutral",
  						"choice_id": 2
  					},
  					{
  						"user_email": "seungjaechoi@lgcns.com",
  						"deleted": false,
  						"user_id": "U02RPSDLFCM",
  						"user_name": "@KAIN(카인) (KAIN(최승재))",
  						"created_at": "2022-06-14T01:33:37.531Z",
  						"id": "oXXvav8NrSn2rxnJ9",
  						"text": ":slightly_smiling_face: Good",
  						"choice_id": 3
  					},
  					{
  						"user_email": "chaeyoon.lim@lgcns.com",
  						"deleted": false,
  						"user_id": "U02S6HWNMPF",
  						"user_name": "@Harry(임채윤)",
  						"created_at": "2022-06-09T06:24:58.295Z",
  						"id": "FDHYxuqQF6BskKcT5",
  						"text": ":slightly_smiling_face: Good",
  						"choice_id": 3
  					},
  					{
  						"user_email": "yeunjin1@naver.com",
  						"deleted": false,
  						"user_id": "U02S96P24KC",
  						"user_name": "@Sarah (사연진)",
  						"created_at": "2022-06-08T12:48:22.958Z",
  						"id": "6ijhkLSA9ceFciZ9F",
  						"text": ":heart_eyes: Great",
  						"choice_id": 4
  					},
  					{
  						"user_email": "ddmczp@naver.com",
  						"deleted": false,
  						"user_id": "U02SB9TPE4U",
  						"user_name": "@Dean(이상헌)",
  						"created_at": "2022-06-08T07:27:34.733Z",
  						"id": "b5CuYcnNMondy4oSK",
  						"text": ":slightly_smiling_face: Good",
  						"choice_id": 3
  					}
  				],
  				"comment_prompt": null
  			}
  		],
  		"active": true,
  		"created_at": "2022-06-08T00:00:03.901Z",
  		"send_at": "2022-06-08T00:00:00.000Z",
  		"appeal": "이번 주 나의 컨디션을 체크해보세요!",
  		"type": "survey",
  		"title": "클라우드개발3팀 Health Check",
  		"sent": true,
  		"anonymity_level": "non_anonymous",
  		"comments_visibility": "hidden",
  		"reminder_count": 0,
  		"send_via_direct_message": true,
  		"close_at": "2022-06-15T00:00:00.000Z",
  		"template_id": "6kzsi7EDRmpDfiM6j",
  		"id": "ctsEo8uvZETsDesaG"
  	}
  ]
  ```
  </details>

- #### Slack Webhook
	<details>
    <summary> url : https://hooks.slack.com/services/{user} </summary>
  </details>
  <details>
    <summary> request </summary>

	```json
	{
		"blocks": [
			{
				"text": {
					"text": "👥  *클라우드개발3팀 (18)*\n📅  *2022-06-08*\n💬  프로젝트 진행 / 의미있는 일 / 나의 상태",
					"type": "mrkdwn"
				},
				"type": "section"
			},
			{
				"text": {
					"text": "👉*`@Felix(최유성) / Bad / Good / Neutral`*\n@최우 (John(최우)) / Neutral / Good / Good\n@HYUN(송성현) / Neutral / Neutral / Neutral\n@Simon_(이상현) (Simon (이상현)) / Great / Great / Great\n@Sarah (사연진) / Great / Great / Great\n@Hailey(김지영) / Good / Good / Good\n@Harry(임채윤) / Neutral / Neutral / Good\n@Mount(정성산) / Neutral / Neutral / Good\n@Lecky(임서형) / Good / Good / Good\n@Lina(윤지원) / Great / Great / Great\n@Brad (박윤환) / Great / Great / Great\n@KAIN(카인) (KAIN(최승재)) / Good / Good / Good\n@Dean(이상헌) / Good / Good / Good\n@Nana(송나현) / Neutral / Neutral / Neutral\n@Vada(김성호) / Neutral / Neutral / Neutral\n@Stomy(이장원) / Neutral / Good / Neutral\n@Scott(박영재) / Neutral / Good / Good\n@Jeff(양재혁) / Great / Great / Great\n",
					"type": "mrkdwn"
				},
				"type": "section"
			},
			{
				"text": {
					"text": "🚀 Health Check Report by 미래위",
					"type": "mrkdwn"
				},
				"type": "section"
			}
		]
	}
	```
  </details>
  <details>
    <summary> response </summary>
		OK
  </details>

## Message Format

- ### v1

<img width="500" alt="image" src="https://user-images.githubusercontent.com/21374902/172743766-129ef9c9-cf71-46fd-9239-11be35b6905e.png">
<img width="500" alt="image" src="https://user-images.githubusercontent.com/21374902/172743865-cbc5c9ab-09ff-4974-99dd-e29f78a165ef.png">

- ### v2

<img width="455" alt="image" src="https://user-images.githubusercontent.com/21374902/172743521-45f04b00-c5a2-4677-8449-bd91a5aca811.png">


## Docker Container
  - Jar 파일 생성
    - mvn 명령어 오류 시 `mvn -N io.takari:maven:wrapper`
	- `./mvnw package && java -jar target/`
  - Dockerfile로 image 생성 : `docker build -t polly:1.0 .`
  - image 실행 : `docker run -d --name polly -p 8085:8080 polly:1.0`
  - container 접속 : `docker exec -it polly bin/sh`
    - log 경로 : /logs
  - ⭐️현재는 평가 시스템 AWS 환경에서 구동중입니다.
  - AWS 환경 내에서 수동으로 실행 : `curl --location --request POST 'localhost:8085/polly/v2?receiver=beaver&date=2022-11-09'`
  
## 스케쥴링 변경
- `com.lgcns.schedule.CronScheduler` 에서 `@Scheduled` 변경

## Reference
  - Polly API Guide : https://docs.polly.ai/api/
  - Slack Webhook Guide : https://api.slack.com/messaging/webhooks
  - JSON Parser : http://json.parser.online.fr/
  - Slack Webhook Block-kit-builder : https://app.slack.com/block-kit-builder/
