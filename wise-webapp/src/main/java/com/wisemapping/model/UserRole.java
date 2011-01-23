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

public enum UserRole {
    OWNER(true, true, true), COLLABORATOR(true, true, false), VIEWER(false, true, false);

    private final boolean hasEditPermission;
    private final boolean hasViewPermission;
    private final boolean hasDeletePermission;

    private UserRole(boolean hasEditPermission, boolean hasViewPermission, boolean hasDeletePermission) {
        this.hasEditPermission = hasEditPermission;
        this.hasViewPermission = hasViewPermission;
        this.hasDeletePermission = hasDeletePermission;
    }

    public boolean hasEditPermission() {
        return hasEditPermission;
    }

    public boolean hasViewPermission() {
        return hasViewPermission;
    }

    public boolean hasDeletePermission() {
        return hasDeletePermission;
    }
}
