import ballerina/io;
import ballerina/jballerina.java;

public function main() returns error? {

    final byte[] byteArray = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

    // Inefficient Byte Array
    io:println("Test Inefficient Byte Array Stream");
    string s = check readBytes(byteArray);
    io:println(s);

    // Create Stream 1
    stream<byte[], error?> strm1 = new (new CustomByteSteam(byteArray));

    // Create Stream 1
    stream<byte[], error?> strm2 = new (new CustomByteSteam(byteArray));
}

function testNative(stream<byte[], error?> str) {

}

function readBytes(byte[] bytes, typedesc<int|string> T = <>) returns T|error = @java:Method {
    'class: "io.github.hasithaa.ballerina.scheduler.Library"
} external;
