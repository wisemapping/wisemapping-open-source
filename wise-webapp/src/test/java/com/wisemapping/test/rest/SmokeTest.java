package com.wisemapping.test.rest;


import com.wisemapping.config.Application;
import com.wisemapping.rest.MindmapController;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@SpringBootTest(classes = Application.class)
@ExtendWith(SpringExtension.class)

class SmokeTest {

    @Autowired
    private MindmapController controller;

    @Test
    void contextLoads() throws Exception {
        if(controller==null) throw new IllegalStateException();
    }
}