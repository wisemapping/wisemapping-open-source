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

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TransformerController extends BaseController {

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
}
