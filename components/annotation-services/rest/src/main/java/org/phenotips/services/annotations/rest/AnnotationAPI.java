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
package org.phenotips.services.annotations.rest;

import org.xwiki.component.annotation.Role;

import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.http.client.fluent.Response;

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
     * @param service the name of the service of interest
     * @param params the form parameters
     * @return the {@link Response response}
     * @throws ServiceException if there is an error with the request, or with finding the requested annotation service
     */
    Response postForm(@Nonnull String service, @Nonnull Map<String, String> params) throws ServiceException;

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
