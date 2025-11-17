/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
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
import com.wisemapping.model.CollaborationRole;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.Account;
import org.jetbrains.annotations.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class LockManagerImpl implements LockManager {
    private static final int ONE_MINUTE_MILLISECONDS = 1000 * 60;
    // Maximum number of concurrent locks to prevent unbounded memory growth
    // This represents a reasonable limit for concurrent editing sessions
    private static final int MAX_LOCKS = 1000;
    private static final int WARN_THRESHOLD = (int) (MAX_LOCKS * 0.8); // Warn at 80% capacity
    
    private final Map<Integer, LockInfo> lockInfoByMapId;
    private final ScheduledExecutorService expirationScheduler;
    final private static Logger logger = LogManager.getLogger();

    @Override
    public boolean isLocked(@NotNull Mindmap mindmap) {
        return this.getLockInfo(mindmap) != null;
    }

    @Override
    public LockInfo getLockInfo(@NotNull Mindmap mindmap) {
        return lockInfoByMapId.get(mindmap.getId());
    }

    @Override
    public void unlockAll(@NotNull final Account user) throws LockException, AccessDeniedSecurityException {
        final Set<Integer> mapIds = lockInfoByMapId.keySet();
        for (final Integer mapId : mapIds) {
            final LockInfo lockInfo = lockInfoByMapId.get(mapId);
            if (lockInfo.getUser().identityEquality(user)) {
                unlock(mapId);
            }
        }
    }

    @Override
    public void unlock(@NotNull Mindmap mindmap, @NotNull Account user) throws LockException, AccessDeniedSecurityException {
        verifyHasLock(mindmap, user);
        this.unlock(mindmap.getId());
    }

    private void unlock(int mapId) {
        logger.debug("Unlock map id:" + mapId);
        lockInfoByMapId.remove(mapId);
    }

    @Override
    public boolean isLockedBy(@NotNull Mindmap mindmap, @NotNull Account collaborator) {
        boolean result = false;
        final LockInfo lockInfo = this.getLockInfo(mindmap);
        if (lockInfo != null && lockInfo.getUser().identityEquality(collaborator)) {
            result = true;
        }
        return result;
    }


    @Override
    public long generateSession() {
        return System.nanoTime();
    }

    @NotNull
    @Override
    public LockInfo lock(@NotNull Mindmap mindmap, @NotNull Account user) throws LockException {
        if (isLocked(mindmap) && !isLockedBy(mindmap, user)) {
            throw LockException.createLockLost(mindmap, user, this);
        }

        // Do I need to create a new lock ?
        LockInfo result = lockInfoByMapId.get(mindmap.getId());
        if (result == null) {
            // Check if we're approaching the maximum limit
            int currentSize = lockInfoByMapId.size();
            if (currentSize >= MAX_LOCKS) {
                logger.error("Maximum lock limit ({}) reached. Cannot create new lock for mindmap {}. " +
                           "This may indicate expired locks are not being cleaned up properly.",
                           MAX_LOCKS, mindmap.getId());
                throw new LockException("Maximum concurrent locks reached. Please try again later.");
            }
            
            if (currentSize >= WARN_THRESHOLD) {
                logger.warn("Lock map size ({}) approaching maximum limit ({}). " +
                          "Consider investigating if locks are being properly expired.",
                          currentSize, MAX_LOCKS);
            }
            
            logger.debug("Creating new lock for map id:" + mindmap.getId() + " (current locks: " + currentSize + ")");
            result = new LockInfo(user, mindmap);
            lockInfoByMapId.put(mindmap.getId(), result);
        }

        // Update timestamp ...
        logger.debug("Updating timeout:" + result);
        result.updateTimeout();

        return result;
    }

    private void verifyHasLock(@NotNull Mindmap mindmap, @NotNull Account user) throws LockException, AccessDeniedSecurityException {
        // Only editor can have lock ...
        if (!mindmap.hasPermissions(user, CollaborationRole.EDITOR)) {
            throw new AccessDeniedSecurityException(mindmap.getId(), user);
        }

        // Is the lock assigned to the user ...
        if (isLocked(mindmap) && !isLockedBy(mindmap, user)) {
            throw LockException.createLockLost(mindmap, user, this);
        }
    }

    public LockManagerImpl() {
        lockInfoByMapId = new ConcurrentHashMap<>();
        // Use daemon thread to prevent JVM shutdown issues
        expirationScheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "LockExpirationScheduler");
            t.setDaemon(true);
            return t;
        });
        
        expirationScheduler.scheduleAtFixedRate(() -> {
            synchronized (lockInfoByMapId) {
                int sizeBefore = lockInfoByMapId.size();
                logger.debug("Lock expiration scheduler started. Current locks: {} (size: {})", 
                           lockInfoByMapId.keySet(), sizeBefore);
                
                // Search for expired sessions and remove them ....
                int expiredCount = 0;
                for (Integer mapId : lockInfoByMapId.keySet()) {
                    LockInfo lockInfo = lockInfoByMapId.get(mapId);
                    if (lockInfo != null && lockInfo.isExpired()) {
                        unlock(mapId);
                        expiredCount++;
                    }
                }
                
                int sizeAfter = lockInfoByMapId.size();
                if (expiredCount > 0) {
                    logger.debug("Expired and removed {} locks. Size: {} -> {}", expiredCount, sizeBefore, sizeAfter);
                }
                
                // Log warning if map size is still high after cleanup
                if (sizeAfter >= WARN_THRESHOLD) {
                    logger.warn("Lock map size ({}) remains high after cleanup. " +
                              "This may indicate locks are not expiring properly or there are many concurrent sessions.",
                              sizeAfter);
                }
            }
        }, ONE_MINUTE_MILLISECONDS, ONE_MINUTE_MILLISECONDS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Shutdown the expiration scheduler. Should be called when the LockManager is no longer needed.
     * This prevents memory leaks from the scheduler thread.
     */
    public void shutdown() {
        if (expirationScheduler != null && !expirationScheduler.isShutdown()) {
            logger.info("Shutting down lock expiration scheduler");
            expirationScheduler.shutdown();
            try {
                if (!expirationScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("Lock expiration scheduler did not terminate gracefully, forcing shutdown");
                    expirationScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for lock expiration scheduler to terminate");
                expirationScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
