package com.example.device;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"MAIL_USERNAME=test@gmail.com",
		"MAIL_PASSWORD=test-password"
})
class DeviceApplicationTests {

	@Test
	void contextLoads() {
	}
}