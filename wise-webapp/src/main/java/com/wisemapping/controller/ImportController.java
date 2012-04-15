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

package com.wisemapping.controller;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.importer.ImporterException;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import com.wisemapping.view.ImportMapBean;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.support.StringMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImportController
        extends BaseSimpleFormController {

    //~ Methods ..............................................................................................
    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        return new ImportMapBean();
    }

    public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
            throws ServletException, WiseMappingException, ImporterException {
        final ImportMapBean bean = (ImportMapBean) command;

        User user = Utils.getUser();
        final UserService userService = this.getUserService();

        user = userService.getUserBy(user.getId());
        final MindMap mindMap = bean.getImportedMap();
        mindMap.setOwner(user);

        final MindmapService mindmapService = this.getMindmapService();
        mindmapService.addMindmap(mindMap, user);

        final StringBuilder redirectionTo = new StringBuilder("redirect:" + mindMap.getId() + "/edit.htm");
        return new ModelAndView(redirectionTo.toString());
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
            throws ServletException {
        // to actually be able to convert Multipart instance to a String
        // we have to register a custom editor
        binder.registerCustomEditor(String.class, new StringMultipartFileEditor());
        // now Spring knows how to handle multipart object and convert them
    }
}
