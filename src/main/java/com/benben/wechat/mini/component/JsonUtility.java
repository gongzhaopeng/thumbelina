package com.benben.wechat.mini.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JsonUtility {

    final private ObjectMapper objectMapper;

    @Autowired
    public JsonUtility(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    public String toJsonString(Object object) {

        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T parseJsonText(String jsonText, Class<T> clazz) {

        try {
            return objectMapper.readValue(jsonText, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
