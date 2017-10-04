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
import org.phenotips.services.annotations.ncr.AnnotationAdapterFactory;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultAnnotationAdapterFactory}.
 */
public class DefaultAnnotationAdapterFactoryTest
{
    private static final String WRONG = "wrong";

    private static final String NCR_LABEL = "ncr";

    private static final AnnotationAdapter NCR = new NCRAnnotationAdapter();

    @Rule
    public MockitoComponentMockingRule<AnnotationAdapterFactory> mocker =
        new MockitoComponentMockingRule<>(DefaultAnnotationAdapterFactory.class);

    private AnnotationAdapterFactory component;

    private ComponentManager componentManager;

    @Before
    public void setUp() throws ComponentLookupException
    {
        this.component = this.mocker.getComponentUnderTest();

        this.componentManager = this.mocker.getInstance(ComponentManager.class, "context");
    }

    @Test
    public void buildCatchesComponentLookupException() throws ComponentLookupException
    {
        when(this.componentManager.getInstance(eq(AnnotationAdapter.class), anyString()))
            .thenThrow(new ComponentLookupException(WRONG));
        Assert.assertNull(this.component.build(WRONG));
    }

    @Test
    public void buildReturnsNullIfServiceIsInvalid() throws ComponentLookupException
    {
        when(this.componentManager.getInstance(eq(AnnotationAdapter.class), anyString())).thenReturn(null);
        Assert.assertNull(this.component.build(WRONG));
    }

    @Test
    public void buildReturnsReturnsCorrectAnnotationAdapter() throws ComponentLookupException
    {
        when(this.componentManager.getInstance(AnnotationAdapter.class, NCR_LABEL)).thenReturn(NCR);
        Assert.assertEquals(NCR, this.component.build(NCR_LABEL));
    }

    @Test
    public void getAllCatchesComponentLookupException() throws ComponentLookupException
    {
        when(this.componentManager.getInstanceList(AnnotationAdapter.class))
            .thenThrow(new ComponentLookupException(WRONG));
        Assert.assertTrue(this.component.getAll().isEmpty());
    }

    @Test
    public void getAllReturnsEmptyListIfNoAnnotationAdaptersAvailable() throws ComponentLookupException
    {
        when(this.componentManager.getInstanceList(AnnotationAdapter.class)).thenReturn(Collections.emptyList());
        Assert.assertTrue(this.component.getAll().isEmpty());
    }

    @Test
    public void getAllReturnsListOfAvailableAnnotationAdapters() throws ComponentLookupException
    {
        when(this.componentManager.getInstanceList(AnnotationAdapter.class)).thenReturn(Collections.singletonList(NCR));
        Assert.assertEquals(Collections.singletonList(NCR), this.component.getAll());
    }
}
