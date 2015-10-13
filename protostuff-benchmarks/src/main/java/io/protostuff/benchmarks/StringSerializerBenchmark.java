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
package io.protostuff.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import io.protostuff.LinkedBuffer;
import io.protostuff.StringSerializer;
import io.protostuff.WriteSession;

@Fork(1)
@State(Scope.Thread)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StringSerializerBenchmark {
    @Param({"1", "10", "100", "1000", "10000", "100000"})
    private int stringLength;

    private String s;
    private LinkedBuffer sharedBuffer;
    private WriteSession sharedSession;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StringSerializerBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void prepare() throws IOException {
        sharedBuffer = LinkedBuffer.allocate(512);
        sharedSession = new WriteSession(sharedBuffer);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringLength; i++) {
            sb.append('.');
        }
        s = sb.toString();
    }

    @Benchmark
    public byte[] builtInSerializer() {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Benchmark
    public byte[] bufferedSerializer() {
        try {
            final WriteSession session = new WriteSession(sharedBuffer);
            StringSerializer.writeUTF8(s, session, sharedBuffer);
            return session.toByteArray();
        } finally {
            sharedBuffer.clear();
        }
    }

    @Benchmark
    public byte[] bufferedRecycledSerializer() {
        final WriteSession session = this.sharedSession;
        try {
            StringSerializer.writeUTF8(s, session, session.head);
            return session.toByteArray();
        } finally {
            session.clear();
        }
    }

}
