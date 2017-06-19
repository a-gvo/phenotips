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

import org.phenotips.data.DictionaryPatientData;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.PatientDataController;
import org.phenotips.data.PatientWritePolicy;
import org.phenotips.vocabulary.VocabularyManager;
import org.phenotips.vocabulary.VocabularyTerm;

import org.xwiki.component.annotation.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseStringProperty;
import com.xpn.xwiki.objects.ListProperty;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

/**
 * Handles the patient's global qualifiers, such as global age of onset.
 *
 * @version $Id$
 * @since 1.0M10
 */
@Component(roles = { PatientDataController.class })
@Named("global-qualifiers")
@Singleton
public class GlobalQualifiersController implements PatientDataController<List<VocabularyTerm>>
{
    private static final String DATA_NAME = "global-qualifiers";

    private static final String ID_NAME = "id";

    /** Logging helper object. */
    @Inject
    private Logger logger;

    @Inject
    private VocabularyManager vocabularyManager;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Override
    public PatientData<List<VocabularyTerm>> load(Patient patient)
    {
        try {
            XWikiDocument doc = patient.getXDocument();
            BaseObject data = doc.getXObject(Patient.CLASS_REFERENCE);
            if (data == null) {
                return null;
            }
            Map<String, List<VocabularyTerm>> result = new LinkedHashMap<>();
            for (String propertyName : getProperties()) {
                PropertyInterface propertyValue = data.get(propertyName);
                List<VocabularyTerm> holder = new LinkedList<>();
                if (propertyValue instanceof StringProperty) {
                    String propertyValueString = ((StringProperty) propertyValue).getValue();
                    addTerms(propertyValueString, holder);
                } else if (propertyValue instanceof ListProperty) {
                    for (String item : ((ListProperty) propertyValue).getList()) {
                        addTerms(item, holder);
                    }
                }
                result.put(propertyName, holder);
            }
            return new DictionaryPatientData<>(DATA_NAME, result);
        } catch (Exception e) {
            this.logger.error(ERROR_MESSAGE_LOAD_FAILED, e.getMessage());
        }
        return null;
    }

    @Override
    public void save(Patient patient)
    {
        PatientData<List<VocabularyTerm>> data = patient.getData(this.getName());
        XWikiContext context = this.xcontextProvider.get();

        BaseObject dataHolder = patient.getXDocument().getXObject(Patient.CLASS_REFERENCE, true, context);
        if (data == null || dataHolder == null) {
            return;
        }
        BaseClass xclass = dataHolder.getXClass(context);
        for (String propertyName : getProperties()) {
            List<VocabularyTerm> terms = data.get(propertyName);
            if (terms == null) {
                continue;
            }
            PropertyClass xpropertyClass = (PropertyClass) xclass.get(propertyName);
            if (xpropertyClass != null) {
                PropertyInterface xproperty = xpropertyClass.newProperty();
                if (xproperty instanceof BaseStringProperty) {
                    // there should be only one term present; just taking the head of the list
                    dataHolder.set(propertyName, terms.isEmpty() ? null : termsToXWikiFormat(terms).get(0), context);
                } else if (xproperty instanceof ListProperty) {
                    dataHolder.set(propertyName, termsToXWikiFormat(terms), context);
                }
            }
        }
    }

    @Override
    public void save(@Nonnull final Patient patient, @Nonnull final PatientWritePolicy policy)
    {
        final BaseObject xobject = patient.getXDocument().getXObject(Patient.CLASS_REFERENCE);
        if (xobject == null) {
            throw new IllegalArgumentException(ERROR_MESSAGE_NO_PATIENT_CLASS);
        }

        final XWikiContext context = this.xcontextProvider.get();
        final PatientData<List<VocabularyTerm>> data = patient.getData(getName());
        final BaseClass xclass = xobject.getXClass(context);
        if (data == null) {
            if (Objects.equals(PatientWritePolicy.REPLACE, policy)) {
                getProperties().forEach(p -> xobject.set(p, null, context));
            }
        } else {
            getProperties().stream()
                .map(p -> new MutableTriple<>(p, null, data.get(p)))
                .filter(t -> Objects.nonNull(t.getRight()))
                .peek(t -> t.setMiddle(xclass.get(t.getLeft())))
                .filter(t -> Objects.nonNull(t.getMiddle()))
                .peek(t -> t.setMiddle(((PropertyClass) t.getMiddle()).newProperty()))
                .forEach(t -> setControllerProperty(t.getLeft(), (PropertyInterface) t.getMiddle(), t.getRight(), xobject));
        }
    }

