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
package org.phenotips.data.rest;

import org.phenotips.rest.ParentResource;
import org.phenotips.rest.Relation;
import org.phenotips.rest.RequiredAccess;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource for working with multiple patient records, identified by their given "external" identifiers.
 *
 * @version $Id$
 * @since 1.3RC1
 */
@Path("/patients/eids")
@Relation("https://phenotips.org/rel/patientRecord")
@ParentResource(PatientsResource.class)
public interface PatientsByExternalIdsResource
{
    /**
     * Retrieve multiple patient records, identified by their given "external" identifiers, in their JSON
     * representation. If any of the indicated patient records don't exist, or if the user sending the request doesn't
     * have the right to view any of the target patient records, they are excluded from the search results.
     *
     * @param eids JSON list of patients' "external" identifiers, see {@link org.phenotips.data.Patient#getExternalId()}
     * @return JSON representations of the requested patients, or a status message in case of error
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequiredAccess("view")
    Response getPatients(@QueryParam("eids") final List<String> eids);
}
