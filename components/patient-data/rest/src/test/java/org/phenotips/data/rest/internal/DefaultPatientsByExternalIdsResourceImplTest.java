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

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.query.internal.DefaultQuery;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultPatientsByExternalIdsResourceImplTest
{
    @Rule
    public final MockitoComponentMockingRule<PatientsByExternalIdsResource> mocker =
        new MockitoComponentMockingRule<PatientsByExternalIdsResource>(DefaultPatientsByExternalIdsResourceImpl.class);

    @Mock
    private Patient patient1;

    @Mock
    private Patient patient2;

    @Mock
    private Patient patient3;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @Mock
    private User user;

    @Mock
    private Logger logger;

    private QueryManager qm;

    private AuthorizationManager access;

    private DefaultPatientsByExternalIdsResourceImpl component;

    private final String eid1 = "eid1";

    private final String eid2 = "eid2";

    private DocumentReference userReference = new DocumentReference("wiki", "XWiki", "padams");

    private DocumentReference patientReference1 = new DocumentReference("wiki", "data", "P0000001");

    private DocumentReference patientReference2 = new DocumentReference("wiki", "data", "P0000002");

    private DocumentReference patientReference3 = new DocumentReference("wiki", "data", "P0000003");

    private JSONObject patient1JSON = new JSONObject().put("id", "P0000001");

    private JSONObject patient2JSON = new JSONObject().put("id", "P0000002");

    private JSONObject patient3JSON = new JSONObject().put("id", "P0000003");

    @Before
    public void setUp() throws ComponentLookupException, URISyntaxException
    {
        MockitoAnnotations.initMocks(this);
        final Execution execution = mock(Execution.class);
        final ExecutionContext executionContext = mock(ExecutionContext.class);
        final ComponentManager componentManager = this.mocker.getInstance(ComponentManager.class, "context");
        when(componentManager.getInstance(Execution.class)).thenReturn(execution);
        doReturn(executionContext).when(execution).getContext();
        doReturn(mock(XWikiContext.class)).when(executionContext).getProperty("xwikicontext");

        final PatientRepository repository = this.mocker.getInstance(PatientRepository.class);
        this.qm = this.mocker.getInstance(QueryManager.class);
        this.access = this.mocker.getInstance(AuthorizationManager.class);
        final UserManager users = this.mocker.getInstance(UserManager.class);
        this.component = (DefaultPatientsByExternalIdsResourceImpl) this.mocker.getComponentUnderTest();
        this.logger = this.mocker.getMockedLogger();
        ReflectionUtils.setFieldValue(this.component, "uriInfo", this.uriInfo);

        doReturn(this.uriBuilder).when(this.uriInfo).getBaseUriBuilder();
        final String id1 = "P0000001";
        when(this.patient1.getId()).thenReturn(id1);
        when(this.patient1.getDocument()).thenReturn(this.patientReference1);
        final String id2 = "P0000002";
        when(this.patient2.getId()).thenReturn(id2);
        when(this.patient2.getDocument()).thenReturn(this.patientReference2);
        final String id3 = "P0000003";
        when(this.patient3.getId()).thenReturn(id3);
        when(this.patient3.getDocument()).thenReturn(this.patientReference3);
        when(users.getCurrentUser()).thenReturn(this.user);
        when(this.user.getProfileDocument()).thenReturn(this.userReference);
        when(repository.get("P0000001")).thenReturn(this.patient1);
        when(repository.get("P0000002")).thenReturn(this.patient2);
        when(repository.get("P0000003")).thenReturn(this.patient3);
        when(repository.get("P0000004")).thenThrow(new IllegalArgumentException());
        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference1)).thenReturn(true);
        when(this.access.hasAccess(Right.EDIT, this.userReference, this.patientReference1)).thenReturn(true);
        when(this.access.hasAccess(Right.DELETE, this.userReference, this.patientReference1)).thenReturn(true);
        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference2)).thenReturn(true);
        when(this.access.hasAccess(Right.EDIT, this.userReference, this.patientReference2)).thenReturn(true);
        when(this.access.hasAccess(Right.DELETE, this.userReference, this.patientReference2)).thenReturn(true);
        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference3)).thenReturn(true);
        when(this.access.hasAccess(Right.EDIT, this.userReference, this.patientReference3)).thenReturn(true);
        when(this.access.hasAccess(Right.DELETE, this.userReference, this.patientReference3)).thenReturn(true);
    }

    @Test
    public void getPatientsWithNullEidsReturnsBadRequestCode() throws ComponentLookupException
    {
        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference1)).thenReturn(false);

        final Response response = this.component.getPatients(null);
        verify(this.logger).warn("The list of external IDs is null");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void getPatientsPerformsCorrectlyOnePatientRecord() throws ComponentLookupException, QueryException
    {
        final Query query = mock(DefaultQuery.class);
        when(this.patient1.toJSON()).thenReturn(this.patient1JSON);
        when(this.qm.createQuery(Matchers.anyString(), Matchers.anyString())).thenReturn(query);
        when(query.execute()).thenReturn(ImmutableList.<Object>of("P0000001"));

        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference1)).thenReturn(true);

        final Response response = this.component.getPatients(ImmutableList.of(this.eid1));
