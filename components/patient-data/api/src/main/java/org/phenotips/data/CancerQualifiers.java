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
package org.phenotips.data;

import org.xwiki.stability.Unstable;

import java.util.Collection;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.json.JSONObject;

/**
 * Information about a {@link Patient patient}'s {@link Cancer cancer} properties (qualifiers).
 *
 * @version $Id$
 * @since 1.4
 */
@Unstable
public interface CancerQualifiers extends VocabularyProperty
{
    /**
     * The supported qualifier types.
     */
    enum Type
    {
        /** The age at which the cancer is diagnosed. */
        AGE_AT_DIAGNOSIS("ageAtDiagnosis"),
        /** The numeric age estimate at which the cancer is diagnosed. */
        NUMERIC_AGE_AT_DIAGNOSIS("numericAgeAtDiagnosis"),
        /** The type of cancer -- can be primary or metastasized. */
        PRIMARY("primary"),
        /** The localization with respect to the side of the body of the specified cancer. */
        LATERALITY("laterality");

        /** @see #getName() */
        private final String name;

        /**
         * Constructor that initializes the {@link #getName() qualifier name}.
         *
         * @param name a qualifier name
         * @see #getName()
         */
        Type(final String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return this.name().toLowerCase(Locale.ROOT);
        }

        /**
         * Get the vocabulary term identifier associated to this type of qualifier.
         *
         * @return an identifier, in the format {@code VOCABULARY:termId}
         */
        public String getName()
        {
            return this.name;
        }
    }

    /**
     * Returns a collection of all properties for the {@link CancerQualifiers} object.
     *
     * @return a collection of all property names as strings
     */
    @Nonnull
    Collection<String> getProperties();

    /**
     * Gets the value for {@code property}.
     *
     * @param property the property of interest
     * @return the value associated with the {@code property}; null if no such value exists
     */
    @Nullable
    Object getPropertyValue(@Nullable String property);

    /**
     * Retrieve information about these qualifiers in a JSON format. For example:
     *
     * <pre>
     * {
     *   "ageAtDiagnosis": "before_40",
     *   "numericAgeAtDiagnosis": 31,
     *   "primary": true,
     *   "laterality": "l"
     * }
     * </pre>
     *
     * @return the meta-feature data, using the org.json classes
     */
    @Override
    JSONObject toJSON();
}
