package com.diaimm.astronaut.facebook.repositories;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.annotations.RestAPIRepository;
import com.diaimm.astronaut.configurer.annotations.mapping.Form;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Supplier;

@RestAPIRepository("facebook2.7")
public interface FaceBookFeedRepository {
	@GetForObject(url="/page-id/feed", dummySupplier = FaceBookFeedDummySupplier.class)
	public APIResponse<FaceBookFeed> get(@Form FaceBookFeedParam meParam);

	public static class FaceBookFeedDummySupplier implements Supplier<FaceBookFeed> {
		@Override
		public FaceBookFeed get() {
			return null;
		}
	}

	public static class FaceBookFeedParam {
		@Param("access_token")
		private String accessToken = "293116111054634|7f58412c725ec949051a579ba03d867f";
	}

	public static class FaceBookFeed {
		private List<Data> data;

		public void setData(List<Data> datas) {
			this.data = datas;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}

	public static class Data {
		private String id;
		private String message;
		private Date createdTime;

		public void setId(String id) {
			this.id = id;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@JsonProperty(value = "created_time")
		public void setCreatedTime(Date createdTime) {
			this.createdTime = createdTime;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}
}
