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
package io.protostuff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * From https://groups.google.com/forum/#!topic/protostuff/D7Bb1REf8pQ (Anil Pandge)
 */
public class CodedDataInputTest extends TestCase
{

    public static void main(String[] args)
    {
        try
        {

            SampleClass _clazz = new SampleClass();
            List<String> testStrings = new ArrayList<>();
            for (int i = 0; i < 1800; i++)
            {
                String test = new String("TestingString" + i);
                testStrings.add(test);

                try
                {
                    _clazz.setTestStringList(testStrings);
                    byte[] serialize = serialize(_clazz);
                    System.out.println("Payload Size = " + serialize.length);
                    SampleClass deserialize = deserialize(serialize);
                    System.out.println(deserialize.getTestStringList().get(i));

                }
                catch (Exception ex)
                {
                    System.out.println("Failed");
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

    }

    public void testIt() throws Exception
    {
        SampleClass _clazz = new SampleClass();
        List<String> testStrings = new ArrayList<>();
        for (int i = 0; i < 1800; i++)
        {
            String test = new String("TestingString" + i);
            testStrings.add(test);

            _clazz.setTestStringList(testStrings);
            byte[] serialize = serialize(_clazz);
            assertNotNull(deserialize(serialize));
        }
    }

    private static byte[] serialize(final SampleClass t) throws Exception
    {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            oos = new ObjectOutputStream(baos);
            t.writeExternal(oos);
        }
        catch (IOException e)
        {
            throw new Exception(e);
        }
        finally
        {
            tryClose(oos);
        }
        return baos.toByteArray();
    }

    private static SampleClass deserialize(final byte[] bytes) throws Exception
    {
        final SampleClass t = new SampleClass();
        ObjectInputStream ois = null;
        try
        {
            ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            t.readExternal(ois);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            tryClose(ois);
        }
        return t;
    }

    private static void tryClose(Closeable closeable) throws Exception
    {
        try
        {
            closeable.close();
        }
        catch (IOException e)
        {
            throw new Exception(e);
        }
    }
}
