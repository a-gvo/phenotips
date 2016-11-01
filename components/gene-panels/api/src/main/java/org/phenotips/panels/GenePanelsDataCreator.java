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
package org.phenotips.panels;

import org.xwiki.stability.Unstable;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Provides access to gene panels.
 *
 * @version $Id$
 * @since 1.3M4
 */
@Unstable("New API introduced in 1.3")
public interface GenePanelsDataCreator
{
    /**
     * Creates JSON representation of gene panels data.
     *
     * @return JSON representation fo gene panels data
     * @throws JsonProcessingException if JSON cannot be created
     */
    String createGeneCountsJson() throws JsonProcessingException;
}
