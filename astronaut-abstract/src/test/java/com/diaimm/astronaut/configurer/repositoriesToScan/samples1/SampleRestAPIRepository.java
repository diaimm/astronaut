package com.diaimm.astronaut.configurer.repositoriesToScan.samples1;

import com.diaimm.astronaut.configurer.APIResponse;
import com.diaimm.astronaut.configurer.annotations.RestAPIRepository;
import com.diaimm.astronaut.configurer.annotations.mapping.Param;
import com.diaimm.astronaut.configurer.annotations.method.GetForObject;
import com.google.common.base.Supplier;

@RestAPIRepository("resourceName")
public interface SampleRestAPIRepository {
}
