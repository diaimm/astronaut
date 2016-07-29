package com.diaimm.astronaut.configurer.transaction;

import java.util.List;

public interface TransactionalRestTemplate {

	ThreadLocal<List<Object>> getCallInfos();

	void startTransaction();

	void endTransaction();

}
