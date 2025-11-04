package com.example.bankcards.config;

import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.handler.CardOperationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Configuration
public class CardOperationHandlerConfig {

    @Bean
    public Map<CardOperation, CardOperationHandler> cardOperationsHandler(ListableBeanFactory beanFactory) {
        Map<CardOperation, CardOperationHandler> map = new EnumMap<>(CardOperation.class);

        Map<String, CardOperationHandler> beans = beanFactory.getBeansOfType(CardOperationHandler.class);

        for (CardOperationHandler value : beans.values()) {
            map.put(value.getOperationType(), value);
        }

        return map;
    }
}
