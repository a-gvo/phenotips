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
package org.phenotips.panels.rest.internal;

import org.phenotips.panels.GenePanel;
import org.phenotips.panels.internal.DefaultGenePanelFactoryImpl;
import org.phenotips.panels.rest.GenePanelsResource;
import org.phenotips.vocabulary.VocabularyManager;

import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.Request;
import org.xwiki.rest.XWikiResource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

/**
 * Default implementation of the {@link GenePanelsResource}.
 *
 * @version $Id$
 * @since 1.3M5
 */
@Component
@Named("org.phenotips.panels.rest.internal.DefaultGenePanelsResourceImpl")
@Singleton
public class DefaultGenePanelsResourceImpl extends XWikiResource implements GenePanelsResource
{
    private static final String REQ_NO = "reqNo";
    @Inject
    private Container container;

    @Inject
    private VocabularyManager vocabularyManager;

    @Override
    public Response getGeneCountsFromPhenotypes()
    {
        final Request request = this.container.getRequest();
        @SuppressWarnings("unchecked")
        final List<String> termIds = (List<String>) (List<?>) request.getProperties("id");
        final Object reqNo = request.getProperty(REQ_NO);
        final GenePanel panel = new DefaultGenePanelFactoryImpl().makeGenePanel(termIds, this.vocabularyManager);

        final JSONObject geneCounts = panel.toJSON();
        geneCounts.put(REQ_NO, reqNo);
        return Response.ok(geneCounts, MediaType.APPLICATION_JSON_TYPE).build();
    }
}
