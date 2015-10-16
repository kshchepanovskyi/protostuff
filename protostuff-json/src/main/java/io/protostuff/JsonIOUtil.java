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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.UTF8JsonGenerator;
import com.fasterxml.jackson.core.json.UTF8StreamJsonParser;
import com.fasterxml.jackson.core.sym.BytesToNameCanonicalizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for the JSON serialization/deserialization of messages and objects tied to a schema.
 *
 * @author David Yu
 */
public final class JsonIOUtil {

    /**
     * The default json factory for creating json parsers and generators.
     */
    public static final Factory DEFAULT_JSON_FACTORY = new Factory();

    static {
        // disable auto-close to have same behavior as protostuff-core utility io methods
        DEFAULT_JSON_FACTORY.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        DEFAULT_JSON_FACTORY.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    }

    private JsonIOUtil() {
    }

    /**
     * Creates a {@link UTF8StreamJsonParser} from the inputstream with the supplied buf {@code
     * inBuffer} to use.
     */
    public static UTF8StreamJsonParser newJsonParser(InputStream in, byte[] buf,
                                                     int offset, int limit) throws IOException {
        return newJsonParser(in, buf, offset, limit, false,
                new IOContext(DEFAULT_JSON_FACTORY._getBufferRecycler(), in,
                        false));
    }

    /**
     * Creates a {@link UTF8StreamJsonParser} from the inputstream with the supplied buf {@code
     * inBuffer} to use.
     */
    static UTF8StreamJsonParser newJsonParser(InputStream in, byte[] buf,
                                              int offset, int limit, boolean bufferRecyclable, IOContext context)
            throws IOException {
        return new UTF8StreamJsonParser(context,
                DEFAULT_JSON_FACTORY.getParserFeatures(), in,
                DEFAULT_JSON_FACTORY.getCodec(),
                DEFAULT_JSON_FACTORY.getRootByteSymbols().makeChild(true, true),
                buf, offset, limit, bufferRecyclable);
    }

    /**
     * Creates a {@link UTF8JsonGenerator} for the outputstream with the supplied buf {@code
     * outBuffer} to use.
     */
    public static UTF8JsonGenerator newJsonGenerator(OutputStream out, byte[] buf) {
        return newJsonGenerator(out, buf, 0, false, new IOContext(
                DEFAULT_JSON_FACTORY._getBufferRecycler(), out, false));
    }

    /**
     * Creates a {@link UTF8JsonGenerator} for the outputstream with the supplied buf {@code
     * outBuffer} to use.
     */
    static UTF8JsonGenerator newJsonGenerator(OutputStream out, byte[] buf, int offset,
                                              boolean bufferRecyclable, IOContext context) {
        context.setEncoding(JsonEncoding.UTF8);

        return new UTF8JsonGenerator(context,
                DEFAULT_JSON_FACTORY.getGeneratorFeatures(),
                DEFAULT_JSON_FACTORY.getCodec(),
                out,
                buf,
                offset,
                bufferRecyclable);
    }

    /**
     * Merges the {@code message} with the byte array using the given {@code schema}.
     */
    public static <T> void mergeFrom(byte[] data, T message, Schema<T> schema,
                                     boolean numeric) throws IOException {
        mergeFrom(data, 0, data.length, message, schema, numeric);
    }

    /**
     * Merges the {@code message} with the byte array using the given {@code schema}.
     */
    public static <T> void mergeFrom(byte[] data, int offset, int length, T message,
                                     Schema<T> schema, boolean numeric) throws IOException {
        final IOContext context = new IOContext(DEFAULT_JSON_FACTORY._getBufferRecycler(),
                data, false);
        final JsonParser parser = newJsonParser(null, data, offset, offset + length, false,
                context);
        /*
         * final JsonParser parser = DEFAULT_JSON_FACTORY.createJsonParser(data, offset, length);
         */
        try {
            mergeFrom(parser, message, schema, numeric);
        } finally {
            parser.close();
        }
    }

    /**
     * Merges the {@code message} from the {@link InputStream} using the given {@code schema}.
     */
    public static <T> void mergeFrom(InputStream in, T message, Schema<T> schema,
                                     boolean numeric) throws IOException {
        final IOContext context = new IOContext(DEFAULT_JSON_FACTORY._getBufferRecycler(),
                in, false);
        final JsonParser parser = newJsonParser(in, context.allocReadIOBuffer(), 0, 0,
                true, context);
        // final JsonParser parser = DEFAULT_JSON_FACTORY.createJsonParser(in);
        try {
            mergeFrom(parser, message, schema, numeric);
        } finally {
            parser.close();
        }
    }

    /**
     * Merges the {@code message} from the {@link InputStream} using the given {@code schema}. <p>
     * The {@link LinkedBuffer}'s internal byte array will be used when reading the message.
     */
    public static <T> void mergeFrom(InputStream in, T message, Schema<T> schema,
                                     boolean numeric, LinkedBuffer buffer) throws IOException {
        final IOContext context = new IOContext(DEFAULT_JSON_FACTORY._getBufferRecycler(),
                in, false);
        final JsonParser parser = newJsonParser(in, buffer.buffer, 0, 0, false, context);
        try {
            mergeFrom(parser, message, schema, numeric);
        } finally {
            parser.close();
        }
    }

