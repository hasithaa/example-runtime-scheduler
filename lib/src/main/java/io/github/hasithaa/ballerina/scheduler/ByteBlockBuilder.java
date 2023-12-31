package io.github.hasithaa.ballerina.scheduler;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

public class ByteBlockBuilder implements Closeable {

    private final BObject iterator;
    private final Environment env;
    private final String nextMethodName;
    private final Type returnType;
    private final String strandName;
    private final StrandMetadata metadata;
    private final Map<String, Object> properties;

    List<byte[]> chunks = new ArrayList<>();

    public ByteBlockBuilder(Environment env, BObject iterator, MethodType nextMethod) {
        this.env = env;
        this.iterator = iterator;
        this.nextMethodName = nextMethod.getName();
        this.returnType = nextMethod.getReturnType();
        this.strandName = env.getStrandName().orElse("");
        this.metadata = env.getStrandMetadata();
        this.properties = Map.of();
    }

    public void readAllBlocksAncConsumer(Consumer<Object> futureResultConsumer) throws IOException {
        if (iterator == null || nextMethodName == null || returnType == null) {
            throw new IOException("Invalid byte[] stream");
        }
        scheduleNextRead(futureResultConsumer);
    }

    private void scheduleNextRead(Consumer<Object> futureResultConsumer) {
        Callback callback = new Callback() {
            @Override
            public void notifySuccess(Object o) {
                if (o == null) {
                    System.out.println("Reading Done");
                    InputStream inputStream = new ByteBlockStream(chunks);
                    Parser parser;
                    try {
                        parser = new BytesToXmlParser(inputStream);
                        parser.parse();
                    } catch (Exception e) {
                        BError error = ErrorCreator.createError(StringUtils.fromString("Cannot read the stream"));
                        futureResultConsumer.accept(error);
                        return;
                    }
                    futureResultConsumer.accept(parser);
                    return;
                }
                System.out.println("Reading a chunk");
                if (o instanceof BMap) {
                    BMap<BString, Object> valueRecord = (BMap<BString, Object>) o;
                    final BString value = Arrays.stream(valueRecord.getKeys()).findFirst().get();
                    final BArray arrayValue = valueRecord.getArrayValue(value);
                    chunks.add(arrayValue.getByteArray());
                }
                scheduleNextRead(futureResultConsumer);
            }

            @Override
            public void notifyFailure(BError bError) {
                futureResultConsumer.accept(bError);
            }
        };
        env.getRuntime().invokeMethodAsyncSequentially(iterator,
                nextMethodName,
                strandName,
                metadata,
                callback,
                properties,
                returnType);
    }

    @Override
    public void close() {
        this.chunks.clear();
        // TODO: Close the stream
    }
}
