package com.creamsugardonut.kibanaproxy.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class JsonUtil {
    private static ObjectMapper mapper = new ObjectMapper();

    public static String convertAsString(Object value) throws JsonProcessingException {
        return mapper.writeValueAsString(value);
    }
}
