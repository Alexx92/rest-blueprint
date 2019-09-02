package com.test.strategy;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class EnrichAggregationStrategy implements AggregationStrategy {
	@Override
	public Exchange aggregate(Exchange original, Exchange resource) {
		String originalBody = original.getIn().getBody(String.class);
		String newString = resource.getIn().getBody(String.class);
		newString = newString.concat(originalBody) + " }";
		resource.getIn().setBody(newString);
		return resource;
	}
}
