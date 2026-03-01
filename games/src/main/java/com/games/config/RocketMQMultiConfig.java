package com.games.config;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RocketMQMultiConfig {

    @Bean("gamesRocketMQTemplate")
    public RocketMQTemplate gamesTemplate(
            @Value("${rocketmq.name-server}") String namesrvAddr,
            RocketMQMessageConverter rocketMQMessageConverter) {
        DefaultMQProducer producer = new DefaultMQProducer("games-group");
        producer.setNamesrvAddr(namesrvAddr);
        RocketMQTemplate template = new RocketMQTemplate();
        template.setProducer(producer);
        template.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        return template;
    }

    @Bean("sportRocketMQTemplate")
    public RocketMQTemplate sportTemplate(
            @Value("${rocketmq.sport-name-server:${rocketmq.name-server}}") String namesrvAddr,
            RocketMQMessageConverter rocketMQMessageConverter) {
        DefaultMQProducer producer = new DefaultMQProducer("sport-group");
        producer.setNamesrvAddr(namesrvAddr);
        RocketMQTemplate template = new RocketMQTemplate();
        template.setProducer(producer);
        template.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        return template;
    }
}
