package com.diaimm.astronaut.facebook;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.facebook.repositories.FaceBookMeRepository;
import com.diaimm.astronaut.facebook.repositories.FaceBookMeRepository.Me;
import com.diaimm.astronaut.facebook.repositories.FaceBookMeRepository.MeParam;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RestTemplateAdapterTestConfiguration.class })
public class FaceBookAPICallTest {
	@Autowired
	private FaceBookMeRepository faceBookMeRepository;

	@Test
	public void testGettingMe() {
		APIResponse<Me> apiResponse = faceBookMeRepository.get(new MeParam());
		if(apiResponse.isSuccess()){
			System.out.println(apiResponse.getContents());
			return;
		}

		System.out.println(apiResponse.getCode());
		System.out.println(apiResponse.getMessage());
	}
}
