/*
 *    Copyright [2022] [wisemapping]
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
            if (!key.equals("mapXML")) {
                buff.append(key);
                buff.append("=");
                buff.append(model.get(key));
            }
        }

        final String digest = DigestUtils.md5DigestAsHex(buff.toString().getBytes());
        boolean result = emailByMd5.containsKey(digest);
        if (!result) {
            emailByMd5.put(digest, email);
        }
        return result;
    }

}
