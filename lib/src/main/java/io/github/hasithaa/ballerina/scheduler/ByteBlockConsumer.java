package io.github.hasithaa.ballerina.scheduler;

import java.util.function.Consumer;

import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.TypeTags;
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
        } else if (value instanceof ByteArrayParser) {
            ByteArrayParser parser = (ByteArrayParser) value;
            if (typedesc.getDescribingType().getTag() == TypeTags.STRING_TAG) {
                future.complete(StringUtils.fromString(parser.getResultString()));
            } else {
                future.complete(parser.getResultInt());
            }
        }
    }
}