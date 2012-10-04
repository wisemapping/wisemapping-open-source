/*
*    Copyright [2012] [wisemapping]
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

package com.wisemapping.rest.model;


import com.wisemapping.model.Collaborator;
import com.wisemapping.model.User;
import com.wisemapping.service.LockInfo;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

@XmlRootElement(name = "lock")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestLockInfo {

    @NotNull
    final private Collaborator user;

    @Nullable
    final private LockInfo lockInfo;

    // This is required only for compliance with the JAXB serializer.
    public RestLockInfo(){

        this.lockInfo = null;
        //noinspection ConstantConditions
        this.user = null;
    }

    public RestLockInfo(@Nullable LockInfo lockInfo, @NotNull Collaborator collaborator) {

        this.lockInfo = lockInfo;
        this.user = collaborator;
    }

    public boolean isLocked() {
        return lockInfo != null;
    }

    public void setLocked(boolean locked) {
        // Ignore ...
    }

    public boolean isLockedByMe() {
        return isLocked() && lockInfo != null && lockInfo.getCollaborator().equals(user);
    }

    public void setLockedByMe(boolean lockedForMe) {
        // Ignore ...
    }

    public long getTimestamp() {
        return lockInfo != null ? lockInfo.getTimestamp() : -1;
    }

    public void setTimestamp(long value) {
        //
    }

}
