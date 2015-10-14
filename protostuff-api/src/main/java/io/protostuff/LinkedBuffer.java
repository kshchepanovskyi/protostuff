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

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * A buffer that wraps a byte array and has a reference to the next buffer for dynamic increase.
 *
 * @author David Yu
 * @author Kostiantyn Shchepanovskyi
 */
@NotThreadSafe
public final class LinkedBuffer {

    /**
     * The minimum buffer size for a {@link LinkedBuffer}.
     */
    public static final int MIN_BUFFER_SIZE = 256;

    /**
     * The default buffer size for a {@link LinkedBuffer}.
     */
    public static final int DEFAULT_BUFFER_SIZE = 512;

    final byte[] buffer;
    final int start;
    int offset;
    LinkedBuffer next;

    /**
     * Creates a buffer with the specified {@code size}.
     */
    LinkedBuffer(int size) {
        this(new byte[size], 0, 0);
    }

    /**
     * Creates a buffer with the specified {@code size} and appends to the provided buffer {@code
     * appendTarget}.
     */
    LinkedBuffer(int size, LinkedBuffer appendTarget) {
        this(new byte[size], 0, 0, appendTarget);
    }

    /**
     * Uses the buffer starting at the specified {@code offset}
     */
    LinkedBuffer(byte[] buffer, int offset) {
        this(buffer, offset, offset);
    }

    LinkedBuffer(byte[] buffer, int start, int offset) {
        this.buffer = buffer;
        this.start = start;
        this.offset = offset;
    }

    /**
     * Uses the buffer starting at the specified {@code offset} and appends to the provided buffer
     * {@code appendTarget}.
     */
    LinkedBuffer(byte[] buffer, int offset, LinkedBuffer appendTarget) {
        this(buffer, offset, offset);
        appendTarget.next = this;
    }

    LinkedBuffer(byte[] buffer, int start, int offset, LinkedBuffer appendTarget) {
        this(buffer, start, offset);
        appendTarget.next = this;
    }

    /**
     * Creates a view from the buffer {@code viewSource} and appends the view to the provided buffer
     * {@code appendTarget}.
     */
    LinkedBuffer(LinkedBuffer viewSource, LinkedBuffer appendTarget) {
        buffer = viewSource.buffer;
        offset = start = viewSource.offset;
        appendTarget.next = this;
    }

    /**
     * Allocates a new buffer with default size.
     */
    public static LinkedBuffer allocate() {
        return new LinkedBuffer(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Allocates a new buffer with the specified size.
     */
    public static LinkedBuffer allocate(int size) {
        int bufferSize = Math.max(size, MIN_BUFFER_SIZE);
        return new LinkedBuffer(bufferSize);
    }

    /**
     * Allocates a new buffer with the specified size and appends it to the previous buffer.
     */
    public static LinkedBuffer allocate(int size, LinkedBuffer previous) {
        int bufferSize = Math.max(size, MIN_BUFFER_SIZE);
        return new LinkedBuffer(bufferSize, previous);
    }

    /**
     * Wraps the byte array buffer as a read-only buffer.
     */
    public static LinkedBuffer wrap(byte[] array, int offset, int length) {
        return new LinkedBuffer(array, offset, offset + length);
    }

    /**
     * Uses the existing byte array as the internal buffer.
     */
    public static LinkedBuffer use(byte[] buffer) {
        return use(buffer, 0);
    }

    /**
     * Uses the existing byte array as the internal buffer.
     */
    public static LinkedBuffer use(byte[] buffer, int start) {
        if (buffer.length - start <= 0) {
            throw new IllegalArgumentException("No space left in the given buffer");
        }
        return new LinkedBuffer(buffer, start, start);
    }

    /**
     * Writes the contents of this {@link LinkedBuffer} into {@link OutputStream}.
     *
     * @return number of bytes written into the {@link OutputStream}.
     */
    public int writeTo(OutputStream out) throws IOException {
        LinkedBuffer node = this;
        int contentSize = 0, len;
        do {
            if ((len = node.offset - node.start) > 0) {
                out.write(node.buffer, node.start, len);
                contentSize += len;
            }
        } while ((node = node.next) != null);
        return contentSize;
    }

    /**
     * Writes the contents of this {@link LinkedBuffer} into {@link OutputStream}.
     *
     * @return number of bytes written into the {@link OutputStream}.
     */
    public int writeTo(DataOutput out) throws IOException {
        LinkedBuffer node = this;
        int contentSize = 0, len;
        do {
            if ((len = node.offset - node.start) > 0) {
                out.write(node.buffer, node.start, len);
                contentSize += len;
            }
        } while ((node = node.next) != null);
        return contentSize;
    }

    /**
     * The offset will be reset to its starting position. The buffer next to this will be
     * dereferenced.
     */
    public LinkedBuffer clear() {
        next = null;
        offset = start;
        return this;
    }

}
