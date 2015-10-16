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

import java.io.IOException;
import java.util.Collection;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Tag;
import io.protostuff.WireFormat.FieldType;
import io.protostuff.runtime.CollectionSchema.MessageFactory;

/**
 * A runtime field for a {@link Collection}. Null values are not written.
 *
 * @author David Yu
 */
abstract class RuntimeCollectionField<T, V> extends Field<T> {

    /**
     * Since we cannot inherit multiple classes, we create this Collection schema simply to delegate
     * to the wrapping class' abstract methods.
     */
    protected final CollectionSchema<V> schema;

    public RuntimeCollectionField(FieldType type, int number, String name,
                                  Tag tag, MessageFactory messageFactory) {
        super(type, number, name, false, tag);
        schema = new CollectionSchema<V>(messageFactory) {
            @Override
            protected void addValueFrom(Input input, Collection<V> collection)
                    throws IOException {
                RuntimeCollectionField.this.addValueFrom(input, collection);
            }

            @Override
            protected void writeValueTo(Output output, int fieldNumber,
                                        V value, boolean repeated) throws IOException {
                RuntimeCollectionField.this.writeValueTo(output, fieldNumber,
                        value, repeated);
            }

        };
    }

    protected abstract void addValueFrom(Input input, Collection<V> collection)
            throws IOException;

    protected abstract void writeValueTo(Output output, int fieldNumber,
                                         V value, boolean repeated) throws IOException;

}
