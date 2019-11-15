package com.codelife.amqp.poc;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
import org.springframework.util.ErrorHandler;

@Configuration
@EnableIntegration
public class RabbitConfiguration {

    @Bean
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory();
    }

    @Bean
    public IntegrationFlow amqpInbound(ConnectionFactory connectionFactory) {
        return IntegrationFlows.from(Amqp.inboundAdapter(connectionFactory, "aName"))
                .log()
                .channel(receiveChannel())
                .handle(myMessageHandler())
                .get();
    }

    @Bean
    public IntegrationFlow controlBus() {
        return IntegrationFlows.from(controlChannel())
                .controlBus()
                .get();
    }

    private MessageHandler myMessageHandler() {
        return message -> System.out.println("Got message " + message);
    }

    private ErrorHandler myErrorHandler() {
        return new ErrorHandler() {
            @Override
            public void handleError(Throwable t) {
                System.out.println(t.getMessage());
            }
        };
    }

    @Bean
    public PollableChannel receiveChannel() {
        return new QueueChannel();
    }

    @Bean
    public MessageChannel controlChannel() {
        return MessageChannels.direct().get();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata defaultPoller() {

        return Pollers.fixedRate(500)
                .receiveTimeout(500)
                .sendTimeout(1000)
                .errorHandler(myErrorHandler())
                .maxMessagesPerPoll(1)
                .get();
    }
}
