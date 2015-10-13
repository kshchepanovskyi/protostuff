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
import java.util.Map;

import io.protostuff.StringSerializer.STRING;
import io.protostuff.runtime.AbstractRuntimeMapTest;

/**
 * Test json ser/deser for runtime {@link Map} fields.
 *
 * @author David Yu
 */
public abstract class AbstractJsonRuntimeMapTest extends AbstractRuntimeMapTest {

    protected abstract boolean isNumeric();

    @Override
    protected <T> void mergeFrom(byte[] data, int offset, int length, T message,
                                 Schema<T> schema) throws IOException {
        JsonIOUtil.mergeFrom(data, offset, length, message, schema, isNumeric());
    }

    @Override
    protected <T> void mergeFrom(InputStream in, T message, Schema<T> schema)
            throws IOException {
        JsonIOUtil.mergeFrom(in, message, schema, isNumeric());
    }

    @Override
    protected <T> byte[] toByteArray(T message, Schema<T> schema) {
        return JsonIOUtil.toByteArray(message, schema, isNumeric());
    }

    @Override
    protected <T> void writeTo(OutputStream out, T message, Schema<T> schema) throws IOException {
        JsonIOUtil.writeTo(out, message, schema, isNumeric());
    }

    @Override
    protected <T> void roundTrip(T message, Schema<T> schema,
                                 Pipe.Schema<T> pipeSchema) throws Exception {
        byte[] json = JsonIOUtil.toByteArray(message, schema, isNumeric());

        ByteArrayInputStream jsonStream = new ByteArrayInputStream(json);

        byte[] protostuff = ProtostuffIOUtil.toByteArray(
                JsonIOUtil.newPipe(json, 0, json.length, isNumeric()), pipeSchema, buf());

        byte[] protostuffFromStream = ProtostuffIOUtil.toByteArray(
                JsonIOUtil.newPipe(jsonStream, isNumeric()), pipeSchema, buf());

        assertTrue(Arrays.equals(protostuff, protostuffFromStream));

        T parsedMessage = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(protostuff, parsedMessage, schema);
        SerializableObjects.assertEquals(message, parsedMessage);

        ByteArrayInputStream protostuffStream = new ByteArrayInputStream(protostuff);

        byte[] jsonRoundTrip = JsonIOUtil.toByteArray(
                ProtostuffIOUtil.newPipe(protostuff, 0, protostuff.length), pipeSchema, isNumeric());

        byte[] jsonRoundTripFromStream = JsonIOUtil.toByteArray(
                ProtostuffIOUtil.newPipe(protostuffStream), pipeSchema, isNumeric());

        assertTrue(jsonRoundTrip.length == jsonRoundTripFromStream.length);

        String strJsonRoundTrip = STRING.deser(jsonRoundTrip);

        assertEquals(strJsonRoundTrip, STRING.deser(jsonRoundTripFromStream));

        assertTrue(jsonRoundTrip.length == json.length);

        assertEquals(strJsonRoundTrip, STRING.deser(json));
    }

}
