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
package org.phenotips.services.annotations.ncr;

import org.xwiki.component.annotation.Role;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Constructs an annotation adapter instance that translates between the incoming request format, and the format
 * expected by the annotation service of interest.
 *
 * @version $Id$
 * @since 1.4
 */
@Role
public interface AnnotationAdapterFactory
{
    /**
     * Constructs a new {@link AnnotationAdapter} instance, that can be used to translate the incoming request into the
     * format expected by the {@code annotationService}.
     *
     * @param annotationService the annotation service of interest (e.g. ncr)
     * @return the {@link AnnotationAdapter} instance that is associated with the {@code annotationService}, or {@code
     * null} if no such instance exists
     */
    @Nullable
    AnnotationAdapter build(@Nonnull String annotationService);

    /**
     * Returns a list of all available {@link AnnotationAdapter} objects.
     *
     * @return a list of all available {@link AnnotationAdapter} objects
     */
    @Nonnull
    List<AnnotationAdapter> getAll();
}
