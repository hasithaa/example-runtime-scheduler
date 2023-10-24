import ballerina/io;
import ballerina/jballerina.java;

public function main() returns error? {
    check testLargeXML();
}

function testLargeXML() returns error? {
    stream<io:Block, io:Error?> data = check io:fileReadBlocksAsStream("large-data.xml");
    check runEfficientByteArrayStream(data, string);
}

function testSimple() returns error? {
    xml x = xml `<webs><web>
                    <name>Ballerina Tips</name>
                    <url>https://bal.tips</url>
                    <author>Hasitha Aravinda</author>
                    <year>2022</year>
                </web><web>
                    <name>Ballerina Tips</name>
                    <url>https://bal.tips</url>
                    <author>Hasitha Aravinda</author>
                    <year>2022</year>
                </web><web>
                    <name>Ballerina Tips</name>
                    <url>https://bal.tips</url>
                    <author>Hasitha Aravinda</author>
                    <year>2022</year>
                </web></webs>`;

    final byte[] byteArray = x.toString().toBytes();

    // Inefficient Byte Array
    check runInefficientByteArray(byteArray, string);

    // Create Stream 1
    io:println("--- Create Stream 1 ---");
    stream<byte[], error?> byteStream1 = new (new CustomByteSteam(byteArray));
    check runInefficientByteArrayStream(byteStream1, string);

    // Create Stream 1
    io:println("--- Create Stream  2---");
    stream<byte[], error?> byteStream2 = new (new CustomByteSteam(byteArray));
    check runEfficientByteArrayStream(byteStream2, string);
}

function runInefficientByteArray(byte[] byteArray, typedesc<int|string> T) returns error? {
    io:println("Test Inefficient Byte Array Stream for ", T);
    string|int result = check readBytes(byteArray, T);
    io:println(result);
}

function runInefficientByteArrayStream(stream<byte[], error?> byteStream, typedesc<int|string> T) returns error? {
    io:println("Test Inefficient Byte Array Stream for ", T);
    int|string result = check readBytesStream(byteStream, T);
    io:println(result);
}

function runEfficientByteArrayStream(stream<byte[], error?> byteStream, typedesc<int|string> T) returns error? {
    io:println("Test Efficient Byte Array Stream for ", T);
    int|string result = check readBytesStreamNew(byteStream, T);
    io:println(result is string);
}

function readBytes(byte[] bytes, typedesc<int|string> T = <>) returns T|error = @java:Method {
    'class: "io.github.hasithaa.ballerina.scheduler.Library"
} external;

function readBytesStream(stream<byte[], error?> str, typedesc<int|string> T = <>) returns T|error = @java:Method {
    'class: "io.github.hasithaa.ballerina.scheduler.Library"
} external;

function readBytesStreamNew(stream<byte[], error?> str, typedesc<int|string> T = <>) returns T|error = @java:Method {
    'class: "io.github.hasithaa.ballerina.scheduler.Library"
} external;