    /**
     * Merges the {@code message} from the {@link Reader} using the given {@code schema}.
     */
    public static <T> void mergeFrom(Reader reader, T message, Schema<T> schema,
                                     boolean numeric) throws IOException {
        final JsonParser parser = DEFAULT_JSON_FACTORY.createJsonParser(reader);
        try {
            mergeFrom(parser, message, schema, numeric);
        } finally {
            parser.close();
        }
    }

    /**
     * Merges the {@code message} from the JsonParser using the given {@code schema}.
     */
    public static <T> void mergeFrom(JsonParser parser, T message, Schema<T> schema,
                                     boolean numeric) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new JsonInputException("Expected token: { but was " +
                    parser.getCurrentToken() + " on message " +
                    schema.messageFullName());
        }

        schema.mergeFrom(new JsonInput(parser, numeric), message);

        if (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            throw new JsonInputException("Expected token: } but was " +
                    parser.getCurrentToken() + " on message " +
                    schema.messageFullName());
        }
    }

    /**
     * Serializes the {@code message} into a byte array using the given {@code schema}.
     */
    public static <T> byte[] toByteArray(T message, Schema<T> schema, boolean numeric) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writeTo(baos, message, schema, numeric);
        } catch (IOException e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException " +
                    "(should never happen).", e);
        }
        return baos.toByteArray();
    }

    /**
     * Serializes the {@code message} into a byte array using the given {@code schema}. <p> The
     * {@link LinkedBuffer}'s internal byte array will be used as the primary buffer when writing
     * the message.
     */
    public static <T> byte[] toByteArray(T message, Schema<T> schema, boolean numeric,
                                         LinkedBuffer buffer) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writeTo(baos, message, schema, numeric, buffer);
        } catch (IOException e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException " +
                    "(should never happen).", e);
        }
        return baos.toByteArray();
    }

    /**
     * Serializes the {@code message} into an {@link OutputStream} using the given {@code schema}.
     */
    public static <T> void writeTo(OutputStream out, T message, Schema<T> schema,
                                   boolean numeric) throws IOException {
        final IOContext context = new IOContext(DEFAULT_JSON_FACTORY._getBufferRecycler(),
                out, false);

        final JsonGenerator generator = newJsonGenerator(out,
                context.allocWriteEncodingBuffer(), 0, true, context);

        /*
         * final JsonGenerator generator = DEFAULT_JSON_FACTORY.createJsonGenerator(out, JsonEncoding.UTF8);
         */
        try {
            writeTo(generator, message, schema, numeric);
        } finally {
            generator.close();
        }
    }

    /**
     * Serializes the {@code message} into an {@link OutputStream} using the given {@code schema}.
     * <p> The {@link LinkedBuffer}'s internal byte array will be used as the primary buffer when
     * writing the message.
     */
    public static <T> void writeTo(OutputStream out, T message, Schema<T> schema,
                                   boolean numeric, LinkedBuffer buffer) throws IOException {
        final IOContext context = new IOContext(DEFAULT_JSON_FACTORY._getBufferRecycler(),
                out, false);

        final JsonGenerator generator = newJsonGenerator(out, buffer.buffer, 0, false,
                context);
        try {
            writeTo(generator, message, schema, numeric);
        } finally {
            generator.close();
        }
    }

    /**
     * Serializes the {@code message} into a {@link Writer} using the given {@code schema}.
     */
    public static <T> void writeTo(Writer writer, T message, Schema<T> schema,
                                   boolean numeric) throws IOException {
        final JsonGenerator generator = DEFAULT_JSON_FACTORY.createJsonGenerator(writer);
        try {
            writeTo(generator, message, schema, numeric);
        } finally {
            generator.close();
        }
    }

    /**
     * Serializes the {@code message} into a JsonGenerator using the given {@code schema}.
     */
    public static <T> void writeTo(JsonGenerator generator, T message, Schema<T> schema,
                                   boolean numeric) throws IOException {
        generator.writeStartObject();

        final JsonOutput output = new JsonOutput(generator, numeric, schema);
        schema.writeTo(output, message);
        if (output.isLastRepeated())
            generator.writeEndArray();

        generator.writeEndObject();
    }

    /**
     * Serializes the {@code messages} into the stream using the given schema.
     */
    public static <T> void writeListTo(OutputStream out, List<T> messages,
                                       Schema<T> schema, boolean numeric) throws IOException {
        final IOContext context = new IOContext(DEFAULT_JSON_FACTORY._getBufferRecycler(),
                out, false);

        final JsonGenerator generator = newJsonGenerator(out,
                context.allocWriteEncodingBuffer(), 0, true, context);
        /*
         * final JsonGenerator generator = DEFAULT_JSON_FACTORY.createJsonGenerator(out, JsonEncoding.UTF8);
         */
        try {
            writeListTo(generator, messages, schema, numeric);
        } finally {
            generator.close();
        }
    }

    /**
     * Serializes the {@code messages} into the stream using the given schema. <p> The {@link
     * LinkedBuffer}'s internal byte array will be used as the primary buffer when writing the
     * message.
     */
    public static <T> void writeListTo(OutputStream out, List<T> messages,
                                       Schema<T> schema, boolean numeric, LinkedBuffer buffer) throws IOException {
        final IOContext context = new IOContext(DEFAULT_JSON_FACTORY._getBufferRecycler(),
                out, false);

        final JsonGenerator generator = newJsonGenerator(out, buffer.buffer, 0, false,
                context);
        try {
            writeListTo(generator, messages, schema, numeric);
        } finally {
            generator.close();
        }
    }

    /**
     * Serializes the {@code messages} into the writer using the given schema.
     */
    public static <T> void writeListTo(Writer writer, List<T> messages, Schema<T> schema,
                                       boolean numeric) throws IOException {
        final JsonGenerator generator = DEFAULT_JSON_FACTORY.createJsonGenerator(writer);
        try {
            writeListTo(generator, messages, schema, numeric);
        } finally {
            generator.close();
        }
    }

    /**
     * Serializes the {@code messages} into the generator using the given schema.
     */
    public static <T> void writeListTo(JsonGenerator generator, List<T> messages,
                                       Schema<T> schema, boolean numeric) throws IOException {
        generator.writeStartArray();
        if (messages.isEmpty()) {
            generator.writeEndArray();
            return;
        }

        final JsonOutput output = new JsonOutput(generator, numeric, schema);

        for (T m : messages) {
            generator.writeStartObject();

            schema.writeTo(output, m);
            if (output.isLastRepeated())
                generator.writeEndArray();

            generator.writeEndObject();
            output.reset();
        }

        generator.writeEndArray();
    }

    /**
     * Parses the {@code messages} from the stream using the given {@code schema}.
     */
    public static <T> List<T> parseListFrom(InputStream in, Schema<T> schema,
                                            boolean numeric) throws IOException {
        final IOContext context = new IOContext(DEFAULT_JSON_FACTORY._getBufferRecycler(),
                in, false);
        final JsonParser parser = newJsonParser(in, context.allocReadIOBuffer(), 0, 0,
                true, context);
        // final JsonParser parser = DEFAULT_JSON_FACTORY.createJsonParser(in);
        try {
            return parseListFrom(parser, schema, numeric);
        } finally {
            parser.close();
        }
    }

    /**
     * Parses the {@code messages} from the stream using the given {@code schema}. <p> The {@link
     * LinkedBuffer}'s internal byte array will be used when reading the message.
     */
    public static <T> List<T> parseListFrom(InputStream in, Schema<T> schema,
                                            boolean numeric, LinkedBuffer buffer) throws IOException {
        final IOContext context = new IOContext(DEFAULT_JSON_FACTORY._getBufferRecycler(),
                in, false);
        final JsonParser parser = newJsonParser(in, buffer.buffer, 0, 0, false, context);
        try {
            return parseListFrom(parser, schema, numeric);
        } finally {
            parser.close();
        }
    }

    /**
     * Parses the {@code messages} from the reader using the given {@code schema}.
     */
    public static <T> List<T> parseListFrom(Reader reader, Schema<T> schema,
                                            boolean numeric) throws IOException {
        final JsonParser parser = DEFAULT_JSON_FACTORY.createJsonParser(reader);
        try {
            return parseListFrom(parser, schema, numeric);
        } finally {
            parser.close();
        }
    }

    /**
     * Parses the {@code messages} from the parser using the given {@code schema}.
     */
    public static <T> List<T> parseListFrom(JsonParser parser, Schema<T> schema,
                                            boolean numeric) throws IOException {
        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throw new JsonInputException("Expected token: [ but was " +
                    parser.getCurrentToken() + " on message: " +
                    schema.messageFullName());
        }

        final JsonInput input = new JsonInput(parser, numeric);
        final List<T> list = new ArrayList<>();
        for (JsonToken t = parser.nextToken(); t != JsonToken.END_ARRAY; t = parser.nextToken()) {
            if (t != JsonToken.START_OBJECT) {
                throw new JsonInputException("Expected token: { but was " +
                        parser.getCurrentToken() + " on message " +
                        schema.messageFullName());
            }

            final T message = schema.newMessage();
            schema.mergeFrom(input, message);

            if (parser.getCurrentToken() != JsonToken.END_OBJECT) {
                throw new JsonInputException("Expected token: } but was " +
                        parser.getCurrentToken() + " on message " +
                        schema.messageFullName());
            }

            list.add(message);
            input.reset();
        }
        return list;
    }

    /**
     * A custom factory simply to expose certain fields.
     */
    public static final class Factory extends JsonFactory {

        /**
         * Needed by jackson's internal utf8 strema parser.
         */
        public BytesToNameCanonicalizer getRootByteSymbols() {
            return _rootByteSymbols;
        }

        /**
         * Returns the parser feature flags.
         */
        public int getParserFeatures() {
            return _parserFeatures;
        }

        /**
         * Returns the generator feature flags.
         */
        public int getGeneratorFeatures() {
            return _generatorFeatures;
        }

    }
}
