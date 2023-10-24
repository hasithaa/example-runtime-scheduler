import ballerina/io;

const BUFFER_SIZE = 50;

class CustomByteSteam {

    final byte[] data;
    int counter = 0;

    public function init(byte[] data) {
        self.data = data;
    }

    public isolated function next() returns record {|byte[] value;|}|error? {
        io:println("Read Buffer from Ballerina");
        if self.data.length() < self.counter + 1 {
            return ();
        }
        byte[] data = [];
        int i = 0;
        while i < BUFFER_SIZE && self.data.length() >= self.counter + 1 {
            data.push(self.data[self.counter]);
            self.counter = self.counter + 1;
            i = i + 1;
        }
        return {value: data};
    }

    public isolated function close() returns error? {
        return ();
    }
}
