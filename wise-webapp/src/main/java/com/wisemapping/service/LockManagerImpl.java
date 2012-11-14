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

package com.wisemapping.service;

import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.LockException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.CollaborationRole;
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
* Internacionalizacion de los mensaje ...
* Logout limpiar las sessiones ...
*
* Casos:
*  - Usuario pierde el lock:
*      - Y grabo con la misma sessions y el timestap ok.
*      - Y grabo con la misma session y el timestap esta mal
 *     - Y grabo con distinta sessions
 *     -
*  - Usuario pierde el lock, pero intenta grabar camio
*/

class LockManagerImpl implements LockManager {
    public static final int ONE_MINUTE_MILLISECONDS = 1000 * 60;
    final Map<Integer, LockInfo> lockInfoByMapId;
    final static Timer expirationTimer = new Timer();
    final private static Logger logger = Logger.getLogger("com.wisemapping.service.LockManager");

    @Override
    public boolean isLocked(@NotNull Mindmap mindmap) {
        return this.getLockInfo(mindmap) != null;
    }

    @Override
    public LockInfo getLockInfo(@NotNull Mindmap mindmap) {
        return lockInfoByMapId.get(mindmap.getId());
    }

    @Override
    public LockInfo updateExpirationTimeout(@NotNull Mindmap mindmap, @NotNull User user) {
        if (!this.isLocked(mindmap)) {
            throw new IllegalStateException("Lock lost for map. No update possible.");
        }

        final LockInfo result = this.getLockInfo(mindmap);
        if (!result.getUser().equals(user)) {
            throw new IllegalStateException("Could not update map lock timeout if you are not the locking user. User:" + result.getUser() + ", " + user);
        }

        result.updateTimeout();
        result.updateTimestamp(mindmap);
        logger.debug("Timeout updated for:" + mindmap.getId());
        return result;
    }

    @Override
    public void unlock(@NotNull Mindmap mindmap, @NotNull User user) throws LockException, AccessDeniedSecurityException {
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
    public boolean isLockedBy(@NotNull Mindmap mindmap, @NotNull User collaborator) {
        boolean result = false;
        final LockInfo lockInfo = this.getLockInfo(mindmap);
        if (lockInfo != null && lockInfo.getUser().equals(collaborator)) {
            result = true;
        }
        return result;
    }


    @Override
    @NotNull
    public LockInfo lock(@NotNull Mindmap mindmap, @NotNull User user) throws WiseMappingException {
        return this.lock(mindmap, user, System.nanoTime());
    }

    @Override
    @NotNull
    public LockInfo lock(@NotNull Mindmap mindmap, @NotNull User user, long session) throws WiseMappingException {
        if (isLocked(mindmap) && !isLockedBy(mindmap, user)) {
            throw new LockException("Invalid lock, this should not happen");
        }

        if (!mindmap.hasPermissions(user, CollaborationRole.EDITOR)) {
            throw new AccessDeniedSecurityException("Invalid lock, this should not happen");
        }

        LockInfo result = lockInfoByMapId.get(mindmap.getId());
        if (result != null) {
            // Update timeout only...
            logger.debug("Update timestamp:" + mindmap.getId());
            updateExpirationTimeout(mindmap, user);
            result.setSession(session);
        } else {
            logger.debug("Lock map id:" + mindmap.getId());
            result = new LockInfo(user, mindmap, session);
            lockInfoByMapId.put(mindmap.getId(), result);
        }
        return result;
    }

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

}
