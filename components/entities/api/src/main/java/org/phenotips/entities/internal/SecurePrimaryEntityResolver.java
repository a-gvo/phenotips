/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.entities.internal;

import org.phenotips.entities.PrimaryEntity;
import org.phenotips.entities.PrimaryEntityManager;
import org.phenotips.entities.PrimaryEntityResolver;
import org.phenotips.security.authorization.AuthorizationService;

import org.xwiki.component.annotation.Component;
import org.xwiki.security.authorization.Right;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;

/**
 * Secure implementation of the {@link PrimaryEntityResolver} interface.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Named("secure")
@Singleton
public class SecurePrimaryEntityResolver implements PrimaryEntityResolver
{
    /** The resolver that will do the actual work. */
    @Inject
    private PrimaryEntityResolver resolver;

    /** Used for obtaining the current user. */
    @Inject
    private UserManager userManager;

    /** Used for checking access rights. */
    @Inject
    private AuthorizationService access;

    @Inject
    private Logger logger;

    @Nullable
    @Override
    public PrimaryEntity resolveEntity(@Nullable final String entityId)
    {
        final User user = this.userManager.getCurrentUser();
        final PrimaryEntity entity = this.resolver.resolveEntity(entityId);

        return checkAccess(entity, user);
    }

    @Nullable
    @Override
    public PrimaryEntityManager getEntityManager(@Nullable final String entityType)
    {
        //TODO: Fixme!!
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasEntityManager(@Nullable final String entityType)
    {
        //TODO: Fixme!!
        throw new UnsupportedOperationException();
    }

    /**
     * Checks that {@code user} has {@link Right#VIEW} for {@code entity}.
     *
     * @param entity the {@link PrimaryEntity} of interest
     * @param user the current {@link User}
     * @return the secure implementation of {@link PrimaryEntity}
     * @throws SecurityException if the {@code user} does not have {@link Right#VIEW} for {@code entity}
     */
    @Nullable
    private PrimaryEntity checkAccess(@Nullable final PrimaryEntity entity, @Nullable final User user)
    {
        return checkAccess(Right.VIEW, entity, user);
    }

    /**
     * Checks that {@code user} has {@code right} for {@code entity}.
     *
     * @param right the desired {@link Right}
     * @param entity the {@link PrimaryEntity} of interest
     * @param user the current {@link User}
     * @return the secure implementation of {@link PrimaryEntity}
     * @throws SecurityException if the {@code user} does not have {@code right} for {@code entity}
     */
    private PrimaryEntity checkAccess(
        @Nonnull final Right right,
        @Nullable final PrimaryEntity entity,
        @Nullable final User user)
    {
        if (entity != null && this.access.hasAccess(user, right, entity.getDocumentReference())) {
            return entity.getReadOnly();
        } else if (entity != null) {
            this.logger.warn("Illegal access requested for entity [{}] by user [{}]", entity.getId(), user);
            throw new SecurityException("Unauthorized access");
        }
        return null;
    }
}
