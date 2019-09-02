package com.test.route;

import java.io.IOException;

import org.apache.camel.CamelExchangeException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.processor.binding.BindingException;

import com.test.strategy.EnrichAggregationStrategy;


public class ImplementRestService_Route extends RouteBuilder{
	@Override
	public void configure() throws Exception {
		restConfiguration()
			.component("spark-rest")
			.port(8585)
//			.bindingMode(RestBindingMode.json)
			.dataFormatProperty("prettyPrint", "true")
			.apiContextPath("api")
		;
		
		onException(BindingException.class)
	    	.handled(true)
	    	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
	    	.setBody(constant("===============> ERROR BindingException IN ROUTE <==============="))
	    ;
		onException(IOException.class)
	    	.handled(true)
	    	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
	    	.setBody(constant("===============> ERROR IOException IN ROUTE <==============="))
	    ;
		onException(CamelExchangeException.class)
	    	.handled(true)
	    	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
	    	.setBody(constant("===============> ERROR CamelExchangeException IN ROUTE <==============="))
	    ;
		
		rest("/spark")
			.description("Methods inputs for Route test JB421 Fuse 7.1")
			.get("/test/{id}")
				.description("Method generate call to other route with CAMEL-HTTP")
				.param()
					.name("id")
					.description("Key ID corresponding to seleted item")
					.type(RestParamType.path)
				.endParam()
				.responseMessage()
					.code(200)
					.message("Correct ejecution")
				.endResponseMessage()
				.to("direct:getA")
			.post()
				.to("direct:postB")
			/*
			 * 
			 * */
			.get("/test/receiveCall/{id}")
				.to("direct:receiveCallGet")
			.post("/test/receiveCall")
				.to("direct:receiveCallPost")
		;
		/*
		 * 
		 * */
		from("direct:receiveCallGet")
			.log("*** RECEIVE CALL | HEADERS => ${headers}")
			.log("*** RECEIVE CALL | BODY => ${body}")			
			.setBody().constant("{ \"description\":\"TEST RECEIVE CALL GET\" }")
			.convertBodyTo(String.class)
			.log("*** RESPONSE RECEIVE CALL => ${body}")			
		;
		/*
		 * 
		 * */
		from("direct:receiveCallPost")
			.log("*** RECEIVE CALL | HEADERS => ${headers}")
			.log("*** RECEIVE CALL | BODY => ${body}")			
			.setBody().constant("\"description\":\"TEST RECEIVE CALL POST\"}")
			.log("*** RESPONSE RECEIVE CALL => ${body}")
		;
		/*
		 * 
		 * */
		from("direct:getA")
			.routeId("DIRECT GET")
			.log("** SEND | ${headers}")
			.log("** SEND | ${body}")
			.setProperty("INITIAL-BODY", simple("${body}"))
			.log("** SEND | CLASS A-1 ${body.class}")
//			.setBody().simple("\"${body.get(\"text\")}\"")
			.setBody().simple("HASMAP")
			.enrich("direct:addResource", new EnrichAggregationStrategy())
			.log("** SEND | CLASS A-2 ${body.class}")
			.log("BODY TO HTTP => ${body}")
			.setHeader("CamelHttpUri", constant("/spark/test/receiveCall/5"))
			.setHeader("Content-Type", constant("application/json"))
            .setHeader("Accept", constant("application/json"))
		    .removeHeader(Exchange.HTTP_PATH)
			.log("** SEND HTTP | ${headers}")
			.to("http4://0.0.0.0:8585/spark/test/receiveCall/5?bridgeEndpoint=true")
			
		    .removeHeader(Exchange.TRANSFER_ENCODING)
		    .setHeader("User-Agent",constant("PostmanRuntime/7.15.0"))
			.convertBodyTo(String.class)
			.log("** RESPONSE HTTP 3| ${body}")
			.log("** RESPONSE HTTP 4| ${body.class}")			
		;
		
		from("direct:addResource")
			.setBody().constant("{ \"INFO\":")
			.log("* LOAD FIRST PART MESSAGE *")
		;
		
		from("direct:postB")
			.routeId("DIRECT POST")
			.log("${headers}")
			.log("${body}")
			.log("${body.class}")
			.log("DIRECT TEST B")
			.to("mock:direct:testB")
		;
		
		
	}
	

}
