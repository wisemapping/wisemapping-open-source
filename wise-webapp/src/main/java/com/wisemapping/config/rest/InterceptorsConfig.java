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
package com.wisemapping.config.rest;

import com.wisemapping.filter.RequestPropertiesInterceptor;
import com.wisemapping.filter.UserLocaleInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan(basePackageClasses = UserLocaleInterceptor.class)
public class InterceptorsConfig implements WebMvcConfigurer {
    @Autowired
    private UserLocaleInterceptor userLocaleInterceptor;

    @Autowired
    private RequestPropertiesInterceptor requestPropertiesInterceptor;

    @Override
    public void addInterceptors(@NotNull final InterceptorRegistry registry) {
        registry.addInterceptor(userLocaleInterceptor);
        registry.addInterceptor(requestPropertiesInterceptor);
    }
}