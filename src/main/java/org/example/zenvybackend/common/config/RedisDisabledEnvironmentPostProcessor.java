package org.example.zenvybackend.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

public class RedisDisabledEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String REDIS_EXCLUDES =
            "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getProperty("app.redis.enabled", Boolean.class, true)) {
            return;
        }

        String existing = environment.getProperty("spring.autoconfigure.exclude", "");
        String excludes = existing.isBlank() ? REDIS_EXCLUDES : existing + "," + REDIS_EXCLUDES;

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.autoconfigure.exclude", excludes);
        environment.getPropertySources().addFirst(new MapPropertySource("redisDisabled", properties));
    }
}
