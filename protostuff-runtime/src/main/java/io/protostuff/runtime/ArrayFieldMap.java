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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Field mapping implemented on top of java array for lookup by number.
 *
 * This is the most efficient implementation for almost all cases. But
 * it should not be used when field numbers are sparse and especially
 * when max field number is big - as this mapping internally uses array
 * of integers with size equal to max field number. In latter case
 * {@code HashFieldMap} should be used.
 *
 * @see io.protostuff.runtime.HashFieldMap
 *
 * @author Kostiantyn Shchepanovskyi
 */
final class ArrayFieldMap<T> implements FieldMap<T> {
    private final List<Field<T>> fields;
    private final Field<T>[] fieldsByNumber;
    private final Map<String, Field<T>> fieldsByName;

    @SuppressWarnings("unchecked")
    public ArrayFieldMap(Collection<Field<T>> fields, int lastFieldNumber) {
        fieldsByName = new HashMap<>();
        fieldsByNumber = (Field<T>[]) new Field<?>[lastFieldNumber + 1];
        for (Field<T> f : fields) {
            Field<T> last = this.fieldsByName.put(f.name, f);
            if (last != null) {
                throw new IllegalStateException(last + " and " + f
                        + " cannot have the same name.");
            }
            if (fieldsByNumber[f.number] != null) {
                throw new IllegalStateException(fieldsByNumber[f.number]
                        + " and " + f + " cannot have the same number.");
            }

            fieldsByNumber[f.number] = f;
        }

        List<Field<T>> fieldList = new ArrayList<>(fields.size());
        for (Field<T> field : fieldsByNumber) {
            if (field != null)
                fieldList.add(field);
        }
        this.fields = Collections.unmodifiableList(fieldList);
    }

    @Override
    public Field<T> getFieldByNumber(int n) {
        return n < fieldsByNumber.length ? fieldsByNumber[n] : null;
    }

    @Override
    public Field<T> getFieldByName(String fieldName) {
        return fieldsByName.get(fieldName);
    }

    /**
     * Returns the message's total number of fields.
     */
    @Override
    public int getFieldCount() {
        return fields.size();
    }

    @Override
    public List<Field<T>> getFields() {
        return fields;
    }
}
