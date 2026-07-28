package com.atta.PolyPaste.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.queue.input}")
    private String inputQueue;

    @Value("${app.rabbitmq.queue.output}")
    private String outputQueue;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.output}")
    private String routingKeyOutput;

    @Value("${app.rabbitmq.routing-key.input}")
    private String routingKeyInput;


    @Bean
    public Queue q1() {
        return new Queue(inputQueue, true);
    }

    @Bean
    public Queue q2() {
        return new Queue(outputQueue, true);
    }

    @Bean
    public DirectExchange moderationExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Binding bindingQ2(Queue q2, DirectExchange moderationExchange) {
        return BindingBuilder.bind(q2).to(moderationExchange).with(routingKeyOutput);
    }

    @Bean
    public Binding bindingQ1(Queue q1, DirectExchange moderationExchange) {
        return BindingBuilder.bind(q1).to(moderationExchange).with(routingKeyInput);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}