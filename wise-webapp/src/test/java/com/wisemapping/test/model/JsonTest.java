package com.wisemapping.test.model;


import com.wisemapping.rest.model.RestMindmap;
import com.wisemapping.rest.model.RestUser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Test;

import java.io.IOException;

@Test
public class JsonTest {

    @Test
    void deserialize() throws IOException {
        String mapJson = "{\"id\":\"1\",\"xml\":\"<map name=\\\"1\\\" version=\\\"tango\\\"><topic central=\\\"true\\\" text=\\\"ss\\\" id=\\\"1\\\"/></map>\",\"properties\":\"{\\\"zoom\\\":0.85}\"}";
        ObjectMapper mapper = new ObjectMapper();
        final RestMindmap restMindmap = mapper.readValue(mapJson, RestMindmap.class);

        String userJson = "{\"username\":\"admin\",\"email\":\"admin@wisemapping.org\",\"tags\":[],\"creationDate\":1329706800000,\"firstname\":\"Wise\",\"lastname\":\"test\",\"password\":\"test\"}";
        final RestUser restUser = mapper.readValue(userJson, RestUser.class);

    }


}
