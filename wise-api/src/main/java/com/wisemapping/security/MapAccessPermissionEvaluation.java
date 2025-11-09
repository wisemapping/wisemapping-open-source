package com.wisemapping.security;

import com.wisemapping.model.Collaborator;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.Account;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;


public class MapAccessPermissionEvaluation implements PermissionEvaluator {
    final private static Logger logger = LogManager.getLogger();

    private MapPermissionsSecurityAdvice readAdvice;

    private MapPermissionsSecurityAdvice updateAdvice;

    public MapAccessPermissionEvaluation(final @NotNull MapPermissionsSecurityAdvice readAdvice, final @NotNull MapPermissionsSecurityAdvice updateAdvice) {
        this.readAdvice = readAdvice;
        this.updateAdvice = updateAdvice;
    }

    @Override
    public boolean hasPermission(
            @NotNull Authentication auth, @NotNull Object targetDomainObject, @NotNull Object permission) {

        logger.log(Level.DEBUG, "auth: " + auth + ",targetDomainObject:" + targetDomainObject + ",permission:" + permission);
        
        // Validate parameters (except auth can be null for READ operations)
        if (targetDomainObject == null || !(permission instanceof String)) {
            logger.debug("Permissions could not be validated, illegal parameters.");
            return false;
        }

        final Account user = Utils.getUser();
        final MapAccessPermission perm;
        try {
            perm = MapAccessPermission.valueOf((permission.toString().toUpperCase()));
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid permission: " + permission);
            return false;
        }

        // For WRITE operations, authentication is required
        if (perm == MapAccessPermission.WRITE && (auth == null || !auth.isAuthenticated())) {
            logger.debug("Write operations require authentication.");
            return false;
        }

        // For Collaborator checks, user must be authenticated
        if (targetDomainObject instanceof Collaborator collab) {
            if (auth == null || !auth.isAuthenticated() || user == null) {
                logger.debug("Collaborator checks require authentication.");
                return false;
            }
            // Read only operations checks ...
            return user.identityEquality(collab) || readAdvice.getMindmapService().isAdmin(user);
        }

        // For READ operations, allow null auth (will check if map is public)
        // For WRITE operations, auth was already validated above
        boolean result;
        if (targetDomainObject instanceof Integer) {
            // Checking permissions by mapId ...
            final int mapId = (Integer) targetDomainObject;
            result = hasPrivilege(mapId, perm);
        } else if (targetDomainObject instanceof Mindmap) {
            final Mindmap map = (Mindmap) targetDomainObject;
            result = hasPrivilege(map, perm);
        } else {
            throw new IllegalArgumentException("Unsupported check control of permissions");
        }

        if (!result) {
            logger.debug("User '" + (user != null ? user.getEmail() : "none") + "' not allowed to invoke");
        }
        return result;
    }


    @Override
    public boolean hasPermission(
            @NotNull Authentication auth, Serializable targetId, @NotNull String targetType, @NotNull Object
            permission) {
        logger.log(Level.FATAL, "Unsupported privilege: auth: " + auth + ",targetId:" + targetType + ",targetType:" + targetType + ", permission:" + permission);
        return false;
    }

    private boolean hasPrivilege(@NotNull int mapId, @NotNull MapAccessPermission permission) {
        boolean result;
        final Account user = Utils.getUser();
        if (MapAccessPermission.READ == permission) {
            result = readAdvice.isAllowed(user, mapId);
        } else {
            result = updateAdvice.isAllowed(user, mapId);
        }
        return result;
    }

    private boolean hasPrivilege(@NotNull Mindmap map, @NotNull MapAccessPermission permission) {
        boolean result;
        final Account user = Utils.getUser();
        if (MapAccessPermission.READ == permission) {
            result = readAdvice.isAllowed(user, map);
        } else {
            result = updateAdvice.isAllowed(user, map);
        }
        return result;
    }
}