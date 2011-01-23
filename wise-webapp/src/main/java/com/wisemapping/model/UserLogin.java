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

package com.wisemapping.model;

import java.io.Serializable;
import java.util.Calendar;

public class UserLogin
    implements Serializable
{

	private int id;
	private Calendar loginDate = null;
	private String email = null;
	
    	public UserLogin()
    	{
	}

	public int getId() {
	        return id;
    	}

    	public void setId(int id) {
        	this.id = id;
    	}
	
	public void setLoginDate(Calendar loginDate)
	{
		this.loginDate = loginDate;
	}
		
	public Calendar getLoginDate()
	{
		return loginDate;
	}
	
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getEmail()
	{
		return email;
	}
/*
	public boolean equals(Object o) {
        	if (this == o) return true;
        	if (o == null || getClass() != o.getClass()) return false;

        	final UserLogin userLogin = (UserLogin) o;

        	if (loginDate.equals(userLogin.loginDate)) return false;
        	if (email.equals(userLogin.email)) return false;        	

        	return true;
    	}

    	public int hashCode() {
        	int result;
        	result = (loginDate!= null ? loginDate.hashCode() : 0);
        	result = 29 * result + (email != null ? email.hashCode() : 0);        	
        	return result;
    	}
    	*/
}