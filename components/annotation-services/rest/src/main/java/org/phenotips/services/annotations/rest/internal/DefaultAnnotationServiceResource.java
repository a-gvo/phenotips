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
import org.phenotips.services.annotations.rest.AnnotationServiceResource;
import org.phenotips.services.annotations.rest.internal.utils.AnnotationAPI;

import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.Request;
import org.xwiki.rest.XWikiResource;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;

/**
 * The default implementation of the {@link AnnotationServiceResource}.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Named("org.phenotips.services.annotations.rest.internal.DefaultAnnotationServiceResource")
@Singleton
@Relation("https://phenotips.org/rel/annotationServices")
public class DefaultAnnotationServiceResource extends XWikiResource implements AnnotationServiceResource
{
    @Inject
    private AnnotationAdapterFactory annotationAdapterFactory;

    @Inject
    private AnnotationAPI annotationAPI;

    @Inject
    private Container container;

    @Inject
    private Logger logger;

    @Override
    public Response annotate(@Nullable final String annotationService)
    {
        // An annotation service should be specified.
        if (StringUtils.isBlank(annotationService)) {
            this.logger.error("Annotation service should not be blank.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Check that the requested annotation service is valid.
        final AnnotationAdapter adapter = this.annotationAdapterFactory.build(annotationService);
        if (adapter == null) {
            this.logger.error("The requested annotations service [{}] does not exist.", annotationService);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Check that there is a request object.
        final Request request = this.container.getRequest();
        if (request == null) {
            this.logger.error("The request object should not be null.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // Annotate the data provided in the request, using the adapter.
        return annotate(request, adapter, annotationService);
    }

    /**
     * Annotates the text contained in the {@code request} using the provided {@code adapter} for the given
     * {@code annotationService annotation service}.
     *
     * @param request the {@link Request} containing the text to annotate
     * @param adapter the {@link AnnotationAdapter} that will translate between the requesting party and the
     *                annotation service
     * @param annotationService the name of the desired annotation service (e.g. ncr)
     * @return a {@link Response} containing the annotated text, an error code otherwise
     */
    @Nonnull
    private Response annotate(
        @Nonnull final Request request,
        @Nonnull final AnnotationAdapter adapter,
        @Nonnull final String annotationService)
    {
        try {
            final String adaptedRequest = adapter.adaptRequest(request);
            final ContentType contentType = adapter.getContentType();
            final String response = this.annotationAPI.post(adapter.getServiceURL(), contentType, adaptedRequest);
            final String adaptedResponse = adapter.adaptResponse(response);
            return Response.ok(adaptedResponse, MediaType.APPLICATION_JSON_TYPE).build();
        } catch (final AnnotationAPI.ServiceException e) {
            this.logger.error("Encountered an error while requesting annotations from {} service: [{}]",
                annotationService, e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (final IOException e) {
            this.logger.error("Could not process response from {} service: [{}]", annotationService, e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (final Exception e) {
            this.logger.error("Unexpected exception occurred: [{}]", e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
