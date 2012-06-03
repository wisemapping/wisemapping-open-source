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

package com.wisemapping.ncontroller;

import com.wisemapping.service.MindmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PublicPagesController {
    @Autowired
    private MindmapService mindmapService;


    @RequestMapping(value = "aboutUs")
    public ModelAndView aboutUs() {
        return new ModelAndView("aboutUs");
    }

    @RequestMapping(value = "crew")
    public ModelAndView crew() {
        return new ModelAndView("crew");
    }

    @RequestMapping(value = "installCFG")
    public ModelAndView installCFG() {
        return new ModelAndView("installCFG");
    }

    @RequestMapping(value = "keyboard")
    public ModelAndView newsPage() {
        return new ModelAndView("keyboard");
    }

    @RequestMapping(value = "home")
    public ModelAndView home() {
        return new ModelAndView("homepage");
    }

    @RequestMapping(value = "iframeWrapper")
      public ModelAndView showIframe(@RequestParam(required = true) String url) {
          return new ModelAndView("iframeWrapper", "url", url);
      }

}
