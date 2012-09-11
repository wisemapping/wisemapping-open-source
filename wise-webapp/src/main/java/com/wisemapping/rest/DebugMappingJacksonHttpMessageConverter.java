package com.wisemapping.rest;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


public class DebugMappingJacksonHttpMessageConverter extends MappingJacksonHttpMessageConverter {
    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, JsonHttpMessageNotReadableException {
        final byte[] bytes = IOUtils.toByteArray(inputMessage.getBody());
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WrapHttpInputMessage wrap = new WrapHttpInputMessage(bais, inputMessage.getHeaders());

        try {
            return super.readInternal(clazz, wrap);

        } catch (org.springframework.http.converter.HttpMessageNotReadableException e) {
            throw new JsonHttpMessageNotReadableException("Request Body:\n" + new String(bytes, "UTF-8"), e);
        }
        catch (IOException e) {
            throw new JsonHttpMessageNotReadableException("Request Body:\n" + new String(bytes, "UTF-8"), e);
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
    public InputStream getBody() throws IOException {
        return body;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }
}
