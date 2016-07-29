package com.diaimm.astronaut.configurer.annotations.mapping;

import org.junit.Test;

import com.diaimm.astronaut.configurer.annotations.mapping.PathParam.EnumNormalizer;

import junit.framework.Assert;

public class EnumNormalizerTest {
	@Test
	public void testIt() {
		EnumNormalizer target = new EnumNormalizer();
		Assert.assertEquals("VALUE1", target.normalize(SampleEnum.VALUE1));
		Assert.assertEquals("VALUE2", target.normalize(SampleEnum.VALUE2));
	}

	public static enum SampleEnum {
		VALUE1,
		VALUE2;
	}
}
