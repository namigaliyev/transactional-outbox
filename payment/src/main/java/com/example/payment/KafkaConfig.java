package com.example.payment;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.math.BigDecimal;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    private final PaymentRepository paymentRepository;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true);
        factory.setConcurrency(kafkaProperties.getListener().getConcurrency());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderMessage> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OrderDeserializer.class);
        return props;
    }

    @KafkaListener(topics = "mysql.orders.orders", groupId = "payments")
    public void listenOrderEvents(OrderMessage orderMessage) {
        OrderMessage.After data = orderMessage.getPayload().getAfter();
        switch (orderMessage.getPayload().getOp()) {
            case "c":
                System.out.println("Received order create event");
                Payment payment = Payment.builder()
                        .id(data.getId())
                        .amount(new BigDecimal(data.getPrice()))
                        .build();
                paymentRepository.save(payment);
            case "u":
                System.out.println("Received order update event");
                Payment payment1 = paymentRepository.findByOrderId(data.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Given order's payment information not found"));
                payment1.setAmount(new BigDecimal(data.getPrice()));
                paymentRepository.save(payment1);
        }
    }
}
