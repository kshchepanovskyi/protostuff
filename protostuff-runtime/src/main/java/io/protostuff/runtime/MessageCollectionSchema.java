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
import io.protostuff.Schema;

/**
 * A schema for a {@link Collection} with {@code Message} or pojo values. Does not allow null
 * values.
 *
 * @author David Yu
 */
public final class MessageCollectionSchema<V> extends CollectionSchema<V> {

    /**
     * The schema of the member (message).
     */
    public final Schema<V> schema;

    public MessageCollectionSchema(Schema<V> schema) {
        this.schema = schema;
    }

    @Override
    protected void addValueFrom(Input input, Collection<V> collection) throws IOException {
        collection.add(input.mergeObject(null, schema));
    }

    @Override
    protected void writeValueTo(Output output, int fieldNumber, V value, boolean repeated) throws IOException {
        output.writeObject(fieldNumber, value, schema, repeated);
    }

}
