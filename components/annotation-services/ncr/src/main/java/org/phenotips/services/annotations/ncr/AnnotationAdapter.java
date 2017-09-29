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

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.http.NameValuePair;

/**
 * An adapter between data formats expected by the clinical text analysis extension and some {@link #getServiceName()}
 * service of interest.
 *
 * @version $Id$
 * @since 1.4
 */
public interface AnnotationAdapter
{
    /**
     * The name of the service represented by this instance (e.g. ncr).
     *
     * @return the name of the service, as as string
     */
    String getServiceName();

    /**
     * Return the service URL for the annotation service.
     *
     * @return the endpoint being used by this instance
     */
    String getServiceURL();

    /**
     * Adapts the request parameters to the format expected by {@link #getServiceName()}.
     *
     * @param params a list name-value pairs to be adapted
     * @return a list of name-value pairs, in format expected by {@link #getServiceName()}
     */
    @Nonnull
    List<NameValuePair> adaptRequest(@Nonnull Map<String, String> params);

    @Nonnull
    List<NameValuePair> adaptResponse(@Nonnull Map<String, String> params);
}
