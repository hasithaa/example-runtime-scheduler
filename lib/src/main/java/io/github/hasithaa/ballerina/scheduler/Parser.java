package io.github.hasithaa.ballerina.scheduler;

public interface Parser {
    void parse() throws Exception;

    String getResultString();

    Integer getResultInt();
}
