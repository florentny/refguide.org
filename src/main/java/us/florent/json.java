package us.florent;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

public class json {

    public static void main(String[] args) throws IOException {

        //Employee emp = JacksonObjectMapperExample.createEmployee();

        //JsonGenerator jsonGenerator = new JsonFactory()
               // .createGenerator(new FileOutputStream("stream_emp.txt"));

        StringWriter sw = new StringWriter();
        JsonGenerator jsonGenerator = new JsonFactory()
                .createGenerator(sw);


        //for pretty printing
        //jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());

        jsonGenerator.writeStartArray();
        jsonGenerator.writeStartObject(); // start root object
        jsonGenerator.writeNumberField("id",123445);
        jsonGenerator.writeStringField("name", "John Doe");
        jsonGenerator.writeBooleanField("permanent", true);

        jsonGenerator.writeObjectFieldStart("address"); //start address object
        jsonGenerator.writeStringField("street", "151 W80th St");
        jsonGenerator.writeStringField("city","New York");
        jsonGenerator.writeNumberField("zipcode", 10024);
        jsonGenerator.writeEndObject(); //end address object

        jsonGenerator.writeArrayFieldStart("phoneNumbers");
        //for(long num : emp.getPhoneNumbers())
            jsonGenerator.writeNumber(212453956);
            jsonGenerator.writeNumber(917445322);
        jsonGenerator.writeEndArray();

        jsonGenerator.writeStringField("role", "Dictator");

        jsonGenerator.writeArrayFieldStart("cities"); //start cities array
        //for(String city : emp.getCities())
            jsonGenerator.writeString("New York");
            jsonGenerator.writeString("Boston");
        jsonGenerator.writeEndArray(); //closing cities array

        jsonGenerator.writeObjectFieldStart("properties");
       // Set<String> keySet = emp.getProperties().keySet();
        //for(String key : keySet){
            //String value = emp.getProperties().get(key);
            jsonGenerator.writeStringField("Key1", "value1");
            jsonGenerator.writeStringField("Key2", "value2");
        //}

        jsonGenerator.writeEndObject(); //closing properties
        jsonGenerator.writeEndObject(); //closing root object
        jsonGenerator.writeEndArray();

        jsonGenerator.flush();
        System.out.println(sw.toString());
        jsonGenerator.close();
    }

}
