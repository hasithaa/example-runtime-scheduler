package io.github.hasithaa.ballerina.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BTypedesc;

class ByteBlockConsumer<T> implements Consumer<T> {

    private final Future future;
    private final BTypedesc typedesc;

    public ByteBlockConsumer(Future future, BTypedesc typedesc) {
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