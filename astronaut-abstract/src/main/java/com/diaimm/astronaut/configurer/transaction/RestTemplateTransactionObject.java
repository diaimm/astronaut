package com.diaimm.astronaut.configurer.transaction;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.google.common.collect.Lists;

public class RestTemplateTransactionObject {
	private List<TransactionCommand> commits = Lists.newArrayList();
	private List<TransactionCommand> rollbacks = Lists.newArrayList();

	public void pushToCallStack(TransactionCommand commitCommand, TransactionCommand rollbackCommand) {
		this.commits.add(commitCommand);
		this.rollbacks.add(rollbackCommand);
	}

	public List<TransactionCommand> getCommits() {
		return commits;
	}

	public List<TransactionCommand> getRollbacks() {
		return rollbacks;
	}

	public interface TransactionCommand {
		void execute();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
