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
package org.phenotips.data.internal.controller;

import org.phenotips.Constants;
import org.phenotips.data.Cancer;
import org.phenotips.data.CancerQualifiers;
import org.phenotips.data.IndexedPatientData;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.PatientDataController;
import org.phenotips.data.PatientWritePolicy;
import org.phenotips.data.VocabularyProperty;
import org.phenotips.data.internal.PhenoTipsCancer;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Handles the patients cancers.
 *
 * @version $Id$
 * @since 1.4
 */
@Component(roles = { PatientDataController.class })
@Named("cancers")
@Singleton
public class CancersController extends AbstractComplexController<Cancer>
{
    /** The XClass used for storing cancer data. */
    static final EntityReference CANCER_CLASS_REFERENCE = new EntityReference("CancerClass",
        EntityType.DOCUMENT, Constants.CODE_SPACE_REFERENCE);

    /** The XClass used for storing cancer qualifier data. */
    static final EntityReference CANCER_QUALIFIER_CLASS_REFERENCE = new EntityReference("CancerQualifierClass",
        EntityType.DOCUMENT, Constants.CODE_SPACE_REFERENCE);

    private static final String CANCERS_FIELD_NAME = "cancers";

    private static final String CONTROLLER_NAME = CANCERS_FIELD_NAME;

    private static final String JSON_KEY_PRIMARY = "primary";

    @Inject
    private Logger logger;

    /** Provides access to the current execution context. */
    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Override
    protected List<String> getBooleanFields()
    {
        return Collections.singletonList(JSON_KEY_PRIMARY);
    }

