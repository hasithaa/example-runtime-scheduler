package io.github.hasithaa.ballerina.scheduler;

import java.io.IOException;

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

        try (ByteBlockBuilder byteBlockSteam = new ByteBlockBuilder(env, iteratorObj, BallerinaByteStream.resolveNextMethod(iteratorObj))) {
            ByteBlockConsumer<Object> transformer = new ByteBlockConsumer<>(future, typedesc);
            byteBlockSteam.readAllBlocksAncConsumer(transformer);
        } catch (Exception e) {
            return ErrorCreator.createError(
                    StringUtils.fromString("Error occurred while reading the stream: " + e.getMessage()));
        }
        return null;
    }


}
