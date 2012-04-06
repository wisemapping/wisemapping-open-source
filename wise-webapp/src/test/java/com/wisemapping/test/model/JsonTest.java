package com.wisemapping.test.model;


import com.wisemapping.rest.model.RestMindmap;
import com.wisemapping.rest.model.RestMindmapInfo;
import com.wisemapping.rest.model.RestUser;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Test;

import java.io.IOException;

@Test
public class JsonTest {

    @Test
    void deserialize() throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        String json1 = "{\"id\":\"1\",\"xml\":\"<map name=\\\"1\\\" version=\\\"tango\\\"><topic central=\\\"true\\\" text=\\\"ss\\\" id=\\\"1\\\"/></map>\",\"properties\":\"{\\\"zoom\\\":0.85}\"}";
        mapper.readValue(json1, RestMindmap.class);

        String json2 = "{\"title\":\"some title\",\"description\":\"description here\"}";
        mapper.readValue(json2, RestMindmap.class);

        String userJson = "{\"username\":\"admin\",\"email\":\"admin@wisemapping.org\",\"tags\":[],\"creationDate\":1329706800000,\"firstname\":\"Wise\",\"lastname\":\"test\",\"password\":\"test\"}";
        final RestUser restUser = mapper.readValue(userJson, RestUser.class);
    }

    @Test
    void serialize() throws IOException {
        String mapJson = "{\"id\":\"1\",\"xml\":\"<map name=\\\"1\\\" version=\\\"tango\\\"><topic central=\\\"true\\\" text=\\\"ss\\\" id=\\\"1\\\"/></map>\",\"properties\":\"{\\\"zoom\\\":0.85}\"}";
        ObjectMapper mapper = new ObjectMapper();

        final RestMindmap value = new RestMindmap();
        value.setTitle("titl");
        value.setTitle("desck");
        final String restMindmap = mapper.writeValueAsString(value);
        System.out.println(restMindmap);
    }


}
