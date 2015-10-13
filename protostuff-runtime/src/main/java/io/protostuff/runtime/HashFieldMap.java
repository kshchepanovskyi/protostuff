/**
 * Copyright (C) 2007-2015 Protostuff http://www.protostuff.io/
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.protostuff.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Field mapping implemented on top of hash for field lookup by number.
 *
 * This is the less efficient than {@code ArrayFieldMap} for almost all cases.
 * But in case when field numbers are sparse and especially when max field
 * number is big - this mapping should be used.
 *
 * @see io.protostuff.runtime.ArrayFieldMap
 *
 * @author Kostiantyn Shchepanovskyi
 */
final class HashFieldMap<T> implements FieldMap<T> {
    private static final FieldComparator FIELD_COMPARATOR = new FieldComparator();
    private final List<Field<T>> fields;
    private final Map<Integer, Field<T>> fieldsByNumber;
    private final Map<String, Field<T>> fieldsByName;

    public HashFieldMap(Collection<Field<T>> fields) {
        fieldsByName = new HashMap<>();
        fieldsByNumber = new HashMap<>();
        for (Field<T> f : fields) {
            if (fieldsByName.containsKey(f.name)) {
                Field<T> prev = fieldsByName.get(f.name);
                throw new IllegalStateException(prev + " and " + f + " cannot have the same name.");
            }
            if (fieldsByNumber.containsKey(f.number)) {
                Field<T> prev = fieldsByNumber.get(f.number);
                throw new IllegalStateException(prev + " and " + f + " cannot have the same number.");
            }
            this.fieldsByNumber.put(f.number, f);
            this.fieldsByName.put(f.name, f);
        }

        List<Field<T>> fieldList = new ArrayList<>(fields.size());
        fieldList.addAll(fields);
        Collections.sort(fieldList, FIELD_COMPARATOR);
        this.fields = Collections.unmodifiableList(fieldList);
    }

    @Override
    public Field<T> getFieldByNumber(int n) {
        return fieldsByNumber.get(n);
    }

    @Override
    public Field<T> getFieldByName(String fieldName) {
        return fieldsByName.get(fieldName);
    }

    @Override
    public int getFieldCount() {
        return fields.size();
    }

    @Override
    public List<Field<T>> getFields() {
        return fields;
    }

    private static class FieldComparator implements Comparator<Field<?>> {
        @Override
        public int compare(Field<?> o1, Field<?> o2) {
            return Integer.compare(o1.number, o2.number);
        }
    }
}
