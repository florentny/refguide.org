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

    public static void main(String[] args) {
        exportToJSON reef = new exportToJSON();
        reef.genExportFiles();
    }

    void genExportFiles() {

        //String[] configFile = {"/config/reeflist.xml","/config/reeflistcarib.xml", "/config/reeflistpac.xml", "/config/reeflistkeys.xml", "/config/reeflisthawaii.xml"};
        String[] configFile = {"/config/reeflist.xml"};
//        try {
//            this.buildCatSpecies(genReef35.configpath + "/config/cat.csv");
//        } catch(IOException ex) {
//            Logger.getLogger(exportToJSON.class.getName()).log(Level.SEVERE, null, ex);
//        }
        Map<String, String> family = new HashMap<>();
        try {
            ArrayList<String> species = new ArrayList<>();
            for(int z = 0; z < 1; z++) {
                buildAllData(configFile[z]);
                for(details elem : detailsList) {
                    if(species.contains(elem.name))
                        continue;
                    else
                        species.add(elem.name);
                    category fam = Family.get(elem.cat);
                    if(fam.catType.equals("Family")) {
                        family.put(elem.sname.split(" ")[0], fam.catSname[0]);
                    }

                    //System.out.println(elem.name + " " + elem.sname + " " + fam.catType + " " + fam.catSname[0] + " " + elem.cat);
                    writeSpeciesJSON(elem);
                }
            }
            for(Map.Entry<String,String> e : family.entrySet()) {
                System.out.println(e.getKey() + " " + e.getValue());
            }
        } catch(IOException ex) {
            Logger.getLogger(exportToJSON.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void writeSpeciesJSON(genReef35.details elem) throws IOException {
        FileWriter writer = new FileWriter("/home/fc/web/reef4/config/json/" + elem.name + ".json");
        try (JsonGenerator gen = Json.createGenerator(writer)) {
            getSpeciesJSON(gen, elem);
        }
        System.out.println(genReef35.configpath + "/config/json/" + elem.name + ".json");
    }

    @SuppressWarnings("unused")
    public void writeSpeciesJSON2(genReef35.details elem) throws IOException {

        //FileWriter writer = new FileWriter(genReef3.configpath + "/config/json/" + elem.name + ".json");

        StringWriter writer = new StringWriter();

        try (JsonGenerator gen = Json.createGenerator(writer)) {
            getSpeciesJSON(gen, elem);
        }

        String json = writer.toString();
        try (FileWriter file = new FileWriter(genReef35.configpath + "/config/json/" + elem.name + ".json")) {
            file.write(json);
        }

        MongoDatabase db = getMongoDB();

        db.getCollection("species").replaceOne(new Document("id", elem.name), Document.parse(json), new ReplaceOptions().upsert(true));

        System.out.println(genReef35.configpath + "/config/json/" + elem.name + ".json");
    }

    @SuppressWarnings("unused")
    public JsonGenerator getSpeciesJSON2(JsonGenerator gen, genReef35.details elem) {

        return gen;
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
        if(elem.endemic)
            endemic = true;
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
        gen.write(Integer.parseInt(elem.fishRef));
        if(!elem.fishRef2.isEmpty()) {
            gen.write(Integer.parseInt(elem.fishRef2));
        }
        if(!elem.fishRef3.isEmpty()) {
            gen.write(Integer.parseInt(elem.fishRef3));
        }
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
