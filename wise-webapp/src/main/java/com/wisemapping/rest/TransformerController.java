package com.wisemapping.rest;


import com.wisemapping.exporter.ExportProperties;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TransformerController {

    @RequestMapping(method = RequestMethod.POST, value = "/transform", produces = {"application/pdf"}, consumes = {"image/svg+xml"})
    @ResponseBody
    public ModelAndView transformPdf(@RequestBody @Nullable final String content) throws IOException {
        final Map<String, Object> values = new HashMap<String, Object>();
        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("Body can not be null.");
        }

        values.put("content", content);
        return new ModelAndView("transformViewPdf", values);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/transform", produces = {"image/png"}, consumes = {"image/svg+xml"})
    @ResponseBody
    public ModelAndView transformPng(@RequestBody @Nullable final String content) throws IOException {
        final Map<String, Object> values = new HashMap<String, Object>();
        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("Body can not be null.");
        }
        values.put("content", content);
        values.put("imageSize", ExportProperties.ImageProperties.Size.LARGE);
        return new ModelAndView("transformViewPng", values);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/transform", produces = {"image/jpeg"}, consumes = {"image/svg+xml"})
    @ResponseBody
    public ModelAndView transformJpeg(@RequestBody @Nullable final String content) throws IOException {
        final Map<String, Object> values = new HashMap<String, Object>();
        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("Body can not be null.");
        }
        values.put("content", content);
        values.put("imageSize", ExportProperties.ImageProperties.Size.LARGE);
        return new ModelAndView("transformViewJpg", values);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/transform", produces = {"application/freemind"}, consumes = {"text/xml"})
    @ResponseBody
    public ModelAndView transformFreemind(@RequestBody @Nullable final String content) throws IOException {
        final Map<String, Object> values = new HashMap<String, Object>();
        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("Body can not be null.");
        }
        values.put("content", content);
        return new ModelAndView("transformViewFreemind", values);
    }
}
