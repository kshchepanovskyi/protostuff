/**
 * Copyright (C) 2007-2015 Protostuff
 * http://www.protostuff.io/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.protostuff.runtime;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Tag;

/**
 * @author Kostiantyn Shchepanovskyi
 */
@SuppressWarnings("unused")
public class RuntimeSchemaTagTest
{

    /**
     * Simple serialization/deserialization test
     *
     * @throws Exception
     */
    @Test
    public void testSerializeDeserialize() throws Exception
    {
        RuntimeSchema<A6> schema = RuntimeSchema.createFrom(A6.class);
        A6 source = new A6(42);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LinkedBuffer buffer = LinkedBuffer.allocate();
        ProtostuffIOUtil.writeTo(outputStream, source, schema, buffer);
        byte[] bytes = outputStream.toByteArray();
        A6 newMessage = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, newMessage, schema);
        Assert.assertEquals(source, newMessage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeTag() throws Exception
    {
        RuntimeSchema.createFrom(NegativeTag.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroTag() throws Exception
    {
        RuntimeSchema.createFrom(ZeroTag.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooBigTag() throws Exception
    {
        RuntimeSchema.createFrom(TooBigTag.class);
    }

    /**
     * {@link io.protostuff.runtime.RuntimeSchema#createFrom(Class)} should not throw
     * {@linkplain java.lang.OutOfMemoryError} when tag value is too big
     */
    @Test
    public void testMaxTag() throws Exception
    {
        RuntimeSchema<MaxTag> schema = RuntimeSchema.createFrom(MaxTag.class);
        Assert.assertNotNull(schema);
    }

    static class A1
    {
        @Tag(1)
        private int x;

        public A1()
        {
        }

        public A1(int x)
        {
            this.x = x;
        }

        public int getX()
        {
            return x;
        }

        public void setX(int x)
        {
            this.x = x;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (!(o instanceof A1))
                return false;

            A1 a1 = (A1) o;

            return x == a1.x;

        }

        @Override
        public int hashCode()
        {
            return x;
        }

        @Override
        public String toString()
        {
            return "A{" + "x=" + x + '}';
        }
    }

    static class A6
    {
        @Tag(1000_000)
        private int x;

        public A6()
        {
        }

        public A6(int x)
        {
            this.x = x;
        }

        public int getX()
        {
            return x;
        }

        public void setX(int x)
        {
            this.x = x;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (!(o instanceof A6))
                return false;

            A6 a6 = (A6) o;

            return x == a6.x;

        }

        @Override
        public int hashCode()
        {
            return x;
        }

        @Override
        public String toString()
        {
            return "A{" + "x=" + x + '}';
        }
    }

    static class NegativeTag
    {
        @Tag(-20)
        private int x;
    }

    static class ZeroTag
    {
        @Tag(0)
        private int x;
    }

    static class TooBigTag
    {
        @Tag(RuntimeSchema.MAX_TAG_VALUE + 1)
        private int x;
    }

    static class MaxTag
    {
        @Tag(RuntimeSchema.MAX_TAG_VALUE - 1)
        private int x;
    }

}