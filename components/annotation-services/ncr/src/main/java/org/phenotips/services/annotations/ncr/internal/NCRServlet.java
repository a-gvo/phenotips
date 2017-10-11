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

import org.phenotips.services.annotations.ncr.internal.utils.NCRAnnotationUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple servlet for translating the requests generated by clinical-text-analysis-extension, to the format expected
 * by the Neural Concept Recognizer service, and translating the received responses to the format expected by the
 * clinical-text-analysis-extension.
 *
 * @version $Id$
 * @since 1.4
 */
public class NCRServlet extends HttpServlet
{
    /** The logging object. */
    private static final Logger LOGGER = LoggerFactory.getLogger(NCRServlet.class);

    private static final long serialVersionUID = -8185410465881809448L;

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    {
        try {
            final String text = NCRAnnotationUtils.doPost(request);
            // Set content type.
            response.setContentType(MediaType.APPLICATION_JSON);
            final PrintWriter writer = response.getWriter();
            writer.append(text);
        } catch (final URISyntaxException e) {
            LOGGER.error("Invalid URI syntax: {}.", e.getMessage());
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } catch (final MalformedURLException e) {
            LOGGER.error("The NCR service url [{}] is invalid: {}", NCRAnnotationUtils.getServiceUrl(), e.getMessage());
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } catch (final IOException e) {
            LOGGER.error("There was an error processing the request: {}", e.getMessage());
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } catch (final Exception e) {
            LOGGER.error("An unexpected error occurred: {}", e.getMessage());
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }
}