//        final Response response = this.component.getPatients(new JSONArray().put(this.eid1).toString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(new JSONArray().put(this.patient1JSON).toString(), response.getEntity());
    }

    @Test
    public void getPatientsPerformsCorrectlySeveralPatientRecordsAccessToAll() throws ComponentLookupException,
        QueryException
    {
        final Query query = mock(DefaultQuery.class);
        when(this.patient1.toJSON()).thenReturn(this.patient1JSON);
        when(this.patient2.toJSON()).thenReturn(this.patient2JSON);
        when(this.patient3.toJSON()).thenReturn(this.patient3JSON);

        when(this.qm.createQuery(Matchers.anyString(), Matchers.anyString())).thenReturn(query);
        // Note: patient records shouldn't have multiple external identifiers.
        when(query.execute()).thenReturn(ImmutableList.<Object>of("P0000001", "P0000002", "P0000003"));

        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference1)).thenReturn(true);
        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference2)).thenReturn(true);
        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference3)).thenReturn(true);

        final Response response = this.component.getPatients(ImmutableList.of(this.eid1, this.eid2));
//        final Response response = this.component.getPatients(new JSONArray().put(this.eid1).put(this.eid2).toString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(new JSONArray().put(this.patient1JSON).put(this.patient2JSON).put(this.patient3JSON).toString(),
            response.getEntity());
    }

    @Test
    public void getPatientsPerformsCorrectlySeveralPatientRecordsAccessToSome() throws ComponentLookupException,
        QueryException
    {
        final Query query = mock(DefaultQuery.class);
        when(this.patient1.toJSON()).thenReturn(this.patient1JSON);
        when(this.patient2.toJSON()).thenReturn(this.patient2JSON);
        when(this.patient3.toJSON()).thenReturn(this.patient3JSON);

        when(this.qm.createQuery(Matchers.anyString(), Matchers.anyString())).thenReturn(query);
        // Note: patient records shouldn't have multiple external identifiers.
        when(query.execute()).thenReturn(ImmutableList.<Object>of("P0000001", "P0000002", "P0000003"));

        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference1)).thenReturn(true);
        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference2)).thenReturn(false);
        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference3)).thenReturn(true);

        final Response response = this.component.getPatients(ImmutableList.of(this.eid1, this.eid2));
//        final Response response = this.component.getPatients(new JSONArray().put(this.eid1).put(this.eid2).toString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(new JSONArray().put(this.patient1JSON).put(this.patient3JSON).toString(), response.getEntity());
    }

    @Test
    public void getPatientsPerformsCorrectlyOnePatientIdDoesNotExist() throws ComponentLookupException, QueryException
    {
        final Query query = mock(DefaultQuery.class);
        when(this.patient1.toJSON()).thenReturn(this.patient1JSON);
        when(this.patient2.toJSON()).thenReturn(this.patient2JSON);
        when(this.patient3.toJSON()).thenReturn(this.patient3JSON);

        when(this.qm.createQuery(Matchers.anyString(), Matchers.anyString())).thenReturn(query);
        when(query.execute()).thenReturn(ImmutableList.<Object>of("P0000001", "P0000002", "P0000003", "P0000004"));

        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference1)).thenReturn(true);
        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference2)).thenReturn(true);
        when(this.access.hasAccess(Right.VIEW, this.userReference, this.patientReference3)).thenReturn(true);

        final Response response = this.component.getPatients(ImmutableList.of(this.eid1, this.eid2));
//        final Response response = this.component.getPatients(new JSONArray().put(this.eid1).put(this.eid2).toString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(new JSONArray().put(this.patient1JSON).put(this.patient2JSON).put(this.patient3JSON).toString(),
            response.getEntity());
    }

    @Test
    public void getPatientsPerformsCorrectlyIfWrongExternalId() throws ComponentLookupException, QueryException
    {
        final Query query = mock(DefaultQuery.class);

        when(this.qm.createQuery(Matchers.anyString(), Matchers.anyString())).thenReturn(query);
        when(query.execute()).thenThrow(new QueryException("Exception when executing query", query, new XWikiException()));

        final Response response = this.component.getPatients(ImmutableList.of(this.eid1));
//        final Response response = this.component.getPatients(new JSONArray().put(this.eid1).put(this.eid2).toString());

        verify(this.logger).warn("Failed to retrieve patient with external id [{}]: {}", this.eid1,
            "Exception when executing query. Query statement = [null]");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(new JSONArray().toString(), response.getEntity());
    }
}
