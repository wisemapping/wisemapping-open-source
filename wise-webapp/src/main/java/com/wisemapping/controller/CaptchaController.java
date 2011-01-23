/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.servlet.ServletOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.octo.captcha.service.image.ImageCaptchaService;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class CaptchaController
	implements Controller, InitializingBean
{
    private ImageCaptchaService captchaService;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) 
		throws Exception 
    {

		byte[] captchaChallengeAsJpeg;
		// the output stream to render the captcha image as jpeg into
		final ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();

    	// get the session id that will identify the generated captcha.
		//the same id must be used to validate the response, the session id is a good candidate!
    	final String captchaId = request.getSession().getId();

		// call the ImageCaptchaService getChallenge method
      final BufferedImage challenge = captchaService.getImageChallengeForID(captchaId,request.getLocale());

		// a jpeg encoder
		final JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(jpegOutputStream);
		jpegEncoder.encode(challenge);

       captchaChallengeAsJpeg = jpegOutputStream.toByteArray();

		// flush it in the response
      response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
      response.setContentType("image/jpeg");
		final ServletOutputStream responseOutputStream = response.getOutputStream();
		responseOutputStream.write(captchaChallengeAsJpeg);
    	responseOutputStream.flush();
		responseOutputStream.close();
		return null;	
    }	
	
    /** Set captcha service
     *  @param captchaService The captchaService to set.
     */
    public void setCaptchaService(ImageCaptchaService captchaService) {
    	this.captchaService = captchaService;		
    }	
	
    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() 
		throws Exception 
    {
		if(captchaService == null){
			throw new RuntimeException("captcha service wasn`t set!");
		}
    }
}
