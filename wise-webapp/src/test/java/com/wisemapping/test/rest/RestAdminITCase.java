package com.wisemapping.test.rest;


import org.testng.annotations.Test;


@Test
public class RestAdminITCase {

    @Test
    public void createNewUser() {
        String uri = "http://localhost:9000/service/admin/user";

//        RestTemplate template = new RestTemplate();
//        location = template.postForLocation(uri);
    }
}
