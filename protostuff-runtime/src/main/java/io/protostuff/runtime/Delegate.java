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

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.WireFormat.FieldType;

/**
 * Controls how certain types are serialized and can even override the existing serializers because this has higher
 * priority when the fields are being built.
 *
 * @author David Yu
 */
public interface Delegate<V> {

    /**
     * The field type (for possible reflective operations in future releases).
     */
    FieldType getFieldType();

    /**
     * Reads the value from the input.
     */
    V readFrom(Input input) throws IOException;

    /**
     * Writes the {@code value} to the output.
     */
    void writeTo(Output output, int number, V value, boolean repeated)
            throws IOException;

    /**
     * Transfers the type from the input to the output.
     */
    void transfer(Pipe pipe, Input input, Output output, int number,
                  boolean repeated) throws IOException;

    /**
     * The class of the target value.
     */
    Class<?> typeClass();
}
