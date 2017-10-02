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

import org.phenotips.rest.Relation;
import org.phenotips.services.annotations.ncr.AnnotationAdapter;
import org.phenotips.services.annotations.ncr.AnnotationAdapterFactory;
import org.phenotips.services.annotations.rest.AnnotationServicesResource;

import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiResource;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Default implementation of the {@link AnnotationServicesResource}.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Named("org.phenotips.services.annotations.rest.internal.DefaultAnnotationServicesResource")
@Singleton
@Relation("https://phenotips.org/rel/annotationServices")
public class DefaultAnnotationServicesResource extends XWikiResource implements AnnotationServicesResource
{
    private static final String ID = "id";

    private static final String LABEL = "label";

    private static final String URL = "url";

    @Inject
    private AnnotationAdapterFactory adapterFactory;

    @Override
    public Response getAllAnnotationServices()
    {
        final List<AnnotationAdapter> services = this.adapterFactory.getAll();
        final JSONArray summary = new JSONArray();
        services.forEach(service -> addServiceData(summary, service));
        return Response.ok(summary, MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Adds {@code service} data to the services {@code summary}.
     *
     * @param summary a {@link JSONArray} containing summary for all available services
     * @param service an {@link AnnotationAdapter} object for some specific annotation service
     */
    private void addServiceData(@Nonnull final JSONArray summary, @Nonnull final AnnotationAdapter service)
    {
        final String serviceId = service.getServiceName();
        final JSONObject serviceSummary = new JSONObject();
        serviceSummary.put(ID, serviceId);
        serviceSummary.put(LABEL, service.getServiceLabel());
        serviceSummary.put(URL, service.getServiceURL());
        summary.put(serviceSummary);
    }
}
