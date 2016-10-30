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
package org.phenotips.vocabulary.internal.solr;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import org.phenotips.obo2solr.TermData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Class for parsing the hpo source, while gathering annotations from HPO-gene mapping from the Human Phenotype
 * Ontology.
 *
 * @version $Id $
 * @since 1.3M4
 */
public class HpoAnnotationSourceParser
{
    private static final String PHENOTYPE_TO_GENES_ANNOTATIONS_URL =
            "http://compbio.charite.de/jenkins/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/" +
                    "ALL_SOURCES_ALL_FREQUENCIES_phenotype_to_genes.txt";

    private static final String ENCODING = "UTF-8";

    private static final String GENES = "associated_genes";

    private final Map<String, TermData> data;

    private final Logger logger = LoggerFactory.getLogger(HpoAnnotationSourceParser.class);

    /**
     * Constructor that parses phenotype to genes annotations and adds them to the default HPO vocabulary data.
     * @param hpoData the HPO vocabulary data
     */
    public HpoAnnotationSourceParser(final Map<String, TermData> hpoData)
    {
        this.data = hpoData;
        // Load gene data for the HPO, only if we have HPO vocabulary data available.
        if (null != hpoData && !hpoData.isEmpty()) {
            loadGenes();
        }
    }

    /**
     * Loads phenotype-gene information and creates HPO-gene links.
     */
    private void loadGenes()
    {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new URL(PHENOTYPE_TO_GENES_ANNOTATIONS_URL).openConnection().getInputStream(), ENCODING))) {
            String prevTermName = "";
            TermData termData = null;
            for (CSVRecord row : CSVFormat.TDF.withHeader().parse(in)) {
                String termName = row.get(0);
                if (!prevTermName.equals(termName)) {
                    prevTermName = termName;
                    termData = this.data.get(termName);
                }
                linkGeneToPhenotype(termData, row.get(3));
            }
        } catch (final IOException e) {
            this.logger.error("Failed to load HPO-Gene links: {}", e.getMessage(), e);
        }
    }

    /**
     * Links a gene to its corresponding HPO term.
     * @param termData HPO term data
     * @param geneName the name of the gene to be added to the HPO term data set
     */
    private void linkGeneToPhenotype(final TermData termData, final String geneName)
    {
        if (null != termData && !geneName.isEmpty()) {
            if (!termData.containsKey(GENES)) {
                termData.put(GENES, new HashSet<String>());
            }
            final Collection<String> genes = termData.get(GENES);
            genes.add(geneName);
        }
    }

    /**
     * Gets the vocabulary data, following the addition of all necessary annotations.
     * @return the HPO vocabulary data
     */
    public Map<String, TermData> getData()
    {
        return this.data;
    }
}
