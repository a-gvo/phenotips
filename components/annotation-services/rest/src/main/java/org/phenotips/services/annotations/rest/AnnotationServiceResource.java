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

import org.phenotips.rest.ParentResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.fluent.Response;

/**
 * An annotation resource for some service of interest.
 *
 * @version $Id$
 * @since 1.4
 */
@Path("/services/{annotation-service}")
@ParentResource(AnnotationServicesResource.class)
public interface AnnotationServiceResource
{
    /**
     * Forwards the text to annotate to the {@code annotationService} of interest, in the format that is expected by
     * this {@code annotationService}. Upon receiving a response, translates it to the format that can be understood by
     * the requesting service.
     *
     * @param annotationService the annotation service of interest (e.g. ncr)
     * @return a {@link Response} containing the annotated text, in the correct format, an error code otherwise
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response annotate(@PathParam("annotation-service") String annotationService);
}
