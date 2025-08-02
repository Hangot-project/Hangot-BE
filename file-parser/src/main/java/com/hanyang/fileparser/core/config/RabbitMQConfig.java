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
    public Queue retryQueue() {
        Map<String, Object> retryArgs = new HashMap<>();
        retryArgs.put("x-message-ttl", 300000); // 5분
        retryArgs.put("x-dead-letter-exchange", exchangeName);
        retryArgs.put("x-dead-letter-routing-key", routingKey);
        return QueueBuilder.durable(queue + ".retry")
                .withArguments(retryArgs)
                .build();
    }


    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(exchangeName + ".retry");
    }




    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public Binding retryBinding(Queue retryQueue, DirectExchange retryExchange) {
        return BindingBuilder.bind(retryQueue).to(retryExchange).with(routingKey + ".retry");
    }



}
