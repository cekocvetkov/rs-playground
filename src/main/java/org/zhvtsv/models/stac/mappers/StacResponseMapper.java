package org.zhvtsv.models.stac.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zhvtsv.models.stac.Feature;

public class StacResponseMapper {
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static Feature getStacResponse(String content) throws JsonProcessingException {
        return mapper.readValue(content, Feature.class);
    }
}
