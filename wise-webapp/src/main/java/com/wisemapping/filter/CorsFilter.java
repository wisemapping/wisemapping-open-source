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

package com.wisemapping.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * If your wisemapping customization throws cross domain errores in browser, you can configure this filter in webdefault.xml 
 * By default it will accept all domains, but you can restrict to the domain you need
 * 
 *       <filter>
 *              <filter-name>cross-origin</filter-name>
 *              <filter-class>com.wisemapping.filter.CorsFilter</filter-class>
 *              <init-param>
 *                      <param-name>allowedOrigins</param-name>
 *                      <param-value>*</param-value>
 *              </init-param>
 *              <init-param>
 *                      <param-name>allowedMethods</param-name>
 *                      <param-value>GET,POST,HEAD</param-value>
 *              </init-param>
 *              <init-param>
 *                      <param-name>allowedHeaders</param-name>
 *                      <param-value>X-Requested-With,Content-Type,Accept,Origin</param-value>
 *              </init-param>
 *      </filter>
 *      <filter-mapping>
 *              <filter-name>cross-origin</filter-name>
 *              <url-pattern>/*</url-pattern>
 *      </filter-mapping>
 * 
 */
public class CorsFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		if (servletResponse != null) {
			// Authorize (allow) all domains to consume the content
			((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Origin", "*");
			((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Methods","GET, OPTIONS, HEAD, PUT, POST");
		}

	    chain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {
	}
}