    @Override
    protected List<String> getCodeFields()
    {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getProperties()
    {
        return Collections.emptyList();
    }

    @Override
    protected String getJsonPropertyName()
    {
        return CONTROLLER_NAME;
    }

    @Override
    public String getName()
    {
        return CONTROLLER_NAME;
    }

    @Override
    public PatientData<Cancer> load(@Nonnull final Patient patient)
    {
        try{
            final XWikiDocument doc = patient.getXDocument();
            final List<BaseObject> cancerXWikiObjects = doc.getXObjects(CANCER_CLASS_REFERENCE);
            if (CollectionUtils.isEmpty(cancerXWikiObjects)) {
                return null;
            }
            final List<Cancer> cancers = cancerXWikiObjects.stream()
                .filter(cancerObj -> cancerObj != null && !cancerObj.getFieldList().isEmpty())
                .map(PhenoTipsCancer::new)
                .collect(Collectors.toList());
            return !cancers.isEmpty() ? new IndexedPatientData<>(getName(), cancers) : null;
        } catch (final Exception e) {
            this.logger.error(ERROR_MESSAGE_LOAD_FAILED, e.getMessage());
        }
        return null;
    }

    @Override
    public void writeJSON(
        @Nonnull final Patient patient,
        @Nonnull final JSONObject json,
        @Nullable final Collection<String> selectedFieldNames)
    {
        if (selectedFieldNames == null || selectedFieldNames.contains(CANCERS_FIELD_NAME)) {
            final JSONArray cancersJson = new JSONArray();
            final PatientData<Cancer> data = patient.getData(getName());
            if (data != null && data.size() != 0 && data.isIndexed()) {
                data.forEach(cancer -> addCancerJson(cancer, cancersJson));
            }
            json.put(getJsonPropertyName(), cancersJson);
        }
    }

    /**
     * Adds the {@link JSONObject} generated from {@code cancer} to the {@code cancersJson}.
     *
     * @param cancer the {@link Cancer} object containing cancer data
     * @param cancersJson the {@link JSONArray} containing all cancer data for a {@link Patient patient}
     */
    private void addCancerJson(@Nonnull final Cancer cancer, @Nonnull final JSONArray cancersJson)
    {
        if (StringUtils.isNotBlank(cancer.getId())) {
            cancersJson.put(cancer.toJSON());
        }
    }

    @Override
    public PatientData<Cancer> readJSON(@Nullable final JSONObject json)
    {
        if (json == null || !json.has(getJsonPropertyName())) {
            return null;
        }
        try {
            final JSONArray cancersJson = json.getJSONArray(getJsonPropertyName());
            final List<Cancer> cancers = IntStream.of(0, cancersJson.length())
                .mapToObj(cancersJson::optJSONObject)
                .filter(Objects::nonNull)
                .map(PhenoTipsCancer::new)
                .collect(Collectors.toList());
            return new IndexedPatientData<>(getName(), cancers);
        } catch (final Exception e) {
            this.logger.error("Could not load cancers from JSON: [{}]", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void save(@Nonnull final Patient patient)
    {
        save(patient, PatientWritePolicy.UPDATE);
    }

    @Override
    public void save(@Nonnull final Patient patient, @Nonnull final PatientWritePolicy policy)
    {
        try {
            final XWikiDocument docX = patient.getXDocument();
            final PatientData<Cancer> cancers = patient.getData(getName());
            if (cancers == null) {
                if (PatientWritePolicy.REPLACE.equals(policy)) {
                    docX.removeXObjects(CANCER_CLASS_REFERENCE);
                }
            } else {
                if (!cancers.isIndexed()) {
                    this.logger.error(ERROR_MESSAGE_DATA_IN_MEMORY_IN_WRONG_FORMAT);
                    return;
                }
                saveCancers(docX, patient, cancers, policy);
            }
        } catch (final Exception ex) {
            this.logger.error("Failed to save cancers data: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Saves {@code cancers} data for {@code patient} according to the provided {@code policy}.
     *
     * @param docX the {@link XWikiDocument} for patient
     * @param patient the {@link Patient} object of interest
     * @param cancers the newly added cancer data
     * @param policy the policy according to which data should be saved
     */
    private void saveCancers(
        @Nonnull final XWikiDocument docX,
        @Nonnull final Patient patient,
        @Nonnull final PatientData<Cancer> cancers,
        @Nonnull final PatientWritePolicy policy)
    {
        if (PatientWritePolicy.MERGE.equals(policy)) {
            final Map<String, Cancer> mergedCancers = getMergedCancers(cancers, load(patient));
            docX.removeXObjects(CANCER_CLASS_REFERENCE);
            mergedCancers.forEach((id, cancer) -> saveCancer(docX, cancer));
        } else {
            docX.removeXObjects(CANCER_CLASS_REFERENCE);
            cancers.forEach(cancer -> saveCancer(docX, cancer));
        }
    }

    private void saveCancer(@Nonnull final XWikiDocument docX, @Nonnull final Cancer cancer)
    {
        final XWikiContext xcontext = this.xcontextProvider.get();
        if (cancer.isAffected()) {
            try {
                final BaseObject xWikiObject = docX.newXObject(CANCER_CLASS_REFERENCE, xcontext);
                setProperties(xWikiObject, cancer.getProperties(), cancer::getPropertyValue, xcontext);
                // Save the qualifiers for the cancer.
                cancer.getQualifiers().forEach(qualifier -> saveQualifier(docX, qualifier, xcontext));
            } catch (final XWikiException ex) {
                this.logger.error("Failed to save data for cancer [{}]: [{}]", cancer.getId(), ex.getMessage());
            }
        }
    }

    /**
     * Saves the {@code qualifier} data to the {@code docX document}.
     *
     * @param docX the {@link XWikiDocument for patient}
     * @param qualifier the {@link CancerQualifiers} object
     * @param xcontext the {@link XWikiContext}
     */
    private void saveQualifier(
        @Nonnull final XWikiDocument docX,
        @Nonnull final CancerQualifiers qualifier,
        @Nonnull final XWikiContext xcontext)
    {
        try {
            final BaseObject xWikiObject = docX.newXObject(CANCER_QUALIFIER_CLASS_REFERENCE, xcontext);
            setProperties(xWikiObject, qualifier.getProperties(), qualifier::getPropertyValue, xcontext);
        } catch (final XWikiException ex) {
            this.logger.error("Failed to save qualifier for cancer [{}]: [{}]", qualifier.getId(), ex.getMessage());
        }
    }

    /**
     * Sets {@code properties} for the {@code xWikiObject}.
     *
     * @param xWikiObject the {@link BaseObject} for which to set the {@code properties}
     * @param properties a collection of properties to set
     * @param getValueFx a {@link Function} for retrieving values for each of the {@code properties}
     * @param xcontext the {@link XWikiContext}
     */
    private void setProperties(
        @Nonnull final BaseObject xWikiObject,
        @Nonnull final Collection<String> properties,
        @Nonnull final Function<String, Object> getValueFx,
        @Nonnull final XWikiContext xcontext)
    {
        properties.forEach(property -> setProperty(xWikiObject, property, getValueFx.apply(property), xcontext));
    }

    /**
     * Sets the {@code property} and {@code value} for the provided {@code xWikiObject}.
     *
     * @param xWikiObject the {@link BaseObject} for which the {@code property} and {@code value} will be set
     * @param property the property as string
     * @param value the value for the {@code property}
     * @param xcontext the {@link XWikiContext}
     */
    private void setProperty(
        @Nonnull final BaseObject xWikiObject,
        @Nonnull final String property,
        @Nullable final Object value,
        @Nonnull final XWikiContext xcontext)
    {
        if (value != null) {
            xWikiObject.set(property, value, xcontext);
        }
    }

    /**
     * Builds a map of merged cancers.
     *
     * @param cancers new {@link Cancer} data
     * @param storedCancers {@link Cancer} data already stored in {@link Patient}
     * @return a map of cancer ID to {@link Cancer}
     */
    private Map<String, Cancer> getMergedCancers(
        @Nullable final PatientData<Cancer> cancers,
        @Nullable final PatientData<Cancer> storedCancers)
    {
        return Stream.of(storedCancers, cancers)
            .filter(Objects::nonNull)
            .flatMap(s -> StreamSupport.stream(s.spliterator(), false))
            .collect(
                Collectors.toMap(VocabularyProperty::getId, Function.identity(), this::mergeCancers, LinkedHashMap::new)
            );
    }

    /**
     * Merges data for two cancers: {@code oldCancer} and {@code newCancer}.
     *
     * @param oldCancer the cancer data already stored
     * @param newCancer the new cancer data being added
     * @return a {@link Cancer} object with merged data
     */
    private Cancer mergeCancers(@Nonnull final Cancer oldCancer, @Nonnull final Cancer newCancer)
    {
        // There is no way to uniquely identify qualifiers. So when merging, keep all.
        final List<CancerQualifiers> mergedQualifiers = Stream.of(oldCancer.getQualifiers(), newCancer.getQualifiers())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        newCancer.setQualifiers(mergedQualifiers);
        return newCancer;
    }
}
