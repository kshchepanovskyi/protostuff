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

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Test;

public class LinkBufferTest
{

    @Test
    public void testBasics() throws Exception
    {
        LinkBuffer buf = new LinkBuffer(8);

        // put in 4 longs:
        ByteBuffer bigBuf = ByteBuffer.allocate(100);
        bigBuf.limit(100);

        // each one of these writes gets its own byte buffer.
        buf.writeByteBuffer(bigBuf); // 0
        buf.writeByteArray(new byte[100]); // 1
        buf.writeByteArray(new byte[2]); // 2
        buf.writeByteArray(new byte[8]); // 3
        buf.writeInt64(1);
        buf.writeInt64(2);
        buf.writeInt64(3);
        buf.writeInt64(4);

        List<ByteBuffer> lbb = buf.finish();
        assertEquals(8, lbb.size());
        assertEquals(100, lbb.get(0).remaining());
        assertEquals(100, lbb.get(1).remaining());
        assertEquals(2, lbb.get(2).remaining());
        assertEquals(8, lbb.get(3).remaining());
        for (int i = 3; i < lbb.size(); i++)
        {
            assertEquals(8, lbb.get(i).remaining());
        }
    }

    @Test
    public void testGetBuffers() throws Exception
    {
        LinkBuffer b = new LinkBuffer(8);
        b.writeInt32(42);
        b.writeInt32(43);
        b.writeInt32(44);
        List<ByteBuffer> buffers = b.getBuffers();
        assertEquals(2, buffers.size());
        assertEquals(8, buffers.get(0).remaining());
        assertEquals(4, buffers.get(1).remaining());
        assertEquals(42, buffers.get(0).getInt());
        assertEquals(43, buffers.get(0).getInt());
        assertEquals(44, buffers.get(1).getInt());
    }

    @Test
    public void testGetBuffersAndAppendData() throws Exception
    {
        LinkBuffer b = new LinkBuffer(8);
        b.writeInt32(42);
        b.writeInt32(43);
        b.writeInt32(44);
        List<ByteBuffer> buffers = b.getBuffers();
        b.writeInt32(45); // new data should not appear in buffers
        assertEquals(2, buffers.size());
        assertEquals(8, buffers.get(0).remaining());
        assertEquals(4, buffers.get(1).remaining());
    }
}
