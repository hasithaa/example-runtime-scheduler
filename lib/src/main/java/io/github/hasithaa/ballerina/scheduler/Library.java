package io.github.hasithaa.ballerina.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.*;
import io.ballerina.runtime.internal.XmlTreeBuilder;

public class Library {

    public static Object readBytes(Environment env, BArray bytes, BTypedesc t) throws IOException {

        // TODO: Check bytes is a byte array, before following line.
        ByteArrayStream inputStream = new ByteArrayStream(bytes.getBytes());
        ByteArrayParser parser = new ByteArrayParser(inputStream);
        parser.parse();
        if (t.getDescribingType().getTag() == TypeTags.STRING_TAG) {
            return StringUtils.fromString(parser.getResultString());
        } else {
            return parser.getResultInt();
        }
    }

    public static Object readBytesStream(Environment env, BStream stream, BTypedesc typedesc) {

        final BObject iteratorObj = stream.getIteratorObj();
        Future future = env.markAsync();

        try (ByteBlockBuilder byteBlockSteam = new ByteBlockBuilder(env, iteratorObj, resolveNextMethod(iteratorObj))) {
            Transformer<Object> transformer = new Transformer<>(future, typedesc);
            byteBlockSteam.readAllBlocksAncConsumer(transformer);
        } catch (Exception e) {
            return ErrorCreator.createError(
                    StringUtils.fromString("Error occurred while reading the stream: " + e.getMessage()));
        }
        return null;
    }

    private static MethodType resolveNextMethod(BObject iterator) {
        ObjectType objectType = (ObjectType) TypeUtils.getReferredType(iterator.getType());
        MethodType[] methods = objectType.getMethods();
        // Assumes compile-time validation of the iterator object
        for (MethodType method : methods) {
            if (method.getName().equals("next")) {
                return method;
            }
        }
        throw new IllegalStateException("next method not found in the iterator object");
    }

    static class Transformer<T> implements Consumer<T> {

        private final Future future;
        private final BTypedesc typedesc;

        public Transformer(Future future, BTypedesc typedesc) {
            this.future = future;
            this.typedesc = typedesc;
        }

        @Override
        public void accept(Object value) {
            if (value instanceof BError) {
                future.complete(value);
            } else if (value instanceof InputStream) {
                InputStream inputStream = (InputStream) value;
                ByteArrayParser parser = new ByteArrayParser(inputStream);
                try {
                    parser.parse();
                } catch (IOException e) {
                    BError createError = ErrorCreator.createError(StringUtils.fromString("Cannot read the stream"));
                    future.complete(createError);
                    return;
                }
                if (typedesc.getDescribingType().getTag() == TypeTags.STRING_TAG) {
                    future.complete(StringUtils.fromString(parser.getResultString()));
                } else {
                    future.complete(parser.getResultInt());
                }
            }
        }
    }

}
