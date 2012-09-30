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

package com.wisemapping.service;

import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.LockException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.CollaborationRole;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
* Refresh page should not lost the lock.
* En caso que no sea posible grabar por que se perdio el lock, usar mensaje de error para explicar el por que...
* Mensaje  modal explicando que el mapa esta siendo editado, por eso no es posible edilarlo....
*/

class LockManagerImpl implements LockManager {
    public static final int ONE_MINUTE_MILLISECONDS = 1000 * 60;
    final Map<Integer, LockInfo> lockInfoByMapId;
    final static Timer expirationTimer = new Timer();
    final private static Logger logger = Logger.getLogger("com.wisemapping.service.LockManager");

    public LockManagerImpl() {
        lockInfoByMapId = new ConcurrentHashMap<Integer, LockInfo>();
        expirationTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                logger.debug("Lock expiration scheduler started. Current locks:" + lockInfoByMapId.keySet());

                final List<Integer> toRemove = new ArrayList<Integer>();
                final Set<Integer> mapIds = lockInfoByMapId.keySet();
                for (Integer mapId : mapIds) {
                    final LockInfo lockInfo = lockInfoByMapId.get(mapId);
                    if (lockInfo.isExpired()) {
                        toRemove.add(mapId);
                    }
                }

                for (Integer mapId : toRemove) {
                    unlock(mapId);
                }
            }
        }, ONE_MINUTE_MILLISECONDS, ONE_MINUTE_MILLISECONDS);
    }

    @Override
    public boolean isLocked(@NotNull Mindmap mindmap) {
        return this.getLockInfo(mindmap) != null;
    }

    @Override
    public LockInfo getLockInfo(@NotNull Mindmap mindmap) {
        return lockInfoByMapId.get(mindmap.getId());
    }

    @Override
    public void updateExpirationTimeout(@NotNull Mindmap mindmap, @NotNull Collaborator user) {
        if (this.isLocked(mindmap)) {
            final LockInfo lockInfo = this.getLockInfo(mindmap);
            if (!lockInfo.getCollaborator().equals(user)) {
                throw new IllegalStateException("Could not update map lock timeout if you are not the locking user. User:" + lockInfo.getCollaborator() + ", " + user);
            }
            lockInfo.updateTimeout();
            logger.debug("Timeout updated for:" + mindmap.getId());

        }else {
            throw new IllegalStateException("Lock lost for map. No update possible.");
        }
    }

    @Override
    public void unlock(@NotNull Mindmap mindmap, @NotNull Collaborator user) throws LockException, AccessDeniedSecurityException {
        if (isLocked(mindmap) && !isLockedBy(mindmap, user)) {
            throw new LockException("Lock can be only revoked by the locker.");
        }

        if (!mindmap.hasPermissions(user, CollaborationRole.EDITOR)) {
            throw new AccessDeniedSecurityException("Invalid lock, this should not happen");
        }

        this.unlock(mindmap.getId());
    }

    private void unlock(int mapId) {
        logger.debug("Unlock map id:" + mapId);
        lockInfoByMapId.remove(mapId);
    }

    @Override
    public boolean isLockedBy(@NotNull Mindmap mindmap, @NotNull Collaborator collaborator) {
        boolean result = false;
        final LockInfo lockInfo = this.getLockInfo(mindmap);
        if (lockInfo != null && lockInfo.getCollaborator().equals(collaborator)) {
            result = true;
        }
        return result;
    }

    @Override
    public void lock(@NotNull Mindmap mindmap, @NotNull Collaborator user) throws AccessDeniedSecurityException, LockException {
        if (isLocked(mindmap) && !isLockedBy(mindmap, user)) {
            throw new LockException("Invalid lock, this should not happen");
        }

        if (!mindmap.hasPermissions(user, CollaborationRole.EDITOR)) {
            throw new AccessDeniedSecurityException("Invalid lock, this should not happen");
        }

        final LockInfo lockInfo = lockInfoByMapId.get(mindmap.getId());
        if (lockInfo != null) {
            // Update timeout only...
            logger.debug("Update timestamp:" + mindmap.getId());
            updateExpirationTimeout(mindmap, user);
        } else {
            logger.debug("Lock map id:" + mindmap.getId());
            lockInfoByMapId.put(mindmap.getId(), new LockInfo(user));
        }

    }

    @Override
    public void updateLock(boolean lock, @NotNull Mindmap mindmap, @NotNull User user) throws WiseMappingException {
        if (lock) {
            this.lock(mindmap, user);
        } else {
            this.unlock(mindmap, user);
        }
    }
}
