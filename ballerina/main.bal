import ballerina/io;
import ballerina/jballerina.java;

public function main() returns error? {

    final byte[] byteArray = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

    // Inefficient Byte Array
    check runInefficientByteArrayStream(byteArray, string);
    check runInefficientByteArrayStream(byteArray, int);

    // Create Stream 1
    stream<byte[], error?> byteStream = new (new CustomByteSteam(byteArray));
    check runEfficientByteArrayStream(byteStream, string);
    check runEfficientByteArrayStream(byteStream, int);
}

function runInefficientByteArrayStream(byte[] byteArray, typedesc<int|string> T) returns error? {
    io:println("Test Inefficient Byte Array Stream for ", T);
    string|int result = check readBytes(byteArray, T);
    io:println(result);
}

function runEfficientByteArrayStream(stream<byte[], error?> byteStream, typedesc<int|string> T) returns error? {
    io:println("Test Efficient Byte Array Stream for ", T);
    int|string result = check readBytesStream(byteStream, T);
    io:println(result);
}

function readBytes(byte[] bytes, typedesc<int|string> T = <>) returns T|error = @java:Method {
    'class: "io.github.hasithaa.ballerina.scheduler.Library"
} external;

function readBytesStream(stream<byte[], error?> str, typedesc<int|string> T = <>) returns T|error = @java:Method {
    'class: "io.github.hasithaa.ballerina.scheduler.Library"
} external;
