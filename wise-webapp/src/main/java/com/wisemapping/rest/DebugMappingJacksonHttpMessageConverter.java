/*
 *    Copyright [2015] [wisemapping]
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


public class DebugMappingJacksonHttpMessageConverter extends MappingJackson2HttpMessageConverter {

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws JsonHttpMessageNotReadableException {
        try {
            final byte[] bytes = IOUtils.toByteArray(inputMessage.getBody());
            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            final WrapHttpInputMessage wrap = new WrapHttpInputMessage(bais, inputMessage.getHeaders());

            return super.readInternal(clazz, wrap);

        } catch (HttpMessageNotReadableException | IOException e) {
            throw new JsonHttpMessageNotReadableException("Request Body could not be read", e);
        }
    }
}


class WrapHttpInputMessage implements HttpInputMessage {
    private InputStream body;
    private HttpHeaders headers;

    WrapHttpInputMessage(InputStream is, HttpHeaders headers) {
        this.body = is;
        this.headers = headers;
    }

    @Override
    public InputStream getBody() {
        return body;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }
}
