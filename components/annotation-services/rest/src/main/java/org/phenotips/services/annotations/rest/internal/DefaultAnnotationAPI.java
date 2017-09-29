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
package org.phenotips.services.annotations.rest.internal;

import org.phenotips.services.annotations.ncr.AnnotationAdapter;
import org.phenotips.services.annotations.ncr.AnnotationAdapterFactory;
import org.phenotips.services.annotations.rest.AnnotationAPI;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

/**
 * The default implementation of the {@link AnnotationAPI}.
 *
 * @version $Id$
 * @since 1.4
 */
public class DefaultAnnotationAPI implements AnnotationAPI
{
    /**
     * The charset to use when sending requests.
     */
    private static final Charset CHARSET = Charset.forName("UTF-8");

    @Inject
    private AnnotationAdapterFactory annotationAdapterFactory;

    @Override
    public Response postForm(@Nonnull final String service, @Nonnull final Map<String, String> params)
        throws ServiceException
    {

    }
}
