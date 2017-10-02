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

import org.xwiki.component.annotation.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.google.common.annotations.VisibleForTesting;

/**
 * The default implementation of the {@link AnnotationAPI}.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Singleton
public class DefaultAnnotationAPI implements AnnotationAPI
{
    @Override
    public String post(
        @Nonnull final String serviceUrl,
        @Nonnull final ContentType contentType,
        @Nonnull final String request)
        throws ServiceException
    {
        try {
            final URI uri = new URL(serviceUrl).toURI();
            return getPostRequest(uri)
                .bodyString(request, contentType)
                .execute()
                .returnContent()
                .asString();
        } catch (final URISyntaxException | IOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /**
     * Gets the {@link Request} object for a post request.
     *
     * @param uri the {@link URI} for the service
     * @return the {@link Request} object
     */
    @VisibleForTesting
    Request getPostRequest(@Nonnull final URI uri)
    {
        return Request.Post(uri);
    }
}
