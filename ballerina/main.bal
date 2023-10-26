import ballerina/http;
import ballerina/io;
import ballerina/jballerina.java;

service / on new http:Listener(9090) {

    isolated resource function get method1() returns @http:Cache {noCache: true} string?|error {
        byte[] fileReadBytes = check io:fileReadBytes("large-data.xml");
        return runInefficientByteArray(fileReadBytes, string);
    }

    isolated resource function get method2() returns @http:Cache {noCache: true} string?|error {
        stream<io:Block, io:Error?> data = check io:fileReadBlocksAsStream("large-data.xml");
        return runInefficientByteArrayStream(data, string);
    }

    isolated resource function get method3() returns @http:Cache {noCache: true} string?|error {
        stream<io:Block, io:Error?> data = check io:fileReadBlocksAsStream("large-data.xml");
        return runEfficientByteArrayStream(data, string);
    }
}

// public function main() returns error? {
//     // io:println(check testLargeXml() is string);
//     // _ = check io:fileReadXml("large-data.xml");
// }

function testSmallXml() returns string|error? {
    stream<io:Block, io:Error?> data = check io:fileReadBlocksAsStream("small-data.xml");
    return runEfficientByteArrayStream(data, string);
}

function testLargeXml() returns string|error? {
    stream<io:Block, io:Error?> data = check io:fileReadBlocksAsStream("large-data.xml");
    return runEfficientByteArrayStream(data, string);
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
    _ = check runInefficientByteArray(byteArray, string);

    // Create Stream 1
    io:println("--- Create Stream 1 ---");
    stream<byte[], error?> byteStream1 = new (new CustomByteSteam(byteArray));
    _ = check runInefficientByteArrayStream(byteStream1, string);

    // Create Stream 1
    io:println("--- Create Stream  2---");
    stream<byte[], error?> byteStream2 = new (new CustomByteSteam(byteArray));
    _ = check runEfficientByteArrayStream(byteStream2, string);
}

isolated function runInefficientByteArray(byte[] byteArray, typedesc<string> T) returns string|error? {
    io:println("Test Inefficient Byte Array Stream for ", T);
    string result = check readBytes(byteArray, T);
    return result;
}

isolated function runInefficientByteArrayStream(stream<byte[], error?> byteStream, typedesc<string> T) returns string|error? {
    io:println("Test Inefficient Byte Array Stream for ", T);
    string result = check readBytesStream(byteStream, T);
    return result;
}

isolated function runEfficientByteArrayStream(stream<byte[], error?> byteStream, typedesc<string> T) returns string|error? {
    io:println("Test Efficient Byte Array Stream for ", T);
    string result = check readBytesStreamNew(byteStream, T);
    return result;
}

isolated function readBytes(byte[] bytes, typedesc<int|string> T = <>) returns T|error = @java:Method {
    'class: "io.github.hasithaa.ballerina.scheduler.Library"
} external;

isolated function readBytesStream(stream<byte[], error?> str, typedesc<int|string> T = <>) returns T|error = @java:Method {
    'class: "io.github.hasithaa.ballerina.scheduler.Library"
} external;

isolated function readBytesStreamNew(stream<byte[], error?> str, typedesc<int|string> T = <>) returns T|error = @java:Method {
    'class: "io.github.hasithaa.ballerina.scheduler.Library"
} external;
