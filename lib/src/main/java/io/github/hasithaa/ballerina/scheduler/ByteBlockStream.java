package io.github.hasithaa.ballerina.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ByteBlockStream extends InputStream {

    private final List<byte[]> chunks;

    private byte[] currentChunk = new byte[0];
    private int nextChunkIndex = 0;

    ByteBlockStream(List<byte[]> chunks) {
        this.chunks = chunks;
    }

    public int read() {
        if (hasBytesInCurrentChunk()) {
            return currentChunk[nextChunkIndex++];
        }
        // Need to get a new block from the stream, before reading again.
        nextChunkIndex = 0;
        if (readNextChunk()) {
            return read();
        }
        return -1;
    }

    @Override
    public void close() throws IOException {
        super.close();
        chunks.clear();
    }

    private boolean readNextChunk() {
        if (chunks.size() == 0) {
            currentChunk = new byte[0];
            return false;
        }
        currentChunk = chunks.remove(0);
        return true;
    }

    private boolean hasBytesInCurrentChunk() {
        return currentChunk.length != 0 && nextChunkIndex < currentChunk.length;
    }

}
