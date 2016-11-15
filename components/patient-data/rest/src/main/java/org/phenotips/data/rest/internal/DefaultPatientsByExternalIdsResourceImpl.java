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
package org.phenotips.data.rest.internal;

import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.phenotips.data.rest.PatientsByExternalIdsResource;
import org.phenotips.entities.PrimaryEntity;

import org.xwiki.component.annotation.Component;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.rest.XWikiResource;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

/**
 * Default implementation for {@link PatientsByExternalIdsResource} using XWiki's support for REST resources.
 *
 * @version $Id$
 * @since 1.3RC1
 */
@Component
@Named("org.phenotips.data.rest.internal.DefaultPatientsByExternalIdsResourceImpl")
@Singleton
public class DefaultPatientsByExternalIdsResourceImpl extends XWikiResource implements PatientsByExternalIdsResource
{
    /** Jackson object mapper to facilitate array serialization. */
    private static final ObjectMapper OBJECT_MAPPER = getCustomObjectMapper();


    /** Logging helper object. */
    @Inject
    private Logger logger;

    @Inject
    private QueryManager qm;

    @Inject
    private PatientRepository repository;

    @Inject
    private AuthorizationManager access;

    @Inject
    private UserManager users;

    @Override
    public Response getPatients(final List<String> eids)
    {
        if (eids == null) {
            logger.warn("The list of external IDs is null");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

//        final List<String> eidList;
//        try {
//            eidList = OBJECT_MAPPER.readValue(eids, TypeFactory.defaultInstance()
//                .constructCollectionType(List.class, String.class));
//        } catch (IOException e) {
//            logger.warn("Invalid input: {}", eids);
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        }

        this.logger.debug("Retrieving patient records with external IDs [{}] via REST", eids);
        // Obtain all the relevant patient objects given a list of eIDs.
        final Set<PrimaryEntity> patients = FluentIterable.from(eids)
            .filter(Predicates.<String>notNull())
            .transformAndConcat(getPatientLookupFunction())
            .toSet();

        try {
            // Generate JSON for all retrieved patients.
            final String json = OBJECT_MAPPER.writeValueAsString(patients);
            return Response.ok(json, MediaType.APPLICATION_JSON_TYPE).build();
        } catch (final JsonProcessingException ex) {
            logger.warn("Failed to serialize patients [{}] to JSON: {}", eids, ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets the function that performs a query using the given patient eid, and returns an iterable containing the
     * relevant patient entity objects.
     *
     * @return an iterable containing all the relevant patient entity objects.
     */
    @Nonnull
    private Function<String, Iterable<PrimaryEntity>> getPatientLookupFunction()
    {
        return new Function<String, Iterable<PrimaryEntity>>()
        {
            @Override
            public Iterable<PrimaryEntity> apply(final String eid)
            {
                try {
                    final Query q = qm.createQuery("where doc.object(PhenoTips.PatientClass)"
                        + ".external_id = :eid", Query.XWQL);
                    q.bindValue("eid", eid);
                    final List<String> patientIds = q.execute();
                    return getMatchingPatientData(patientIds);
                } catch (final QueryException ex) {
                    logger.warn("Failed to retrieve patient with external id [{}]: {}", eid, ex.getMessage());
                }
                return Collections.emptyList();
            }
        };
    }

    /**
     * Gets patient entities, given a list of patient IDs. Any patient IDs that are not in the database will
     * be ignored.
     *
     * @param patientIds the list of patient IDs of interest -- should not be null
     * @return an iterable containing patient entity objects
     */
    @Nonnull
    private Iterable<PrimaryEntity> getMatchingPatientData(@Nonnull final List<String> patientIds)
    {
        return FluentIterable.from(patientIds).transform(new Function<String, PrimaryEntity>()
        {
            @Override
            public PrimaryEntity apply(final String patientId)
            {
                return getPatientJSON(patientId);
            }
        }).filter(Predicates.<PrimaryEntity>notNull());
    }

    /**
     * Returns the patient entity if it exists and if the user has view rights. Otherwise returns null.
     *
     * @param patientId the ID of the patient of interest
     * @return the patient entity with the specified ID, if exists, null otherwise
     */
    @Nullable
    private PrimaryEntity getPatientJSON(final String patientId)
    {
        try {
            final PrimaryEntity patient = repository.get(patientId);
            final User currentUser = this.users.getCurrentUser();
            // If the user has view rights, return the patient entity. Else return null.
            if (this.access.hasAccess(Right.VIEW, currentUser == null ? null : currentUser.getProfileDocument(),
                patient.getDocument())) {
                return patient;
            }
            this.logger.debug("View access denied to user [{}] on patient record [{}]", currentUser, patientId);
        } catch (final IllegalArgumentException ex) {
            logger.warn("Failed to retrieve patient with ID [{}]: {}", patientId, ex.getMessage());
        }
        return null;
    }

    /**
     * A custom object mapper to facilitate serializing a list of {@link Patient} objects.
     *
     * @return an object mapper that can serialize {@link Patient} objects
     */
    private static ObjectMapper getCustomObjectMapper()
    {
        final ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule m = new SimpleModule("org.json", new Version(1, 0, 0, "", "org.json", "json"));
        m.addSerializer(PrimaryEntity.class, new PrimaryEntitySerializer());
        objectMapper.registerModule(m);
        return objectMapper;
    }

    /**
     * A custom serializer for primary entities.
     */
    private static final class PrimaryEntitySerializer extends JsonSerializer<PrimaryEntity>
    {
        @Override
        public void serialize(final PrimaryEntity primaryEntity, final JsonGenerator jgen,
            final SerializerProvider provider) throws IOException
        {
            jgen.writeRawValue(primaryEntity.toJSON().toString());
        }
    }
}
