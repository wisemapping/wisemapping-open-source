package com.wisemapping.test.model;


import com.wisemapping.rest.model.RestMindmap;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Test;

import java.io.IOException;

@Test
public class JsonTest {

    @Test
    void deserialize() throws IOException {
        String json = "{\"id\":\"1\",\"xml\":\"<map name=\\\"1\\\" version=\\\"tango\\\"><topic central=\\\"true\\\" text=\\\"ss\\\" id=\\\"1\\\"/></map>\",\"properties\":\"{\\\"zoom\\\":0.85}\"}";
        ObjectMapper mapper = new ObjectMapper();
        final RestMindmap restMindmap = mapper.readValue(json, RestMindmap.class);

    }

}
