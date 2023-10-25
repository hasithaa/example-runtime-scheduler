package io.github.hasithaa.ballerina.scheduler;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.*;

public class Library {

    public static Object readBytes(Environment env, BArray bytes, BTypedesc t) throws Exception {

        // TODO: Check bytes is a byte array, before following line.
        ByteArrayStream inputStream = new ByteArrayStream(bytes.getBytes());
        Parser parser = new BytesToXmlParser(inputStream);
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
            ByteBlockConsumer<Object> transformer = new ByteBlockConsumer<>(future, typedesc);
            byteBlockSteam.readAllBlocksAncConsumer(transformer);
        } catch (Exception e) {
            return ErrorCreator.createError(
                    StringUtils.fromString("Error occurred while reading the stream: " + e.getMessage()));
        }
        return null;
    }

    public static Object readBytesStreamNew(Environment env, BStream stream, BTypedesc typedesc) {

        final BObject iteratorObj = stream.getIteratorObj();
        Future future = env.markAsync();
        ByteBlockConsumer<Object> transformer = new ByteBlockConsumer<>(future, typedesc);
        try (var byteBlockSteam = new BallerinaByteStream(env, iteratorObj, resolveNextMethod(iteratorObj),
                transformer)) {
            Parser parser = new BytesToXmlParser(byteBlockSteam);
            parser.parse();
            transformer.accept(parser);
        } catch (Exception e) {
            return ErrorCreator.createError(
                    StringUtils.fromString("Error occurred while reading the stream: " + e.getMessage()));
        }
        return null;
    }

    static MethodType resolveNextMethod(BObject iterator) {
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

}
