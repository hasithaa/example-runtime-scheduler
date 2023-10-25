package io.github.hasithaa.ballerina.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

public class BallerinaByteStream extends InputStream {

    private byte[] currentChunk = new byte[0];
    private int nextChunkIndex = 0;

    private final BObject iterator;
    private final Environment env;
    private final String nextMethodName;
    private final Type returnType;
    private final String strandName;
    private final StrandMetadata metadata;
    private final Map<String, Object> properties;
    private final Consumer<Object> futureResultConsumer;
    private AtomicInteger counter = new AtomicInteger(0);
    AtomicBoolean done = new AtomicBoolean(false);

    public BallerinaByteStream(Environment env, BObject iterator, MethodType nextMethod,
            Consumer<Object> futureResultConsumer) {
        this.env = env;
        this.iterator = iterator;
        this.nextMethodName = nextMethod.getName();
        this.returnType = nextMethod.getReturnType();
        this.strandName = env.getStrandName().orElse("");
        this.metadata = env.getStrandMetadata();
        this.properties = Map.of();
        this.futureResultConsumer = futureResultConsumer;
    }

    @Override
    public int read() throws IOException {
        if (done.get()) {
            System.out.println("DONE");
            return -1;
        }
        if (hasBytesInCurrentChunk()) {
            return currentChunk[nextChunkIndex++];
        }
        // Need to get a new block from the stream, before reading again.
        nextChunkIndex = 0;
        try {
            if (readNextChunk()) {
                return read();
            }
        } catch (InterruptedException e) {
            BError error = ErrorCreator
                    .createError(StringUtils.fromString("Cannot read the stream, interrupted error"));
            futureResultConsumer.accept(error);
            return -1;
        }
        System.out.println("DONE2");
        return -1;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    private boolean readNextChunk() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Callback callback = new Callback() {
            @Override
            public void notifyFailure(BError bError) {
                System.out.println("XX-Reading error");
                done.set(true);
                futureResultConsumer.accept(bError);
                currentChunk = new byte[0];
                semaphore.release();
            }

            @Override
            public void notifySuccess(Object result) {
                if (result == null) {
                    System.out.println("XX-Reading Done " + counter.get());
                    done.set(true);
                    // futureResultConsumer.accept(parser);
                    currentChunk = new byte[0];
                    semaphore.release();
                    return;
                }
                if (result instanceof BMap) {
                    System.out.println("XX-Reading a chunk " + counter.get());
                    BMap<BString, Object> valueRecord = (BMap<BString, Object>) result;
                    final BString value = Arrays.stream(valueRecord.getKeys()).findFirst().get();
                    final BArray arrayValue = valueRecord.getArrayValue(value);
                    currentChunk = arrayValue.getByteArray();
                    semaphore.release();
                    return;
                } else {
                    // TODO : Investigate why this is happening.
                    System.out.println("XX-Reading unknown " + counter.get());
                    System.out.println(result.toString());
                    done.set(true);
                    semaphore.release();
                }
            }

        };
        env.getRuntime().invokeMethodAsyncSequentially(iterator,
                nextMethodName,
                strandName,
                metadata,
                callback,
                properties,
                returnType);
        System.out.println("XX-Reading wait " + counter.incrementAndGet());
        semaphore.acquire();
        return !done.get();
    }

    private boolean hasBytesInCurrentChunk() {
        return currentChunk.length != 0 && nextChunkIndex < currentChunk.length;
    }

}
