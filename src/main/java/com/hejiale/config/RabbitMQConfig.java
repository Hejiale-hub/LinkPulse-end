//package com.hejiale.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.amqp.support.converter.MessageConverter;
//import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
//import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import static com.hejiale.common.constants.MqConstants.*;
//
// todo 本项目由于服务器资源有限，暂时不使用RabbitMQ消息队列，配置类代码已注释掉，后续有条件时再完善 ！！！！
//
//@Configuration
//@Slf4j
//public class RabbitMQConfig {
//
//    // 声明队列（第二个参数true表示持久化）
//    @Bean
//    public Queue logRecordQueue() {
//        return QueueBuilder.durable(LOGRECORD_QUEUE).build();
//    }
//
//    @Bean
//    public Queue logRecordRetryQueue() {
//        return QueueBuilder.durable(LOGRECORD_RETRY_QUEUE)
//                .ttl(RETRY_TTL_MILLIS)
//                .deadLetterExchange(MONITOR_EXCHANGE)
//                .deadLetterRoutingKey(LOGRECORD_ROUTING_KEY)
//                .build();
//    }
//
//    @Bean
//    public Queue logRecordDlqQueue() {
//        return QueueBuilder.durable(LOGRECORD_DLQ_QUEUE).build();
//    }
//
//    // 声明交换机
//    @Bean
//    public DirectExchange monitorExchange() {
//        return new DirectExchange(MONITOR_EXCHANGE, true, false);
//    }
//
//    @Bean
//    public DirectExchange retryExchange() {
//        return new DirectExchange(RETRY_EXCHANGE, true, false);
//    }
//
//    @Bean
//    public DirectExchange dlxExchange() {
//        return new DirectExchange(DLX_EXCHANGE, true, false);
//    }
//
//    // 绑定队列到交换机，指定路由键
//    @Bean
//    public Binding binding(@Qualifier("logRecordQueue") Queue logRecordQueue,
//                           @Qualifier("monitorExchange") DirectExchange monitorExchange) {
//        return BindingBuilder.bind(logRecordQueue)
//                .to(monitorExchange)
//                .with(LOGRECORD_ROUTING_KEY);
//    }
//
//    @Bean
//    public Binding retryBinding(@Qualifier("logRecordRetryQueue") Queue logRecordRetryQueue,
//                                @Qualifier("retryExchange") DirectExchange retryExchange) {
//        return BindingBuilder.bind(logRecordRetryQueue)
//                .to(retryExchange)
//                .with(LOGRECORD_RETRY_ROUTING_KEY);
//    }
//
//    @Bean
//    public Binding dlqBinding(@Qualifier("logRecordDlqQueue") Queue logRecordDlqQueue,
//                              @Qualifier("dlxExchange") DirectExchange dlxExchange) {
//        return BindingBuilder.bind(logRecordDlqQueue)
//                .to(dlxExchange)
//                .with(LOGRECORD_DLQ_ROUTING_KEY);
//    }
//
//    // Use JSON payloads to avoid Java native deserialization security restrictions.
//    @Bean
//    public MessageConverter messageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//
//    @Bean(name = "rabbitListenerContainerFactory")
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
//            SimpleRabbitListenerContainerFactoryConfigurer configurer,
//            ConnectionFactory connectionFactory,
//            MessageConverter messageConverter) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        configurer.configure(factory, connectionFactory);
//        factory.setMessageConverter(messageConverter);
//        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//        // Prevent endless redelivery loops for malformed/legacy payloads.
//        factory.setDefaultRequeueRejected(false);
//        return factory;
//    }
//
//    @Bean
//    public RabbitTemplateCustomizer rabbitTemplateCustomizer() {
//        return rabbitTemplate -> {
//            rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
//                if (!ack) {
//                    String messageId = correlationData == null ? "null" : correlationData.getId();
//                    log.error("MQ消息未到达交换机, messageId={}, cause={}", messageId, cause);
//                }
//            });
//
//            rabbitTemplate.setReturnsCallback(returned -> log.error(
//                    "MQ消息路由失败, exchange={}, routingKey={}, replyCode={}, replyText={}, messageId={}",
//                    returned.getExchange(),
//                    returned.getRoutingKey(),
//                    returned.getReplyCode(),
//                    returned.getReplyText(),
//                    returned.getMessage().getMessageProperties().getMessageId()));
//        };
//    }
//}
