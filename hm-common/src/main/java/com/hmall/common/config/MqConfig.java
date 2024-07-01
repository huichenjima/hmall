package com.hmall.common.config;

import com.hmall.common.utils.RabbitMqHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: MqConfig
 * Package: com.hmall.common.config
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/14 12:29
 * @Version 1.0
 */
@ConditionalOnClass(RabbitTemplate.class)
@Configuration
public class MqConfig {
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitMqHelper rabbitMqHelper(RabbitTemplate rabbitTemplate){
        return new RabbitMqHelper(rabbitTemplate);
    }
}