    private void setControllerProperty(
        @Nonnull final String propertyName,
        @Nonnull final PropertyInterface xproperty,
        @Nonnull final List<VocabularyTerm> terms,
        @Nonnull BaseObject xobject)
    {
        // FIXME: ON MERGE???
        final XWikiContext context = this.xcontextProvider.get();
        if (xproperty instanceof BaseStringProperty) {
            xobject.set(propertyName, terms.isEmpty() ? null : termsToXWikiFormat(terms).get(0), context);
        } else if (xproperty instanceof ListProperty) {
            xobject.set(propertyName, termsToXWikiFormat(terms), context);
        }
    }

    private List<String> termsToXWikiFormat(List<VocabularyTerm> terms)
    {
        List<String> ids = new LinkedList<>();
        for (VocabularyTerm term : terms) {
            ids.add(term.getId());
        }
        return ids;
    }

    @Override
    public void writeJSON(Patient patient, JSONObject json)
    {
        writeJSON(patient, json, null);
    }

    @Override
    public void writeJSON(Patient patient, JSONObject json, Collection<String> selectedFieldNames)
    {
        PatientData<List<VocabularyTerm>> data = patient.getData(DATA_NAME);
        if (data == null) {
            return;
        }
        Iterator<Entry<String, List<VocabularyTerm>>> iterator = data.dictionaryIterator();
        while (iterator.hasNext()) {
            Entry<String, List<VocabularyTerm>> datum = iterator.next();
            if (selectedFieldNames == null || selectedFieldNames.contains(datum.getKey())) {
                List<VocabularyTerm> terms = datum.getValue();
                if (terms == null || terms.isEmpty()) {
                    if (selectedFieldNames != null && selectedFieldNames.contains(datum.getKey())) {
                        json.put(datum.getKey(), new JSONArray());
                    }
                    continue;
                }
                JSONArray elements = new JSONArray();
                for (VocabularyTerm term : terms) {
                    JSONObject element = new JSONObject();
                    element.put(ID_NAME, term.getId());
                    element.put("label", term.getName());
                    elements.put(element);
                }
                json.put(datum.getKey(), elements);
            }
        }
    }

    @Override
    public PatientData<List<VocabularyTerm>> readJSON(JSONObject json)
    {
        try {
            Map<String, List<VocabularyTerm>> result = new HashMap<>();
            for (String property : this.getProperties()) {
                JSONArray elements = json.optJSONArray(property);
                if (elements != null) {
                    List<VocabularyTerm> propertyTerms = new LinkedList<>();
                    Iterator<Object> elementsIterator = elements.iterator();
                    while (elementsIterator.hasNext()) {
                        JSONObject element = (JSONObject) elementsIterator.next();
                        String termId = element.optString(ID_NAME);
                        if (termId != null) {
                            VocabularyTerm term = this.vocabularyManager.resolveTerm(termId);
                            propertyTerms.add(term);
                        }
                    }
                    result.put(property, propertyTerms);
                } else {
                    result.put(property, null);
                }
            }
            return new DictionaryPatientData<>(DATA_NAME, result);
        } catch (Exception ex) {
            // must be in a wrong format
        }
        return null;
    }

    @Override
    public String getName()
    {
        return DATA_NAME;
    }

    protected List<String> getProperties()
    {
        return Arrays.asList("global_age_of_onset", "global_mode_of_inheritance");
    }

    private void addTerms(String item, List<VocabularyTerm> holder)
    {
        if (StringUtils.isNotBlank(item)) {
            VocabularyTerm term = this.vocabularyManager.resolveTerm(item);
            if (term != null) {
                holder.add(term);
            }
        }
    }
}
