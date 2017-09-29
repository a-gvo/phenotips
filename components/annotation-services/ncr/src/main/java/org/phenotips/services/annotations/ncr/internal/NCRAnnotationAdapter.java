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

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.http.NameValuePair;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * An implementation of an {@link AnnotationAdapter} for the Neural Concept Recogniser.
 *
 * @version $Id$
 * @since 1.4
 */
public class NCRAnnotationAdapter implements AnnotationAdapter
{
    @Override
    public String getServiceName()
    {
        throw new NotImplementedException();
    }

    @Override
    public String getServiceURL()
    {
        throw new NotImplementedException();
    }

    @Nonnull
    @Override
    public List<NameValuePair> adaptRequest(@Nonnull final Map<String, String> params)
    {
        throw new NotImplementedException();
    }

    @Nonnull
    @Override
    public List<NameValuePair> adaptResponse(@Nonnull final Map<String, String> params)
    {
        throw new NotImplementedException();
    }
}
