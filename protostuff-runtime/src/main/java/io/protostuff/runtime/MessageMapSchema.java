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
import java.util.Map;

import io.protostuff.Input;
import io.protostuff.Message;
import io.protostuff.Output;
import io.protostuff.Schema;

/**
 * A schema for a {@link Map} with {@link Message} or pojo keys. The key and value can be null
 * (depending on the particular map impl).
 *
 * @author David Yu
 */
public final class MessageMapSchema<K, V> extends MapSchema<K, V> {

    /**
     * The schema of the message key.
     */
    public final Schema<K> kSchema;
    /**
     * The schema of the message value.
     */
    public final Schema<V> vSchema;

    public MessageMapSchema(Schema<K> kSchema, Schema<V> vSchema) {
        this.kSchema = kSchema;
        this.vSchema = vSchema;
    }

    @Override
    protected K readKeyFrom(Input input, MapWrapper<K, V> wrapper) throws IOException {
        return input.mergeObject(null, kSchema);
    }

    @Override
    protected void putValueFrom(Input input, MapWrapper<K, V> wrapper, K key)
            throws IOException {
        wrapper.put(key, input.mergeObject(null, vSchema));
    }

    @Override
    protected void writeKeyTo(Output output, int fieldNumber, K value, boolean repeated)
            throws IOException {
        output.writeObject(fieldNumber, value, kSchema, repeated);
    }

    @Override
    protected void writeValueTo(Output output, int fieldNumber, V value, boolean repeated)
            throws IOException {
        output.writeObject(fieldNumber, value, vSchema, repeated);
    }

}
