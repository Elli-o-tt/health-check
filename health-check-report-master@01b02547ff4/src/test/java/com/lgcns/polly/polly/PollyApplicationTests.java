package com.lgcns.polly.polly;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.lgcns.polly.PollyController;

@SpringBootTest
class PollyApplicationTests {

	@Autowired
	PollyController controller;
	
	@Test
	@DisplayName("수기로 전송할 때 사용")
	void testPolly() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = sdf.parse("2023-12-29");

		controller.sendPollyMessageV2(date, "all");//all
//		controller.sendPollyMessageV2(date, "elliot");//all
//		controller.sendPollyMessageV2(date, "peter");//all

	}

}
