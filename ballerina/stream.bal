import ballerina/io;

class CustomByteSteam {

    final byte[] data;
    int counter = 0;

    public function init(byte[] data) {
        self.data = data;
    }

    public isolated function next() returns record {|byte[] value;|}|error? {
        io:println("next");
        if self.data.length() >= self.counter {
            return ();
        }
        byte[] data = [];
        int i = 0;
        while i < 4 && self.data.length() >= self.counter {
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
