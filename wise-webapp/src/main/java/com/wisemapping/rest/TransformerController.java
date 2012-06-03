/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package com.wisemapping.rest;


import com.wisemapping.exporter.ExportFormat;
import com.wisemapping.exporter.ExportProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TransformerController extends BaseController {

    private static final String PARAM_SVG_XML = "svgXml";
    private static final String PARAM_WISE_MAP_XML = "mapXml";
    private static final String PARAM_FILENAME = "filename";

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

    @RequestMapping(method = RequestMethod.POST, value = "/transform", produces = {"image/svg+xml"}, consumes = {"image/svg+xml"})
    @ResponseBody
    public ModelAndView transformSvg(@RequestBody @Nullable final String content) throws IOException {
        final Map<String, Object> values = new HashMap<String, Object>();
        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("Body can not be null.");
        }

        values.put("content", content);
        return new ModelAndView("transformViewSvg", values);
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
        return new ModelAndView("transformViewJpeg", values);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/transform", produces = {"application/freemind"}, consumes = {"application/xml"})
    @ResponseBody
    public ModelAndView transformFreemind(@RequestBody @Nullable final String content) throws IOException {
        final Map<String, Object> values = new HashMap<String, Object>();
        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("Body can not be null.");
        }
        values.put("content", content);
        return new ModelAndView("transformViewFreemind", values);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/transform", consumes = {"application/x-www-form-urlencoded"})
    public ModelAndView transform(@NotNull HttpServletRequest request) throws IOException {
        final String svg = request.getParameter(PARAM_SVG_XML);
        final String mapXml = request.getParameter(PARAM_WISE_MAP_XML);
        final String filename = request.getParameter(PARAM_FILENAME);


        // Obtains transformation type based on the last part of the URL ...
        final String requestURI = request.getRequestURI();
        final String format = requestURI.substring(requestURI.lastIndexOf(".") + 1, requestURI.length());
        final ExportFormat exportFormat = ExportFormat.valueOf(format.toUpperCase());

        ModelAndView result;
        switch (exportFormat) {
            case PNG:
                result = this.transformPng(svg);
                break;
            case JPEG:
                result = this.transformJpeg(svg);
                break;
            case PDF:
                result = this.transformPdf(svg);
                break;
            case SVG:
                result = this.transformSvg(svg);
                break;
            case FREEMIND:
                result = this.transformFreemind(mapXml);
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format");

        }
        result.getModelMap().put("filename", filename);
        return result;
    }
}
