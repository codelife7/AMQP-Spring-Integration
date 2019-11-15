package com.codelife.amqp.poc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;

@SpringBootTest
class PocApplicationTests {


	@Autowired
	private MessageChannel controlChannel;

	@Autowired
	private PollableChannel receiveChannel;

	@Autowired
	private AbstractApplicationContext abstractApplicationContext;

	@Autowired
	AmqpInboundChannelAdapter inboundAdapter;

	@Test
	void channelTurnOnOff() {

		System.out.println("I am" + inboundAdapter.getBeanName());
		System.out.println("Channel running? " + inboundAdapter.isRunning());

		controlChannel.send(new GenericMessage<>("@'amqpInbound.amqp:inbound-channel-adapter#0'.stop()"));
		System.out.println("Stopping..");
		System.out.println("Channel running? " + inboundAdapter.isRunning());

		controlChannel.send(new GenericMessage<>("@'amqpInbound.amqp:inbound-channel-adapter#0'.start()"));
		System.out.println("Starting..");
		System.out.println("Channel running? " + inboundAdapter.isRunning());
	}

	@Test
	void channelReceivesMessageWhileItsRunning(){
		controlChannel.send(new GenericMessage<>("@'amqpInbound.amqp:inbound-channel-adapter#0'.start()"));
		System.out.println("Channel running? " + inboundAdapter.isRunning());
		assert(inboundAdapter.isRunning());
		Boolean msg = receiveChannel.send(new GenericMessage<>("I AM TEST MESSAGE"));
		assert(msg);
	}

	@Test
	void channelDoesNotReceivesMessageWhileItsStopped(){
		controlChannel.send(new GenericMessage<>("@'amqpInbound.amqp:inbound-channel-adapter#0'.stop()"));
		System.out.println("Channel running? " + inboundAdapter.isRunning());
		assert(!inboundAdapter.isRunning());
		Boolean msg = receiveChannel.send(new GenericMessage<>("I AM TEST MESSAGE"));
	}
}
