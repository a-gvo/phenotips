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
package org.phenotips.panels.internal;

import org.phenotips.panels.GenePanelsDataCreator;
import org.phenotips.vocabulary.Vocabulary;
import org.phenotips.vocabulary.VocabularyTerm;

import org.xwiki.component.annotation.Component;
import org.xwiki.stability.Unstable;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

/**
 * An implementation of {@link GenePanelsDataCreator}.
 *
 * @since 1.3M4
 * @version $Id$
 */
@Component
@Unstable("New API introduced in 1.3")
public class DefaultGenePanelsDataCreator implements GenePanelsDataCreator
{
    /** The hpo vocabulary. */
    @Inject
    @Named("hpo")
    private Vocabulary hpo;

    /** A list of hpo terms. */
    private final List<String> termIds;

    /** The property of interest for the provided hpo terms. */
    private final static String GENES = "associated_genes";

    /** Object mapper for JSON creation */
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Simple constructor, passing in the required list of hpo terms.
     *
     * @param termIds a list of HPO terms, for example {@code [HP:0011451]}
     */
    public DefaultGenePanelsDataCreator(final List<String> termIds)
    {
        Validate.noNullElements(termIds);
        this.termIds = termIds;
    }

    @Override
    public String createGeneCountsJson() throws JsonProcessingException
    {
        final ImmutableMap.Builder<String, List<String>> geneLinksBuilder = new ImmutableMap.Builder<>();
        final ImmutableMultiset.Builder<String> geneCountsBuilder = new ImmutableMultiset.Builder<>();

        // Populate the maps with data.
        buildCountsData(geneLinksBuilder, geneCountsBuilder);
        // Create the DTO object.
        final HpoToGenesLinksDto hpoToGenesLinksDto = new HpoToGenesLinksDto(geneLinksBuilder.build(),
            geneCountsBuilder.build());
        // Create a JSON string of the data.
        return OBJECT_MAPPER.writeValueAsString(hpoToGenesLinksDto);
    }

    /**
     * Builds the data for the provided list of phenotypes using the HPO {@link Vocabulary}.
     *
     * @param geneLinksBuilder the builder for the gene links data
     * @param geneCountsBuilder the builder for the gene counts data
     */
    private void buildCountsData(@Nonnull final ImmutableMap.Builder<String, List<String>> geneLinksBuilder,
        @Nonnull final ImmutableMultiset.Builder<String> geneCountsBuilder)
    {
        for (String id : this.termIds) {
            List<String> storedGenes = getGeneData(id);
            if (!CollectionUtils.isEmpty(storedGenes)) {
                final ImmutableList.Builder<String> genesListBuilder = new ImmutableList.Builder<>();
                genesListBuilder.addAll(storedGenes);
                geneLinksBuilder.put(id, genesListBuilder.build());
                geneCountsBuilder.addAll(storedGenes);
            }
        }
    }

    /**
     * Gets the gene data for an HPO term.
     *
     * @param id the HPO term id, for example {@code HP:0011451}
     * @return genes associated with the provided HPO term, null otherwise
     */
    private List<String> getGeneData(@Nonnull final String id)
    {
        // Try to find the term in the vocabulary.
        final VocabularyTerm hpoTerm = hpo.getTerm(id);
        if (hpoTerm == null) {
            return null;
        }
        // Try to get associated genes. An HPO term may not have any.
        @SuppressWarnings("unchecked")
        final List<String> storedGenes = (List<String>) hpoTerm.get(GENES);
        if (CollectionUtils.isEmpty(storedGenes)) {
            return null;
        }
        return storedGenes;
    }

    /**
     * A DTO containing associated genes data for a selected subset of HPO terms.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HpoToGenesLinksDto
    {
        /** A map of phenotypes to their associated genes */
        private final Map<String, List<String>> links;

        /** A multiset of genes for counting purposes */
        private final Multiset<String> counts;

        private static final String LINKS = "gene_links";

        private static final String COUNTS = "gene_counts";

        /**
         * Default constructor for the DTO object.
         *
         * @param links a {@link Map} containing a list of genes associated with each provided phenotype
         * @param counts a {@link Multiset} counting the number of times each gene occurs among the provided phenotypes
         */
        @JsonCreator
        private HpoToGenesLinksDto(@JsonProperty(LINKS) final Map<String, List<String>> links,
            @JsonProperty(COUNTS) final Multiset<String> counts)
        {
            this.links = links;
            this.counts = counts;
        }

        /**
         * Gets the phenotypes to associated genes map.
         *
         * @return a {@link Map} of phenotypes to genes
         */
        @JsonProperty(LINKS)
        private Map<String, List<String>> getLinks()
        {
            return this.links;
        }

        /**
         * Gets the genes to number of occurrences among selected phenotypes map.
         *
         * @return a {@link Multiset} of genes to number of occurrences
         */
        @JsonProperty(COUNTS)
        private Multiset<String> getCounts()
        {
            return this.counts;
        }
    }
}
