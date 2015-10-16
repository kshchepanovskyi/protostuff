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

import io.protostuff.GraphInput;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.ProtostuffException;
import io.protostuff.Schema;

import static io.protostuff.runtime.RuntimeFieldFactory.ID_ENUM;
import static io.protostuff.runtime.RuntimeFieldFactory.STR_ENUM;

/**
 * Used when a field is declared as {@code Enum<?>} (with or with-out generics).
 *
 * @author David Yu
 */
public abstract class PolymorphicEnumSchema extends PolymorphicSchema {

    static final int ID_ENUM_VALUE = 1;
    static final String STR_ENUM_VALUE = "a";

    public PolymorphicEnumSchema(IdStrategy strategy) {
        super(strategy);
    }

    static String name(int number) {
        switch (number) {
            case ID_ENUM_VALUE:
                return STR_ENUM_VALUE;
            case ID_ENUM:
                return STR_ENUM;
            default:
                return null;
        }
    }

    static int number(String name) {
        if (name.length() != 1)
            return 0;

        switch (name.charAt(0)) {
            case 'a':
                return ID_ENUM_VALUE;
            case 'x':
                return ID_ENUM;
            default:
                return 0;
        }
    }

    static void writeObjectTo(Output output, Object value,
                              Schema<?> currentSchema, IdStrategy strategy) throws IOException {
        final Class<?> clazz = value.getClass();
        if (clazz.getSuperclass() != null && clazz.getSuperclass().isEnum()) {
            EnumIO<?> eio = strategy.getEnumIO(clazz.getSuperclass());
            strategy.writeEnumIdTo(output, ID_ENUM, clazz.getSuperclass());
            eio.writeTo(output, ID_ENUM_VALUE, false, (Enum<?>) value);
        } else {
            EnumIO<?> eio = strategy.getEnumIO(clazz);
            strategy.writeEnumIdTo(output, ID_ENUM, clazz);
            eio.writeTo(output, ID_ENUM_VALUE, false, (Enum<?>) value);
        }
    }

    static Object readObjectFrom(Input input, Schema<?> schema, Object owner,
                                 IdStrategy strategy) throws IOException {
        if (ID_ENUM != input.readFieldNumber(schema))
            throw new ProtostuffException("Corrupt input.");

        final EnumIO<?> eio = strategy.resolveEnumFrom(input);

        if (ID_ENUM_VALUE != input.readFieldNumber(schema))
            throw new ProtostuffException("Corrupt input.");

        final Object value = eio.readFrom(input);

        if (input instanceof GraphInput) {
            // update the actual reference.
            ((GraphInput) input).updateLast(value, owner);
        }

        if (0 != input.readFieldNumber(schema))
            throw new ProtostuffException("Corrupt input.");

        return value;
    }

    @Override
    public String getFieldName(int number) {
        return name(number);
    }

    @Override
    public int getFieldNumber(String name) {
        return number(name);
    }

    @Override
    public String messageFullName() {
        return Enum.class.getName();
    }

    @Override
    public String messageName() {
        return Enum.class.getSimpleName();
    }

    @Override
    public void mergeFrom(Input input, Object owner) throws IOException {
        setValue(readObjectFrom(input, this, owner, strategy), owner);
    }

    @Override
    public void writeTo(Output output, Object value) throws IOException {
        writeObjectTo(output, value, this, strategy);
    }

}
