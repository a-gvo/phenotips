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
package org.phenotips.services.annotations.rest.internal.utils;

import org.xwiki.component.annotation.Role;

import javax.annotation.Nonnull;

import org.apache.http.entity.ContentType;

/**
 * Interacts with an annotation REST API endpoint.
 *
 * @version $Id$
 * @since 1.4
 */
@Role
public interface AnnotationAPI
{
    /**
     * Post the {@code request} parameters to the {@code serviceUrl} provided.
     *
     * @param serviceUrl the url for service of interest
     * @param contentType the {@link ContentType} for the {@code request}
     * @param request the string containing request parameters
     * @return the response, as string
     * @throws ServiceException if there is an error with the request
     */
    String post(@Nonnull String serviceUrl, @Nonnull ContentType contentType, @Nonnull String request)
        throws ServiceException;

    /**
     * An exception returned by the annotation api.
     * @version $Id$
     * @since 1.4
     */
    class ServiceException extends Exception
    {
        /**
         * Default constructor with message.
         *
         * @param message the message
         */
        public ServiceException(String message)
        {
            super(message);
        }

        /**
         * Constructor with message and cause.
         *
         * @param message the message
         * @param cause exception cause
         */
        public ServiceException(String message, Exception cause)
        {
            super(message, cause);
        }
    }
}
