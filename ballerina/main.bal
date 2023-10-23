import ballerina/io;
import ballerina/jballerina.java;

public function main() returns error? {

    xml x = xml `<web>
                    <name>Ballerina Tips</name>
                    <url>https://bal.tips</url>
                    <author>Hasitha Aravinda</author>
                    <year>2022</year>
                </web>`;

    final byte[] byteArray = x.toString().toBytes();

    // Inefficient Byte Array
    check runInefficientByteArrayStream(byteArray, string);

    // Create Stream 1
    io:println("--- Create Stream  ---");
    stream<byte[], error?> byteStream1 = new (new CustomByteSteam(byteArray));
    check runEfficientByteArrayStream(byteStream1, string);
}

function runInefficientByteArrayStream(byte[] byteArray, typedesc<int|string> T) returns error? {
    io:println("Test Inefficient Byte Array Stream for ", T);
    string|int result = check readBytes(byteArray, T);
    io:println(result);
}

function runEfficientByteArrayStream(stream<byte[], error?> byteStream, typedesc<int|string> T) returns error? {
    io:println("Test Inefficient Byte Array Stream for ", T);
    int|string result = check readBytesStream(byteStream, T);
    io:println(result);
}

function readBytes(byte[] bytes, typedesc<int|string> T = <>) returns T|error = @java:Method {
    'class: "io.github.hasithaa.ballerina.scheduler.Library"
} external;

function readBytesStream(stream<byte[], error?> str, typedesc<int|string> T = <>) returns T|error = @java:Method {
    'class: "io.github.hasithaa.ballerina.scheduler.Library"
} external;
