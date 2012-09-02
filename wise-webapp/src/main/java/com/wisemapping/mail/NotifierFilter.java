package com.wisemapping.mail;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.DigestUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class NotifierFilter {
    public static final int MAX_CACHE_ENTRY = 500;
    private final Map<String, String> emailByMd5 = Collections.synchronizedMap(new LinkedHashMap<String, String>() {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_CACHE_ENTRY;
        }
    });

    public boolean hasBeenSend(@NotNull final String email, @NotNull final Map<String, String> model) {

        final StringBuilder buff = new StringBuilder();
        for (String key : model.keySet()) {
            buff.append(key);
            buff.append("=");
            buff.append(model.get(key));
        }

        final String digest = DigestUtils.md5DigestAsHex(buff.toString().getBytes());
        boolean result = emailByMd5.containsKey(digest);
        if (!result) {
            emailByMd5.put(digest, email);
        }
        return result;
    }

}
