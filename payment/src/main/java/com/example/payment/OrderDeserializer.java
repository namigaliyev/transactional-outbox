package com.example.payment;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.hibernate.type.SerializationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class OrderDeserializer implements Deserializer<OrderMessage> {

    private final ObjectMapper mapper = new ObjectMapper();


    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public OrderMessage deserialize(String topic, byte[] data) {
        try {
            if (data == null) {
                System.out.println("Empty data");
                return null;
            }

            return mapper.readValue(new String(data, StandardCharsets.UTF_8), OrderMessage.class);
        } catch (IOException e) {
            throw new SerializationException("Unable to deserialize order message", e);
        }
    }

    @Override
    public OrderMessage deserialize(String topic, Headers headers, byte[] data) {
        return Deserializer.super.deserialize(topic, headers, data);
    }

    @Override
    public void close() {
        Deserializer.super.close();
    }
}
