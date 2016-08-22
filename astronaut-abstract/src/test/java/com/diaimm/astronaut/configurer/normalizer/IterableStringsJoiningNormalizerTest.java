package com.diaimm.astronaut.configurer.normalizer;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import junit.framework.Assert;

public class IterableStringsJoiningNormalizerTest {
	@Test
	public void underbarConcatterTest() {
		IterableStringsJoiningNormalizer normalizer = new IterableStringsJoiningNormalizer() {
			@Override
			String getSeparator() {
				return "_";
			}
		};

		String[] values1 = new String[]{"1", "2", "3"};
		Assert.assertEquals("1_2_3", normalizer.normalize(values1));
		
		List<String> values2 = Lists.newArrayList("1", "2", "3");
		Assert.assertEquals("1_2_3", normalizer.normalize(values2));
		
		Assert.assertEquals("1_2_3", normalizer.normalize(values2.iterator()));
	}
	
	@Test
	public void underbarConcatterIncludingNullAndEmptySpaceTest() {
		IterableStringsJoiningNormalizer normalizer = new IterableStringsJoiningNormalizer() {
			@Override
			String getSeparator() {
				return "_";
			}
		};

		String[] values1 = new String[]{" 1", null, "3 "};
		Assert.assertEquals("1_3", normalizer.normalize(values1));
		
		List<String> values2 = Lists.newArrayList(" 1", null, "3 ");
		Assert.assertEquals("1_3", normalizer.normalize(values2));
		
		Assert.assertEquals("1_3", normalizer.normalize(values2.iterator()));
	}
	
	@Test
	public void CommanJoningNormalizerTest(){
		CommanJoningNormalizer normalizer = new CommanJoningNormalizer();

		String[] values1 = new String[]{" 1", null, "3 "};
		Assert.assertEquals("1,3", normalizer.normalize(values1));
		
		List<String> values2 = Lists.newArrayList(" 1", null, "3 ");
		Assert.assertEquals("1,3", normalizer.normalize(values2));
		
		Assert.assertEquals("1,3", normalizer.normalize(values2.iterator()));
	}
}