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

package com.wisemapping.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Calendar;

public class AccessAuditory
        implements Serializable {

    private int id;
    private Calendar loginDate = null;
    private User user = null;

    public AccessAuditory() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLoginDate(@NotNull Calendar loginDate) {
        this.loginDate = loginDate;
    }

    public Calendar getLoginDate() {
        return loginDate;
    }

    public void setUser(@NotNull User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }
}