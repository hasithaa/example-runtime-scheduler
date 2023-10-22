package io.github.hasithaa.ballerina.scheduler;

import java.io.IOException;
import java.io.InputStream;

public class ByteArrayParser {

    private InputStream inputStream;
    StringBuilder resultBuilder = new StringBuilder();
    int result = 0;

    ByteArrayParser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    void parse() throws IOException {
        while (true) {
            int c = inputStream.read();
            if (c == -1) {
                break;
            }

            result = result + c;
            resultBuilder.append(c);
            System.out.println(">" +  c);
        }
    }

    public String getResultString() {
        return "string :" + resultBuilder.toString();
    }

    public Integer getResultInt() {
        return result;
    }
}
