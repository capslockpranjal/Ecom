package org.example.zenvybackend.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

@Component
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String normalize(String json) {
        try {
            // Convert JSON → TreeMap (sorted keys)
            Map<String, Object> map = mapper.readValue(json, TreeMap.class);

            // Convert back → JSON string (sorted)
            return mapper.writeValueAsString(map);

        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON");
        }
    }
}
