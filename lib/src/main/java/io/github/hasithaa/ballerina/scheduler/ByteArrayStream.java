package io.github.hasithaa.ballerina.scheduler;

import java.io.IOException;
import java.io.InputStream;

public class ByteArrayStream extends InputStream {

    // Inefficient implementation of InputStream
    public final byte[] bytes;
    int count = 0;

    ByteArrayStream(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public int read() throws IOException {
        // Inefficient implementation
        if (count < bytes.length) {
            return bytes[count++];
        } else {
            return -1;
        }
    }

}
