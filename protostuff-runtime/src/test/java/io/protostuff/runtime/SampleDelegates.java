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
import io.protostuff.ProtostuffException;
import io.protostuff.WireFormat.FieldType;

/**
 * Sample delegates for testing.
 *
 * @author David Yu
 */
public final class SampleDelegates {

    public static final Delegate<Singleton> SINGLETON_DELEGATE = new Delegate<Singleton>() {
        @Override
        public Class<?> typeClass() {
            return Singleton.class;
        }

        @Override
        public FieldType getFieldType() {
            return FieldType.UINT32;
        }

        @Override
        public void writeTo(Output output, int number, Singleton value,
                            boolean repeated) throws IOException {
            output.writeUInt32(number, 0, repeated);
        }

        @Override
        public Singleton readFrom(Input input) throws IOException {
            if (0 != input.readUInt32())
                throw new ProtostuffException("Corrupt input.");

            return Singleton.INSTANCE;
        }

        @Override
        public void transfer(Pipe pipe, Input input, Output output, int number,
                             boolean repeated) throws IOException {
            output.writeUInt32(number, input.readUInt32(), repeated);
        }
    };

    private SampleDelegates() {
    }

    public static final class ShortArrayDelegate implements Delegate<short[]> {
        int reads, writes, transfers;

        @Override
        public Class<?> typeClass() {
            return short[].class;
        }

        @Override
        public FieldType getFieldType() {
            return FieldType.BYTES;
        }

        @Override
        public void writeTo(Output output, int number, short[] value,
                            boolean repeated) throws IOException {
            writes++;

            byte[] buffer = new byte[value.length * 2];
            for (int i = 0, offset = 0; i < value.length; i++) {
                short s = value[i];
                buffer[offset++] = (byte) ((s >>> 8) & 0xFF);
                buffer[offset++] = (byte) ((s >>> 0) & 0xFF);
            }

            output.writeByteArray(number, buffer, repeated);
        }

        @Override
        public short[] readFrom(Input input) throws IOException {
            reads++;

            byte[] buffer = input.readByteArray();
            short[] s = new short[buffer.length / 2];
            for (int i = 0, offset = 0; i < s.length; i++) {
                s[i] = (short) ((buffer[offset++] & 0xFF) << 8 | (buffer[offset++] & 0xFF));
            }

            return s;
        }

        @Override
        public void transfer(Pipe pipe, Input input, Output output, int number,
                             boolean repeated) throws IOException {
            transfers++;

            input.transferByteRangeTo(output, false, number, repeated);
        }
    }

    public static final class Singleton {
        public static final Singleton INSTANCE = new Singleton();

        private Singleton() {
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj && obj == INSTANCE;
        }

        public int hashCode() {
            return System.identityHashCode(this);
        }
    }
}
