package io.github.hasithaa.ballerina.scheduler;

import java.io.IOException;
import java.io.InputStream;

public class MockInputStream extends InputStream {

    // Inefficient implementation of InputStream
    public final byte[] bytes;
    int count = 0;
    boolean efficient = false;

    MockInputStream(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public int read() throws IOException {
        if (efficient) {
            return -1; // TODO : Implement this.
        }

        // Inefficient implementation
        if (count < bytes.length) {
            return bytes[count++];
        } else {
            return -1;
        }
    }

}
