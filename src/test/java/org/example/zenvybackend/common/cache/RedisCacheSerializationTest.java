package org.example.zenvybackend.common.cache;

import org.example.zenvybackend.category.dto.response.CustomerCategoryResponse;
import org.example.zenvybackend.category.dto.response.MetadataFieldResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RedisCacheSerializationTest {

    private final GenericJackson2JsonRedisSerializer serializer =
            new GenericJackson2JsonRedisSerializer(RedisCacheObjectMapperFactory.create());

    @Test
    void roundTripsCustomerCategoryList() {
        CustomerCategoryResponse category = CustomerCategoryResponse.builder()
                .id(UUID.randomUUID())
                .name("Fashion")
                .metadataFields(List.of(
                        MetadataFieldResponse.builder()
                                .fieldId(UUID.randomUUID())
                                .name("size")
                                .values(List.of("S", "M"))
                                .build()
                ))
                .brands(List.of("Nike"))
                .minPrice(10.0)
                .maxPrice(100.0)
                .build();

        List<CustomerCategoryResponse> original = new ArrayList<>(List.of(category));

        byte[] bytes = serializer.serialize(original);
        Object restored = serializer.deserialize(bytes);

        assertNotNull(restored);
        @SuppressWarnings("unchecked")
        List<CustomerCategoryResponse> categories = (List<CustomerCategoryResponse>) restored;
        assertEquals(1, categories.size());
        assertEquals("Fashion", categories.get(0).getName());
        assertEquals(List.of("Nike"), categories.get(0).getBrands());
        assertEquals("size", categories.get(0).getMetadataFields().get(0).getName());
    }

    @Test
    void ignoresUnknownPropertiesFromOlderCacheEntries() throws Exception {
        UUID categoryId = UUID.randomUUID();
        String legacyJson = """
                ["java.util.ArrayList",[
                  {"@class":"org.example.zenvybackend.category.dto.response.CustomerCategoryResponse",
                   "id":"%s",
                   "name":"Legacy",
                   "legacyField":"ignored"}
                ]]
                """.formatted(categoryId);

        Object restored = serializer.deserialize(legacyJson.getBytes());

        assertNotNull(restored);
        @SuppressWarnings("unchecked")
        List<CustomerCategoryResponse> categories = (List<CustomerCategoryResponse>) restored;
        assertEquals("Legacy", categories.get(0).getName());
    }
}
