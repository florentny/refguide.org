package us.florent;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import org.bson.Document;


public class exportToJSON extends genReef35 {

    public exportToJSON() {

    }



    public void writeSpeciesJSON(genReef35.details elem) throws IOException {
        FileWriter writer = new FileWriter("/home/fc/web/reef4/config/json/" + elem.name + ".json");
        try (JsonGenerator gen = Json.createGenerator(writer)) {
            getSpeciesJSON(gen, elem);
        }
        System.out.println(genReef35.configpath + "/config/json/" + elem.name + ".json");
    }


    public JsonGenerator getSpeciesJSON(JsonGenerator gen, genReef35.details elem) {

        gen.writeStartObject();
        String[] dist_list;
        boolean endemic = false;
        if(elem.dist != null) {
            dist_list = elem.dist.split(",");
            for(String dist_list1 : dist_list) {
                if (dist_list1.equals("E")) {
                    endemic = true;
                    break;
                }
            }
        }
        //if(elem.endemic)
          //  endemic = true;
        if(elem.asname == null) {
            elem.asname = "";
        }

        gen.write("id", elem.name).write("Name", elem.fishName).write("sciName", elem.sname);
        if(!elem.size.isEmpty())
            gen.write("size", elem.size);
        if(!elem.depthraw.isEmpty())
            gen.write("depth", elem.depthraw);
        if(elem.subCat != null)
            gen.write("subCat", elem.subCat);
        if(elem.ref != null)
            gen.write("ref", elem.ref);

        if(!elem.family.isEmpty())
            if(elem.family.contains("/")) {
                gen.write("family", elem.family.split("/")[0]);
                gen.write("subFamily", elem.family.split("/")[1]);
            }
            else gen.write("family", elem.family);


        getStrings(elem.aka, "aka", gen);
        getStrings(elem.asname, "aSciName", gen);
        if(endemic)
            gen.write("endemic", true);

        getStrings(elem.distribution, "distribution", gen);

        if(elem.disp1 != null) {
            gen.writeStartArray("dispNames");
            for(int i = 0; i < elem.getCount(); i++) {
                gen.write(elem.getName(i));
            }
            gen.writeEnd();
        }
        gen.writeStartArray("thumbs");
//        gen.write(Integer.parseInt(elem.fishRef));
//        if(!elem.fishRef2.isEmpty()) {
//            gen.write(Integer.parseInt(elem.fishRef2));
//        }
//        if(!elem.fishRef3.isEmpty()) {
//            gen.write(Integer.parseInt(elem.fishRef3));
//        }
        gen.writeEnd();

        gen.writeStartArray("photos");
        for(int j = 0; j < elem.thumbList.size(); j++) {
            gen.writeStartObject().write("id", Integer.parseInt(getField(elem.thumbList.get(j), 0))).write("location", getField(elem.thumbList.get(j), 1));
            if(!getField(elem.thumbList.get(j), 3).isEmpty()) {
                gen.write("type", getField(elem.thumbList.get(j), 3));
            }
            if(!getField(elem.thumbList.get(j), 4).isEmpty()) {
                gen.write("comment", getField(elem.thumbList.get(j), 4));
            }
            if(!getField(elem.thumbList.get(j), 2).isEmpty()) {
                gen.write("depth", getField(elem.thumbList.get(j), 2));
            }
            gen.writeEnd();
        }

        gen.writeEnd();

        if(!elem.note.isEmpty()) {
            gen.write("note", elem.note);
        }

        gen.writeEnd();
        return gen;
    }

    void getStrings(String arg, String name, JsonGenerator gen) {
        if(arg == null)
            return;
        if(arg.isEmpty())
            return;
        String[] s = arg.split(",");
        gen.writeStartArray(name);
        for(String str : s)
            if(str.endsWith(" (Endemic)"))
                gen.write(str.replace(" (Endemic)", "").trim());
            else
                gen.write(str.trim());
        gen.writeEnd();
    }

}
