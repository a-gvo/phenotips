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
import org.xwiki.container.Request;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * An adapter between data formats expected by the clinical text analysis extension and some {@link #getServiceName()}
 * service of interest.
 *
 * @version $Id$
 * @since 1.4
 */
@Role
public interface AnnotationAdapter
{
    /**
     * The name of the service represented by this instance (e.g. ncr).
     *
     * @return the name of the service, as as string
     */
    @Nonnull
    String getServiceName();

    /**
     * The full service name (e.g. Neural Concept Recognizer).
     *
     * @return the full service name, as string
     */
    @Nonnull
    String getServiceLabel();

    /**
     * Return the service URL for the annotation service.
     *
     * @return the endpoint being used by this instance
     */
    @Nonnull
    String getServiceURL();

    /**
     * Returns the entity {@link ContentType}.
     *
     * @return the {@link ContentType}
     */
    ContentType getContentType();

    /**
     * Adapts the request parameters to the format expected by {@link #getServiceName()}.
     *
     * @param request the incoming request
     * @return a list of name-value pairs, in format expected by {@link #getServiceName()}
     * @throws JsonProcessingException if there was an error converting request data to json string
     */
    @Nonnull
    String adaptRequest(@Nonnull Request request) throws JsonProcessingException;

    /**
     * Adapts the response returned by service to the desired format.
     *
     * @param response the response from {@link #getServiceURL()} service, as string
     * @return the adapted response, as string
     * @throws IOException if {@code response} could not be adapted to the desired format
     */
    @Nonnull
    String adaptResponse(@Nonnull String response) throws IOException;
}
