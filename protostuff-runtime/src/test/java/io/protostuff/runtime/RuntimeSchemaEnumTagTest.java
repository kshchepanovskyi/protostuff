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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import io.protostuff.LinkedBuffer;
import io.protostuff.Output;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Tag;

/**
 * @author Konstantin Shchepanovskyi
 */
public class RuntimeSchemaEnumTagTest
{

    @Test
    public void testWriteNumericEnum() throws Exception
    {
        RuntimeSchema<A> schema = RuntimeSchema.createFrom(A.class);
        A a = new A(TaggedEnum.TEN);
        Output output = Mockito.mock(Output.class);
        schema.writeTo(output, a);
        Mockito.verify(output).writeEnum(1, 10, false);
        Mockito.verifyNoMoreInteractions(output);
    }

    @Test
    public void testSerializeDeserializeNumericEnum() throws Exception
    {
        RuntimeSchema<A> schema = RuntimeSchema.createFrom(A.class);
        A source = new A(TaggedEnum.TEN);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LinkedBuffer buffer = LinkedBuffer.allocate();
        ProtostuffIOUtil.writeTo(outputStream, source, schema, buffer);
        byte[] bytes = outputStream.toByteArray();
        A newMessage = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, newMessage, schema);
        Assert.assertEquals(source, newMessage);
    }

    @Test
    @Ignore("https://github.com/protostuff/protostuff/issues/69")
    public void testWriteStringEnum() throws Exception
    {
        // TODO: it is not possible to create this test in simple way until we are using RuntimeEnv singleton
    }

    enum TaggedEnum
    {

        @Tag(value = 1, alias = "one")
        ONE,

        @Tag(value = 2, alias = "two")
        TWO,

        @Tag(value = 3, alias = "three")
        THREE,

        @Tag(value = 10, alias = "ten")
        TEN

    }

    static class A
    {
        private TaggedEnum x;

        public A()
        {
        }

        public A(TaggedEnum x)
        {
            this.x = x;
        }

        public TaggedEnum getX()
        {
            return x;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (!(o instanceof A))
                return false;

            A a = (A) o;

            return x == a.x;

        }

        @Override
        public int hashCode()
        {
            return x != null ? x.hashCode() : 0;
        }

        @Override
        public String toString()
        {
            return "A{" +
                    "x=" + x +
                    '}';
        }

    }
}
