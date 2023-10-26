import ballerina/io;

xmlns "http://www.example.com/products" as pro;
xmlns "http://www.example.com/customers" as customers;

public function main() returns error? {

    io:WritableByteChannel wbc = check io:openWritableFile("ballerina//large-data.xml");
    _ = check wbc.write("<root>\n".toBytes(), 0);
    _ = check wbc.write("<products>\n".toBytes(), 0);
    foreach int i in 0 ... 50000 {
        _ = check wbc.write(createProduct(i).toString().toBytes(), 0);
    }
    _ = check wbc.write("</products>\n".toBytes(), 0);
    _ = check wbc.write("<customers>\n".toBytes(), 0);
    foreach int i in 0 ... 50000 {
        _ = check wbc.write(createCustomer(i).toString().toBytes(), 0);
    }
    _ = check wbc.write("</customers>\n".toBytes(), 0);
    _ = check wbc.write("</root>\n".toBytes(), 0);
}

function createProduct(int id) returns xml {
    string name = "Product" + id.toString();
    string description = "This is a " + name;
    int price = 100 + id;
    string category = "category" + id.toString();
    xml product = xml `<pro:product id="${id}" name="${name}">
        <description>${description}</description>
        <price currency="USD">${price}</price>
        <category>${category}</category>
    </pro:product>`;
    return product;
}

function createCustomer(int id) returns xml {
    string name = "Customer" + id.toString();
    string address = "Address" + id.toString();
    string city = "City" + id.toString();
    string country = "Country" + id.toString();
    xml customer = xml `<customers:customer id="${id}" name="${name}">
        <address>${address}</address>
        <email>${name}@example.com</email>
        <city>${city}</city>
        <country>${country}</country>
    </customers:customer>`;
    return customer;
}
