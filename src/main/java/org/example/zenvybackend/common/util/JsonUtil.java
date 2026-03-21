package org.example.zenvybackend.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String normalize(Object json) {
        try {
            Map<String, Object> source;

            if (json instanceof String jsonString) {
                source = mapper.readValue(jsonString, TreeMap.class);
            } else {
                source = mapper.convertValue(json, LinkedHashMap.class);
            }

            Map<String, Object> map = new TreeMap<>(source);

            return mapper.writeValueAsString(map);

        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON");
        }
    }
}
