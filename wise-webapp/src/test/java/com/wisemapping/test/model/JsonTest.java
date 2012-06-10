package com.wisemapping.test.model;


import com.wisemapping.rest.model.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

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
        ObjectMapper mapper = new ObjectMapper();

        final RestMindmap value = new RestMindmap();
        value.setTitle("titl");
        value.setTitle("desck");
        final String restMindmap = mapper.writeValueAsString(value);
        System.out.println(restMindmap);
    }

    @Test
    void deserializeCollbsList() throws IOException {
        String collbsJson = "{\"count\":1,\"collaborations\":[{\"email\":\"paulo@pveiga.com.ar\",\"role\":\"editor\"}]}";
        ObjectMapper mapper = new ObjectMapper();

        final RestCollaborationList list = mapper.readValue(collbsJson, RestCollaborationList.class);
        final List<RestCollaboration> collaborations = list.getCollaborations();
        for (RestCollaboration collaboration : collaborations) {
            final String role = collaboration.getRole();
            final String email = collaboration.getEmail();
        }
    }


}
