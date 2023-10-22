package io.github.hasithaa.ballerina.scheduler;

import java.io.IOException;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.*;

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

    public static Object readBytesStream(Environment env, BStream bytes, BTypedesc t) {

        if (t.getDescribingType().getTag() == TypeTags.STRING_TAG) {
            return StringUtils.fromString("TODO");
        } else {
            return -1;
        }
    }

}
