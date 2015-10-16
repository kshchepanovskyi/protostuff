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
package io.protostuff;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import io.protostuff.runtime.AbstractRuntimeCollectionSchemaTest;
import io.protostuff.runtime.CollectionSchema;

/**
 * Test runtime collection fields with {@link CollectionSchema} via smile ser/deser.
 *
 * @author David Yu
 */
public class SmileRuntimeCollectionSchemaTest extends AbstractRuntimeCollectionSchemaTest {

    @Override
    protected <T> void mergeFrom(byte[] data, int offset, int length, T message,
                                 Schema<T> schema) throws IOException {
        SmileIOUtil.mergeFrom(data, offset, length, message, schema, false);
    }

    @Override
    protected <T> void mergeFrom(InputStream in, T message, Schema<T> schema)
            throws IOException {
        SmileIOUtil.mergeFrom(in, message, schema, false);
    }

    @Override
    protected <T> byte[] toByteArray(T message, Schema<T> schema) {
        return SmileIOUtil.toByteArray(message, schema, false);
    }

    @Override
    protected <T> void writeTo(OutputStream out, T message, Schema<T> schema) throws IOException {
        SmileIOUtil.writeTo(out, message, schema, false);
    }

}
