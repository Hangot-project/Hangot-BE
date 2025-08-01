package com.hanyang.fileparser.core.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name}")
    private String queue;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(queue).build();
    }

    @Bean
    public Queue delayQueue() {
        Map<String, Object> delayArgs = new HashMap<>();
        delayArgs.put("x-message-ttl", 60000);
        delayArgs.put("x-dead-letter-exchange", exchangeName + ".dlx");
        delayArgs.put("x-dead-letter-routing-key", routingKey + ".dlq");
        return QueueBuilder.durable(queue + ".delay")
                .withArguments(delayArgs)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(queue + ".dlq").build();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(exchangeName + ".delay");
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(exchangeName + ".dlx");
    }



    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public Binding delayBinding(Queue delayQueue, DirectExchange delayExchange) {
        return BindingBuilder.bind(delayQueue).to(delayExchange).with(routingKey + ".delay");
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(routingKey + ".dlq");
    }


}
