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

package com.wisemapping.service;

import com.wisemapping.model.Mindmap;
import com.wisemapping.model.Account;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class LockInfo {
    final private Account user;
    private Calendar timeout;
    private static final int EXPIRATION_MIN = 30;

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    private int mapId;

    public LockInfo(@NotNull Account user, @NotNull Mindmap mindmap) {
        this.user = user;
        this.mapId = mindmap.getId();
        this.updateTimeout();
    }

    public Account getUser() {
        return user;
    }

    public boolean isExpired() {
        return timeout.before(Calendar.getInstance());
    }

    public void updateTimeout() {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, EXPIRATION_MIN);
        this.timeout = calendar;

    }

    @Override
    public String toString() {
        return "LockInfo{" +
                "user=" + user +
                ", timeout=" + timeout +
                ", mapId=" + mapId +
                '}';
    }
}
