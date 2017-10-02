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
package org.phenotips.services.annotations.ncr.internal;

import org.phenotips.services.annotations.ncr.AnnotationAdapter;
import org.phenotips.services.annotations.ncr.AnnotationAdapterFactory;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Default implementation of the {@link AnnotationAdapterFactory}.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Singleton
public class DefaultAnnotationAdapterFactory implements AnnotationAdapterFactory
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManager;

    @Nullable
    @Override
    public AnnotationAdapter build(@Nonnull final String annotationService)
    {
        try {
            return this.componentManager.get().getInstance(AnnotationAdapter.class, annotationService);
        } catch (final ComponentLookupException e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public List<AnnotationAdapter> getAll()
    {
        try {
            return this.componentManager.get().getInstanceList(AnnotationAdapter.class);
        } catch (ComponentLookupException e) {
            return Collections.emptyList();
        }
    }
}
