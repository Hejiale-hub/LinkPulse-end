package com.hejiale.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.hejiale.common.constants.MqConstants.*;

@Configuration
public class RabbitMQConfig {

    // 声明队列（第二个参数true表示持久化）
    @Bean
    public Queue logRecordQueue() {
        return new Queue(LOGRECORD_QUEUE, true);
    }

    // 声明交换机
    @Bean
    public DirectExchange monitorExchange() {
        return new DirectExchange(MONITOR_EXCHANGE, true, false);
    }

    // 绑定队列到交换机，指定路由键
    @Bean
    public Binding binding(Queue logRecordQueue, DirectExchange monitorExchange) {
        return BindingBuilder.bind(logRecordQueue)
                .to(monitorExchange)
                .with(LOGRECORD_ROUTING_KEY);
    }

    // Use JSON payloads to avoid Java native deserialization security restrictions.
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean(name = "rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter);
        // Prevent endless redelivery loops for malformed/legacy payloads.
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
