package us.florent;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import org.bson.Document;

@SuppressWarnings("StatementWithEmptyBody")
public class genReef35 {

    public final boolean useJSON = true;
    public final boolean useMongo = true;

    static protected class details implements Cloneable {

        String name;
        String disp1;
        String disp2;
        String disp3;
        String cat;
        String subCat;
        String distribution;
        String dist;
        String size;
        String depth;
        String depthraw;
        String fishName;
        String note;
        String aka;
        String ref;
        final String thumbDetail;
        String sname;
        String asname;
        String fishRef;
        String fishRef2;
        String fishRef3;
        String indexLink;
        details next;
        details prev;
        String date;
        group group;
        String family;
        boolean endemic;
        java.util.List<String> thumbList;
        String distributionRaw;

        protected details() {
            name = "";
            cat = "";
            distribution = "";
            distributionRaw = "";
            size = "";
            depth = "";
            depthraw = "";
            fishName = "";
            note = "";
            aka = "";
            ref = null;
            thumbDetail = "";
            sname = "";
            fishRef = "";
            fishRef2 = "";
            fishRef3 = "";
            disp1 = null;
            disp2 = null;
            disp3 = null;
            subCat = null;
            family = "";
            endemic = false;
            thumbList = new java.util.ArrayList<>();
        }

        protected int getCount() {
            if(disp3 != null) {
                return 3;
            }
            if(disp2 != null) {
                return 2;
            }
            return 1;
        }

        protected String getName(int j) {
            switch(j) {
                case 0:
                    if(disp1 != null) {
                        return disp1;
                    }   break;
                case 1:
                    return disp2;
                case 2:
                    return disp3;
                default:
                    break;
            }
            return fishName;
        }

        protected String getFishRef(int j) {
            if(j == 1) {
                return fishRef2;
            }
            if(j == 2) {
                return fishRef3;
            }
            return fishRef;

        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof details) {
                return this.name.equals(((details) o).name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();

        }

        @Override
        public details clone() throws CloneNotSupportedException {
            return (details) super.clone();
        }
    }

    protected static class group {

        String name;
        int start;
        int end;
        int page;
        int index;
        boolean last;
        String maxCol;
        String icon;
        String icon2;
        int iconIndex;
        int iconIndex2;

        public group() {
            page = 1;
            last = true;
            maxCol = "0";
            icon = null;
        }
    }

    protected static class category {
        String[] catSname;
        String catType;
        String[] subSname;
        final String icon;
        int count;
        String img;
        final ArrayList<details> species;

        public category() {
            catType = "Family";
            catSname = new String[1];
            catSname[0] = "";
            subSname = new String[1];
            subSname[0] = "";
            icon = "";
            count = 0;
            img = "";
            species = new ArrayList<>();
        }
    }

    String basepathIndexAll = null;
    public boolean analytics = false;
    protected int numPhotos = 0;
    protected java.util.ArrayList<details> detailsList = new java.util.ArrayList<>();
    protected java.util.ArrayList<group> groupList = new java.util.ArrayList<>();
    protected java.util.HashMap<String, java.util.TreeSet<Integer>> keyworkList = new java.util.HashMap<>();
    protected java.util.HashMap<String, Integer> pageSearch = new java.util.HashMap<>();
    final java.util.Map<String, String> catSpecies = new java.util.HashMap<>();
    java.util.Map<String, Integer> ClassFirst = new java.util.HashMap<>();
    java.util.Map<String, category> Family = new java.util.HashMap<>();

    final String[] reefId = {"all","carib", "indopac", "hawaii", "keys","baja"};
    final String[] reefName = {"Tropical Reefs","Caribbean Reefs","Tropical Pacific Reefs","South Florida Reefs","Hawaii Reefs","Eastern Pacific Reefs"};
    final String[] preReefName = {"","Florida, Bahamas &","","","",""};
    final String[] reefMenu = {"Worldwide", "Caribbean", "Pacific", "South Florida", "Hawaii", "Eastern Pacific"};

    int __count = 0;

    public genReef35() {

        Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
        mongoLogger.setLevel(Level.SEVERE);

    }

    protected void buildCatSpecies(String filename) throws IOException {
        java.io.BufferedReader file = new java.io.BufferedReader(new java.io.FileReader(filename));
        String line;
        while((line = file.readLine()) != null) {
            String[] field = line.split(",");
            if(field.length > 1) {
                catSpecies.put(field[0], field[1]);
            }
        }

    }

    public void createSite(String path, boolean analytics) {
        try {
            basepathIndexAll = path;
            String basepathIndexIndoPac = path + "/indopac";
            String basepathIndexHawaii = path + "/hawaii";
            String basepathIndexCarib = path + "/carib";
            String basepathIndexKeys = path + "/keys";
            String basepathIndexBaja = path + "/baja";
            String[] hearderAll = {"banner1", "banner2", "banner3"};
            String[] hearderCarib = {"banner1", "banner3"};
            String[] hearderIndopac = {"banner2"};
            String[] hearderKeys = {"banner1", "banner3"};
            String[] hearderHawaii = {"banner2"};

            this.analytics = analytics;
            System.out.println("================= Site " + path + " =================");
            System.out.println("Processing worldwide:");
            int all = process("/config/reeflist.xml", basepathIndexAll, 0, hearderAll);
            int all_pic = numPhotos;
            System.out.println("Processing Caribbean:");
            int carib = process("/config/reeflistcarib.xml", basepathIndexCarib, 1, hearderCarib);
            int carib_pic = numPhotos;
            System.out.println("Processing Indo-Pacific:");
            int indopac = process("/config/reeflistpac.xml", basepathIndexIndoPac, 2,  hearderIndopac);
            int indopac_pic = numPhotos;
            System.out.println("Processing Florida Keys:");
            int keys = process("/config/reeflistkeys.xml", basepathIndexKeys, 3, hearderKeys);
            int key_pics = numPhotos;
            System.out.println("Processing Hawaii:");
            int hawaii = process("/config/reeflisthawaii.xml", basepathIndexHawaii, 4, hearderHawaii);
            int hawaii_pics = numPhotos;
            System.out.println("Processing Baja:");
            /* int baja = */ process("/config/reeflistbaja.xml", basepathIndexBaja, 5, hearderHawaii);
            //int baja_pics = numPhotos;


            String outString = readFile("about.html");
            outString = outString.replace("__ALL__", Integer.toString(all));
            outString = outString.replace("__ALLPIC__", Integer.toString(all_pic));
            outString = outString.replace("__CARIB__", Integer.toString(carib));
            outString = outString.replace("__CARIBPIC__", Integer.toString(carib_pic));
            outString = outString.replace("__INDOPAC__", Integer.toString(indopac));
            outString = outString.replace("__INDOPACPIC__", Integer.toString(indopac_pic));
            outString = outString.replace("__KEYS__", Integer.toString(keys));
            outString = outString.replace("__KEYSPIC__", Integer.toString(key_pics));
            outString = outString.replace("__HAWAII__", Integer.toString(hawaii));
            outString = outString.replace("__HAWAIIPIC__", Integer.toString(hawaii_pics));

            if(analytics) {
                outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
            } else {
                outString = outString.replaceAll("__ANALYTICS__", "");
            }

            if(!compareToFile(outString, basepathIndexAll + "/about.html")) {
                try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(basepathIndexAll + "/about.html"))) {
                    outFile.write(outString);
                }
                System.out.println(basepathIndexAll + "/about.html");
            }

            outString = readFile("home.html");
            outString = outString.replace("__ALL__", Integer.toString(all));
            outString = outString.replace("__CARIB__", Integer.toString(carib));
            outString = outString.replace("__INDOPAC__", Integer.toString(indopac));
            outString = outString.replace("__KEYS__", Integer.toString(keys));
            outString = outString.replace("__HAWAII__", Integer.toString(hawaii));

            if(analytics) {
                outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
            } else {
                outString = outString.replaceAll("__ANALYTICS__", "");
            }

            if(!compareToFile(outString, basepathIndexAll + "/home.html")) {
                try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(basepathIndexAll + "/home.html"))) {
                    outFile.write(outString);
                }
                System.out.println(basepathIndexAll + "/home.html");
            }

            outString = readFile("search.html");
            if(analytics) {
                outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
            } else {
                outString = outString.replaceAll("__ANALYTICS__", "");
            }

            if(!compareToFile(outString, basepathIndexAll + "/search.html")) {
                try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(basepathIndexAll + "/search.html"))) {
                    outFile.write(outString);
                }
                System.out.println(basepathIndexAll + "/search.html");
            }

            outString = readFile("unknow.html");
            outString = outString.replace("__MAIN__", getUnknowSpecies(configpath + "/config/unknow.txt"));

            if(analytics) {
                outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
            } else {
                outString = outString.replaceAll("__ANALYTICS__", "");
            }

            if(!compareToFile(outString, basepathIndexAll + "/unknow.html")) {
                try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(basepathIndexAll + "/unknow.html"))) {
                    outFile.write(outString);
                }
                System.out.println(basepathIndexAll + "/unknow.html");
            }

        } catch(IOException ex) {
            Logger.getLogger(genReef35.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    void buildAllData(String configFile) {
        buildDB(configFile);

        int index = 1;
        for(group elem : groupList) {
            if(elem.index == 0) {
                elem.index = index++;
            }
            if(elem.index < 0) {
                elem.index *= -1;
                index = elem.index + 1;
            }
            if(elem.icon != null) {
                for(int i = elem.start; i < elem.end; i++) {
                    details d = detailsList.get(i);
                    if(d.name.equals(elem.icon)) {
                        elem.iconIndex = i;
                    }
                }
            } else {
                elem.iconIndex = elem.start;
            }
            if(elem.icon2 != null) {
                for(int i = elem.start; i < elem.end; i++) {
                    details d = detailsList.get(i);
                    if(d.name.equals(elem.icon2)) {
                        elem.iconIndex2 = i;
                    }
                }
            } else {
                elem.iconIndex2 = elem.iconIndex;
            }
        }
        ClassFirst = new java.util.HashMap<>();
        groupList.forEach((elem) -> {
            details node = detailsList.get(elem.start);
            String sp_class = getSpeciesClass(node.cat);
            if(! ClassFirst.containsKey(sp_class)) {
                ClassFirst.put(sp_class, elem.index);
            }
        });
    }

    static MongoClient mongoClient = null;
    static private MongoDatabase db  = null;
    protected MongoDatabase getMongoDB() {
        if(db == null) {
            mongoClient = MongoClients.create();
            db = mongoClient.getDatabase("reef");
        }
        return db;
    }

    @SuppressWarnings("unused")
    protected MongoDatabase getMongoDB4() {
        if(db == null) {
            mongoClient = MongoClients.create();
            db = mongoClient.getDatabase("reef4");
        }
        return db;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    protected void buildDB(String Config) {

        try {
            if(useMongo) {
                db = getMongoDB();
            }
            detailsList = new java.util.ArrayList<>();
            groupList = new java.util.ArrayList<>();
            keyworkList = new java.util.HashMap<>();
            pageSearch = new java.util.HashMap<>();
            Family = new java.util.HashMap<>();
            java.io.BufferedReader reeffile = new java.io.BufferedReader(new java.io.FileReader(configpath + Config));
            String masterline;
            String cat = "";
            details prev = null;
            group _group = null;
            while((masterline = reeffile.readLine()) != null) {
                masterline = masterline.trim();
                if(masterline.startsWith("<category")) {
                    //cat = masterline.split("=")[1];
                    //if(subdir == null)
                    //   subdir = getName(masterline);
                    cat = getName(masterline);
                    category fam = new category();
                    getSname(masterline, fam);
                    Family.put(cat, fam);
                } else if(masterline.startsWith("<dir")) {
                    _group = new group();
                    _group.name = getName(masterline);
                    masterline = reeffile.readLine();
                    _group.maxCol = getMaxCol(masterline);
                    _group.index = Integer.parseInt(getIndex(masterline));
                    _group.icon = getIcon(masterline);
                    _group.icon2 = getIcon2(masterline);
                    _group.start = detailsList.size();
                    if(groupList.size() > 0) {
                        groupList.get(groupList.size() - 1).end = _group.start;
                    }
                    groupList.add(_group);
                } else if(masterline.startsWith("<page")) {
                    _group = new group();
                    _group.name = groupList.get(groupList.size() - 1).name;
                    _group.maxCol = getMaxCol(masterline);
                    _group.index = Integer.parseInt(getIndex(masterline));
                    _group.icon = getIcon(masterline);
                    _group.icon2 = getIcon2(masterline);
                    _group.page = groupList.get(groupList.size() - 1).page + 1;
                    groupList.get(groupList.size() - 1).last = false;
                    _group.start = detailsList.size();
                    if(groupList.size() > 0) {
                        groupList.get(groupList.size() - 1).end = _group.start;
                    }
                    groupList.add(_group);
                } else //noinspection StatementWithEmptyBody
                    if(masterline.equals("</category>")) {
                    //subdir = null;
                } else if(masterline.startsWith("<")) {
                } else if(masterline.equals("")) {
                } else {
                    details node = new details();
                    node.name = masterline;
                    node.prev = prev;
                    prev = node;
                    node.cat = cat;
                    Family.get(cat).count++;
                    Family.get(cat).species.add(node);
                    node.group = _group;
                    if(useJSON) {
                        loadSpeciesJSON(node, masterline, db);
                    } else {
                        try (java.io.BufferedReader fishfile = new java.io.BufferedReader(new java.io.FileReader(configpath + "/config/" + masterline))) {
                            node.fishName = fishfile.readLine();
                            String nextLine = fishfile.readLine();
                            if(nextLine.split("=")[0].equals("disp1")) {
                                node.disp1 = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("disp2")) {
                                node.disp2 = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("disp3")) {
                                node.disp3 = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("subcat")) {
                                node.subCat = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("sn")) {
                                node.sname = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("family")) {
                                node.family = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("asn")) {
                                node.asname = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("dist")) {
                                node.dist = nextLine.split("=")[1];
                                node.distribution = getDistribution(node.dist);
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("size")) {
                                node.size = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("depth")) {
                                node.depth = nextLine.split("=")[1];
                                node.depthraw = node.depth;
                                long sd = depthmetric(Integer.parseInt(node.depth.split("-")[0]));
                                long ed = depthmetric(Integer.parseInt(node.depth.split("-")[1]));
                                node.depth += " ft. (" + sd + "-" + ed + " m)";
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("AKA")) {
                                node.aka = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("note")) {
                                node.note = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("ref")) {
                                node.ref = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            node.fishRef = nextLine.split("=")[1]; // thumb=
                            nextLine = fishfile.readLine();
                            if(nextLine.split("=")[0].equals("thumb2")) {
                                node.fishRef2 = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            if(nextLine.split("=")[0].equals("thumb3")) {
                                node.fishRef3 = nextLine.split("=")[1];
                                nextLine = fishfile.readLine();
                            }
                            String thumbDetail;
                            while((thumbDetail = nextLine) != null) {
                                node.thumbList.add(thumbDetail);
                                nextLine = fishfile.readLine();
                            }
                        }
                    }
                    if(detailsList.size() > 0) {
                        detailsList.get(detailsList.size() - 1).next = node;
                    }
                    detailsList.add(node);
                }
            }
            detailsList.get(detailsList.size() - 1).next = detailsList.get(0);
            detailsList.get(0).prev = detailsList.get(detailsList.size() - 1);
            groupList.get(groupList.size() - 1).end = detailsList.size();



        } catch(Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

//        for(details d : detailsList) {
//            System.out.println(d.fishName + " (" + d.sname + ")");
//        }

    }

    private void loadSpeciesJSON(details node, String masterline, MongoDatabase db) throws Exception {
        //System.out.println(masterline);
        JsonReader reader;
        if(useMongo) {
            String json = loadSpeciesMongo(masterline, db);
            reader = Json.createReader(new StringReader(json));
        } else {
            reader = Json.createReader(new FileReader(configpath + "/config/json/" + masterline + ".json"));
        }
        JsonObject jsonst = reader.readObject();
        if(!jsonst.getString("id").equals(masterline)) {
            throw new Exception("ID does not match file name!");
        }
        node.fishName = jsonst.getString("Name");
        node.sname = jsonst.getString("sciName");
        if(jsonst.containsKey("size"))
            node.size = jsonst.getString("size");
        if(jsonst.containsKey("depth")) {
            node.depthraw = jsonst.getString("depth");
            long sd = depthmetric(Integer.parseInt(node.depthraw.split("-")[0]));
            long ed = depthmetric(Integer.parseInt(node.depthraw.split("-")[1]));
            node.depth = node.depthraw + " ft. (" + sd + "-" + ed + " m)";
        }
        if(jsonst.containsKey("note"))
            node.note = jsonst.getString("note");
        if(jsonst.containsKey("subCat"))
            node.subCat = jsonst.getString("subCat");
        if(jsonst.containsKey("ref"))
            node.ref = jsonst.getString("ref");
        if(jsonst.containsKey("family"))
            node.family = jsonst.getString("family");
        if(jsonst.containsKey("subFamily"))
            node.family += "/" + jsonst.getString("subFamily");
        if(jsonst.containsKey("aka")) {
            StringBuilder str = new StringBuilder();
            JsonArray array = jsonst.getJsonArray("aka");
            array.forEach((val) ->  str.append(((JsonString)val).getString()).append(", "));
            node.aka = str.substring(0, str.length() - 2);
        }
        if(jsonst.containsKey("aSciName")) {
            StringBuilder str = new StringBuilder();
            JsonArray array = jsonst.getJsonArray("aSciName");
            array.forEach((val) ->  str.append(((JsonString)val).getString()).append(", "));
            node.asname = str.substring(0, str.length() - 2);
        }
        if(jsonst.containsKey("dispNames")) {
            JsonArray array = jsonst.getJsonArray("dispNames");
            node.disp1 = array.getString(0);
            if(array.size() >= 2)
                node.disp2 = array.getString(1);
            if(array.size() == 3)
                node.disp3 = array.getString(2);
        }
        if(jsonst.containsKey("thumbs")) {
            JsonArray array = jsonst.getJsonArray("thumbs");
            node.fishRef = Integer.toString(array.getInt(0));
            if(array.size() >= 2)
                node.fishRef2 = Integer.toString(array.getInt(1));
            if(array.size() == 3)
                node.fishRef3 = Integer.toString(array.getInt(2));
        }
        if(jsonst.containsKey("distribution")) {
            StringBuilder str = new StringBuilder();
            JsonArray array = jsonst.getJsonArray("distribution");
            array.forEach((val) ->  str.append(((JsonString)val).getString()).append(", "));
            node.distribution = str.substring(0, str.length() - 2);
        }
        node.distributionRaw = node.distribution;
        if(jsonst.containsKey("endemic")) {
            node.distribution += " (Endemic)";
            node.endemic = true;
        }

        StringBuilder str;
        JsonArray array = jsonst.getJsonArray("photos");
        for(JsonValue v: array) {
            str = new StringBuilder();
            JsonObject o = (JsonObject) v;
            str.append(o.getInt("id")).append(":").append(o.getString("location")).append(":");
            if(o.containsKey("depth"))
                str.append(o.getString("depth"));
            str.append(":");
            if(o.containsKey("type"))
                str.append(o.getString("type"));
            str.append(":");
            if(o.containsKey("comment"))
                str.append(o.getString("comment"));
            node.thumbList.add(str.toString());
        }


    }

    private String loadSpeciesMongo(String masterline, MongoDatabase db) {
        FindIterable<Document> iterable = db.getCollection("species").find(new Document("id", masterline));
        return Objects.requireNonNull(iterable.first()).toJson();
    }

    private String getName(String masterline) {
        return masterline.split("name=\"")[1].split("\"")[0];
    }

    private void getSname(String masterline, category fam) {
        if(!masterline.contains("sname")) {
            return;
        }
        String cat = masterline.split("sname=\"")[1].split("\"")[0];
        if(cat.contains("+")) {
            String[] list = cat.split("\\+");
            fam.catSname = new String[list.length];
            System.arraycopy(list, 0, fam.catSname, 0, list.length);
        }
        else
            fam.catSname[0] = cat;

        if(masterline.contains("subname")) {
            String subcat = masterline.split("subname=\"")[1].split("\"")[0];
            if(subcat.contains("+")) {
                String[] list = subcat.split("\\+");
                fam.subSname = new String[list.length];
                System.arraycopy(list, 0, fam.subSname, 0, list.length);
            }
            else
                fam.subSname[0] = subcat;


        }

        if(masterline.contains("stype")) {
            fam.catType = masterline.split("stype=\"")[1].split("\"")[0];
        }

        if(masterline.contains("img")) {
            fam.img = masterline.split("img=\"")[1].split("\"")[0];
        }
    }

    private String getMaxCol(String masterline) {
        if(!masterline.contains("maxcol")) {
            return "0";
        }
        return masterline.split("maxcol=\"")[1].split("\"")[0];
    }

    private String getIndex(String masterline) {
        if(!masterline.contains("index")) {
            return "0";
        }
        return masterline.split("index=\"")[1].split("\"")[0];
    }

    private String getIcon(String masterline) {
        if(!masterline.contains("icon")) {
            return null;
        }
        return masterline.split("icon=\"")[1].split("\"")[0];
    }

    private String getIcon2(String masterline) {
        if(!masterline.contains("icon2")) {
            return null;
        }
        return masterline.split("icon2=\"")[1].split("\"")[0];
    }

    protected int depthmetric(int d) {
        long r = Math.round(d * 0.3048);
        r = (r == 49) ? 50 : r;
        r = (r == 76) ? 75 : r;
        r = (r == 24) ? 25 : r;
        r = (r == 61) ? 60 : r;
        r = (r == 46) ? 45 : r;
        r = (r == 14) ? 15 : r;
        r = (r == 9) ? 10 : r;
        r = (r == 11) ? 12 : r;
        r = (r == 183) ? 180 : r;
        r = (r == 366) ? 350 : r;
        r = (r == 213) ? 200 : r;
        r = (r == 79) ? 80 : r;
        r = (r == 91) ? 90 : r;
        r = (r == 122) ? 120 : r;
        r = (r == 101) ? 100 : r;
        r = (r == 73) ? 75 : r;
        return (int) r;
    }

    protected int process(String configFile,
                          String baseIndex,
                          int reefRef,
                          String[] headers) {

        int headercount = 0;
        numPhotos = 0;
        buildAllData(configFile);
        try {
            genIndexFile(detailsList, baseIndex, 0, detailsList.size(), "index_all.html", "", "", "0", reefRef, headers[0]);

            genCatalogFiles(detailsList, baseIndex, reefRef, headers[0]);
            String prevname;
            String nextname;

            for(int i = 0; i < groupList.size(); i++) {
                if(i == (groupList.size() - 1)) {
                    details d = detailsList.get(groupList.get(0).iconIndex);
                    nextname = d.cat;

                } else {
                    details d = detailsList.get(groupList.get(i + 1).iconIndex);
                    nextname = d.cat;

                }
                details d;
                if(i == 0) {
                    d = detailsList.get(groupList.get(groupList.size() - 1).iconIndex2);

                } else {
                    d = detailsList.get(groupList.get(i - 1).iconIndex2);

                }
                prevname = d.cat;
                String indexName = "index" + groupList.get(i).index + ".html";
                genIndexFile(detailsList, baseIndex, groupList.get(i).start, groupList.get(i).end, indexName, nextname, prevname, groupList.get(i).maxCol, reefRef, headers[headercount]);
                if(++headercount == headers.length) {
                    headercount = 0;
                }
            }
            genFamilyIndex(detailsList, baseIndex, reefRef, headers[0]);

            headercount = 0;
            String newcat = detailsList.get(0).cat;
            for(details elem : detailsList) {
                if(!newcat.equals(elem.cat)) {
                    headercount = 0;
                    newcat = elem.cat;
                }
                if(__count++ < 10000) {
                    genFishFile(elem, baseIndex, reefRef, headers[headercount]);
                }
                if(++headercount == headers.length) {
                    headercount = 0;
                }
            }
            //genSearchFiles(baseIndex);

            int start = 0;
            String cat = detailsList.get(0).cat;
            for(int i = 0; i < detailsList.size(); i++) {
                if(!detailsList.get(i).cat.equals(cat)) {
                    cat = detailsList.get(i).cat;
                    genCatFile(start, i, baseIndex, reefRef, headers[0]);
                    start = i;

                }
            }
            genCatFile(start, detailsList.size(), baseIndex, reefRef, headers[0]);
            //genXML(detailsList, baseIndex);

            // gen latest file
            java.util.ArrayList<details> latestList = new java.util.ArrayList<>();
            String latestline;
            String date;
            java.io.BufferedReader latestfile = new java.io.BufferedReader(new java.io.FileReader(configpath + "/config/latest"));
            while((latestline = latestfile.readLine()) != null) {
                latestline = latestline.trim();
                date = null;
                if(latestline.contains("|")) {
                    date = latestline.split("\\|")[1];
                    latestline = latestline.split("\\|")[0];
                }
                for(details elem : detailsList) {
                    if(elem.name.equals(latestline)) {
                        @SuppressWarnings("UnusedAssignment")
                        details newdetails = null;
                        try {
                            newdetails = elem.clone();
                        } catch(CloneNotSupportedException ex) {
                            ex.printStackTrace();
                            newdetails = new details();
                        }
                        newdetails.subCat = newdetails.cat;
                        newdetails.cat = "Latest Updates";
                        newdetails.date = date;
                        latestList.add(newdetails);
                        break;
                    }
                }
            }
            int size = Math.min(20, latestList.size());
            genIndexFile(latestList, baseIndex, 0, size, "latest.html", "", "", "0", reefRef, headers[0]);
            genRSS(latestList, baseIndex);

            updateMongo(reefRef, baseIndex);

            copyFile(baseIndex + "/index1.html", baseIndex + "/index.html");

            // writeSpeciesListJSON(reefRef);


        } catch(IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("Number of species: " + detailsList.size());
        return detailsList.size();
    }

    @SuppressWarnings("unused")
    public void writeSpeciesListJSON_X(int reefRef) throws IOException {
        exportToJSON genJSON = new exportToJSON();

        String area = reefId[reefRef];
        FileWriter writer = new FileWriter(genReef35.configpath + "/json/" + area + "_species_list.json");
        JsonGenerator gen = Json.createGenerator(writer).writeStartObject();

        gen.write("area", area).writeStartArray("Species");
        for(details elem : detailsList) {
            gen = genJSON.getSpeciesJSON(gen, elem);
        }

        gen.writeEnd().writeEnd();
        gen.close();

    }

    private void updateMongo(int region, String baseIndex) throws IOException {
        if(baseIndex.contains("clean")) {
            return;
        }
        String area = reefId[region];

        db.getCollection("species_by_regions").deleteMany(new Document("region", area));
        MongoCollection<Document> c = db.getCollection("species_by_regions");
        String subcat;
        StringWriter writer = new StringWriter();
        try (JsonGenerator gen = Json.createGenerator(writer)) {
            gen.writeStartArray();
            for(details elem : detailsList) {
                if(elem.subCat == null) {
                    subcat = elem.cat;
                }
                else
                    subcat = elem.subCat;
                c.insertOne(new Document("region", area).append("name", elem.name).append("cat", elem.cat).append("subCat", subcat));
                gen.writeStartObject();
                gen.write("name", elem.name).write("fullname", elem.fishName).write("sname", elem.sname).write("category", elem.cat)
                        .write("subcategory", subcat).write("size", elem.size).write("depth", elem.depth).write("thumb1", elem.fishRef);
                gen.writeEnd();
            }
            gen.writeEnd();
            gen.flush();
        }
        writer.flush();
        String json = writer.toString();

        try (FileWriter file = new FileWriter(genReef35.configpath + "/species_region_" + region + ".json")) {
            file.write(json);
        }


    }

    private void genRSS(ArrayList<genReef35.details> latestList, String baseIndex) throws IOException {
        if(!baseIndex.equals(basepathIndexAll)) {
            return;
        }
        StringBuilder outString = new StringBuilder();

        outString.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n<rss version=\"2.0\">");
        outString.append("<channel>\n<title>Reefguide.org</title>\n");
        outString.append("<link>http://reefguide.org</link>");
        outString.append("<description>A Guide To The Tropical Reefs</description>");

        latestList.forEach((node) -> {
            outString.append("<item><title>");
            outString.append(node.fishName).append("</title><link>http://reefguide.org/").append(node.name).append(".html</link>");
            outString.append("<description>&lt;img src=\"http://reefguide.org/pix/thumb3/").append(node.name).append(node.fishRef).append(".jpg\" /&gt;&lt;br /&gt;");
            outString.append(node.fishName).append(" (").append(node.sname).append(")&lt;br /&gt;");
            outString.append("Category: ").append(node.subCat).append("&lt;br /&gt;");
            outString.append("Size: ").append(node.size).append("&lt;br /&gt;");
            outString.append("Depth: ").append(node.depth).append("&lt;br /&gt;");
            outString.append("Distribution: ").append(node.distribution).append("&lt;br /&gt;");
            outString.append("</description>");
            if(node.date != null) {
                outString.append("<pubDate>").append(node.date).append("</pubDate>");

            }
            outString.append("</item>");
        });
        outString.append("</channel></rss>");

        if(!compareToFile(outString.toString(), baseIndex + "/reefguide.xml")) {
            try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(baseIndex + "/reefguide.xml"))) {
                outFile.write(outString.toString());
            }
            System.out.println(baseIndex + "/reefguide.xml");
        }

    }

    protected void genCatFile(int start,
                              int end,
                              String baseIndex,
                              int reefRef,
                              String header) throws IOException {

        String outString = readFile("index_cat.html");
        outString = processSelectedGuideMenu(outString, reefRef);
        StringBuilder img_reef = new StringBuilder();
        StringBuilder reef_name = new StringBuilder();
        StringBuilder sci_name = new StringBuilder();
        StringBuilder cat_reef = new StringBuilder();
        StringBuilder ref_reef = new StringBuilder();
        StringBuilder link_reef = new StringBuilder();
        String preName="";
        if(preReefName[reefRef].length() > 0) {
            preName = "<span class=\"pretitle\">" + preReefName[reefRef] + "</span>";
        }
        String next = "";
        String prev = "";

        String base = "";
        if(!baseIndex.equals(basepathIndexAll)) {
            base = "../";
        }
        String subdir = "";
        int count1 = 0;
        for(int i = start; i < end; i++) {
            details node = detailsList.get(i);
            for(String thumbList : node.thumbList) {
                //node.indexLink = name;
                count1++;
                //img_reef.append("\"pix/thumb/" + node.name + node.fishRef + ".jpg\",");
                img_reef.append("\"").append(base).append("pix/thumb/").append(node.name).append(getField(thumbList, 0)).append(".jpg\",");
                link_reef.append("\"").append(node.name).append(".html\",");
                reef_name.append("\"").append(node.fishName);
                if(!getField(thumbList, 3).equals("")) {
                    reef_name.append(" - ").append(getField(thumbList, 3));
                }
                reef_name.append("\",");
                sci_name.append("\"").append(node.sname).append("\",");
                if(!subdir.equals(node.cat)) {
                    subdir = node.cat;
                    cat_reef.append("\"").append(subdir).append("\",");
                    ref_reef.append(i - start).append(",");
                }
            }
        }
        ref_reef.append(count1);
        img_reef.deleteCharAt(img_reef.length() - 1);
        link_reef.deleteCharAt(link_reef.length() - 1);
        reef_name.deleteCharAt(reef_name.length() - 1);
        sci_name.deleteCharAt(sci_name.length() - 1);
        cat_reef.deleteCharAt(cat_reef.length() - 1);
        //ref_reef.deleteCharAt(ref_reef.length() - 1);
        if(ref_reef.toString().equals("0")) {
            ref_reef.append(",").append(end - start);
        }



        outString = outString.replaceAll("__IMG_REEF__", img_reef.toString());
        outString = outString.replaceAll("__LINK_REEF__", link_reef.toString());
        outString = outString.replaceAll("__NAME_REEF__", reef_name.toString());
        outString = outString.replaceAll("__NAME_SCI__", sci_name.toString());
        outString = outString.replaceAll("__CAT_REEF_", cat_reef.toString());
        outString = outString.replaceAll("__REF_REEF__", ref_reef.toString());
        outString = outString.replaceAll("__MAX_COL__", "0");
        outString = outString.replaceAll("__PREV__", prev);

        outString = outString.replaceAll("__NEXT__", next);
        outString = outString.replaceAll("__REEF__", reefName[reefRef]);
        outString = outString.replaceAll("__PRENAME__", preName);
        outString = outString.replaceAll("__BASE__", base);
        outString = outString.replaceAll("__BANNER__", header);

        if(analytics) {
            outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
        } else {
            outString = outString.replaceAll("__ANALYTICS__", "");
        }


        StringBuilder html = new StringBuilder();
        html.append("<tbody>");
        subdir = "";
        int col = 0;
        for(int i = start; i < end; i++) {
            details node = detailsList.get(i);
            if(!subdir.equals(node.cat)) {
                if(!subdir.equals("")) {
                    html.append("</tr></table>");
                }
                subdir = node.cat;
                html.append("<tr><td><div id=\"catheader\">").append(subdir).append("</div></td></tr>\n");
                html.append("<tr><td><table align=\"center\"><tr>");
                col = 0;
            }
            if((col != 0) && ((col % 3) == 0)) {
                html.append("</tr></table><tr><td><table align=\"center\"><tr>");
            }
            html.append("<td width=\"240px\" height=\"170px\"><img src=\"").append(base).append("pix/thumb/").append(node.name).append(node.fishRef).append(".jpg\" alt=\"").append(node.fishName).append(" - ").append(node.sname).append("\" title=\"").append(node.fishName).append(" - ").append(node.sname).append("\" />\n");
            html.append("<br /><div class=\"nameid\"><a href=\"").append(node.name).append(".html\">").append(node.fishName).append("</a></div></td>");
            col++;
        }
        html.append("</tr></table></td></tr></tbody>\n");

        outString = outString.replaceAll("__INDEX_HTML__", html.toString());
        outString = outString.replaceAll("__TITLE__", " - " + subdir + " - Show all");

        boolean sameFile = compareToFile(outString, baseIndex + "/" + detailsList.get(start).cat.replace(' ', '_') + ".html");
        if(!sameFile) {
            try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(baseIndex + "/" + detailsList.get(start).cat.replace(' ', '_') + ".html"))) {
                outFile.write(outString);
            }
            System.out.println(baseIndex + "/" + detailsList.get(start).cat.replace(' ', '_') + ".html");
        }


    }

    static int count =0;
    protected void genFishFile(genReef35.details node, String baseIndex, int reefRef, String header) throws IOException {

        String outString = readFile("species.html");

        outString = processSelectedGuideMenu(outString, reefRef);

        StringBuilder output = new StringBuilder();
        StringBuilder text = new StringBuilder();
        String base = "";
        if(!baseIndex.equals(basepathIndexAll)) {
            base = "../";
        }
        String prename = "";
        if(preReefName[reefRef].length() > 0) {
            prename = "<span class=\"pretitle\">" + preReefName[reefRef] + "</span>";
        }
        outString = outString.replaceAll("__REEFREF__", Integer.toString(reefRef));
        outString = outString.replaceAll("__REEF__", reefName[reefRef]);
        outString = outString.replaceAll("__PRENAME__", prename);
        outString = outString.replaceAll("__CLASS__", getSpeciesClass(node.cat));

        outString = outString.replaceAll("__GROUP__", node.group.name);
        if((node.subCat != null) && ! node.cat.equals(node.subCat)) {
            outString = outString.replaceAll("__SUBCAT__",
                    "<div class=\"navbox\" style=\"margin-left: 15px;\"><span class=\"ui-icon ui-icon-triangle-1-e\" style=\"display: inline-block; vertical-align: middle\"></span><span style=\"margin-left: 0px;\">" + node.subCat  +"</span></div>");
        }
        else
            outString = outString.replaceAll("__SUBCAT__", "");

        if(getSpeciesClass(node.cat).equals(node.group.name)) {
            outString = outString.replaceAll("__HIDDEN1__", "style=\"display: none;\"");
            outString = outString.replaceAll("__CLASSINDEX__", node.indexLink);
        }
        else {
            outString = outString.replaceAll("__HIDDEN1__", "");
            outString = outString.replaceAll("__CLASSINDEX__", "index" + ClassFirst.get(getSpeciesClass(node.cat)).toString() + ".html");
        }

        if(getSpeciesClass(node.cat).equals(node.cat)) {
            outString = outString.replaceAll("__HIDDEN2__", "display: none; ");
            outString = outString.replaceAll("__ICONSTYLE__", "ui-icon ui-icon-triangle-1-s");
        }
        else {
            outString = outString.replaceAll("__HIDDEN2__", "");
            outString = outString.replaceAll("__ICONSTYLE__", "ui-icon ui-icon-triangle-1-e");
        }

        outString = outString.replaceAll("__TITLE__", node.fishName + " - " + node.sname + " - " + node.cat + " - " + node.aka);
        outString = outString.replaceAll("__NAME2__", node.fishName);
        if((node.thumbList.size() == 1) && (! getField(node.thumbList.get(0), 3).isEmpty())) {
            outString = outString.replaceAll("__NAME__", node.fishName + " - " + getField(node.thumbList.get(0), 3));
        } else {
            outString = outString.replaceAll("__NAME__", node.fishName);
        }
        if(!node.sname.equals("")) {
            outString = outString.replaceAll("__SCINAME__", "<span class=\"details\">Scientific Name: </span><span class=\"sntitle\">" + node.sname + "</span>");
        } else {
            outString = outString.replaceAll("__SCINAME__", "");
        }
        if((node.asname != null) && (!node.asname.equals(""))) {
            outString = outString.replaceAll("__ASCINAME__", "<span class=\"details\">Synonyms: </span><span class=\"sntitle2\">" + node.asname + "</span>");
        } else {
            outString = outString.replaceAll("__ASCINAME__", "");
        }
        category fam = Family.get(node.cat);
        if(!fam.catSname[0].equals("")) {
            String catType = fam.catType;
            String f = fam.catSname[0];
            if(!node.family.equals("")) {
                f = node.family.split("/")[0];
                catType = "Family";
            }
            outString = outString.replaceAll("__FAM__", "<span class=\"details\">" + catType + ": </span><span class=\"sntitle\">" + f + "</span>");
        } else {
            outString = outString.replaceAll("__FAM__", "");
        }
        if(!fam.subSname[0].equals("")) {
            String f = fam.subSname[0];
            if(!node.family.equals(""))
                f = node.family.split("/")[1];
            outString = outString.replaceAll("__SUBFAM__", "<span class=\"details\">" + "Subfamily" + ": </span><span class=\"sntitle\">" + f + "</span>");
        } else {
            outString = outString.replaceAll("__SUBFAM__", "");
        }

        String cat = node.cat;
        if(node.subCat != null) {
            cat = node.subCat;
        }
        outString = outString.replaceAll("__CAT__", node.cat);
        outString = outString.replaceAll("__CAT1__", node.cat.replaceAll(" ", "_"));
        outString = outString.replaceAll("__MAINCAT__", cat);
        outString = outString.replaceAll("__DIST1__", node.distribution + " - " + node.aka);
        if(!node.distribution.equals("")) {
            outString = outString.replaceAll("__DIST__", "<span class=\"details\">Distribution: </span><span class=\"details2\">" + node.distribution + "</span>");
        } else {
            outString = outString.replaceAll("__DIST__", "");
        }
        if(!node.aka.equals("")) {
            outString = outString.replaceAll("__AKA__", "<span class=\"details\">Also known as: </span><span class=\"details2\">" + node.aka + "</span>");
        } else {
            outString = outString.replaceAll("__AKA__", "");
        }
        if(!node.note.equals("")) {
            outString = outString.replaceAll("__NOTE__", "<span class=\"details\">" + "Note: " + processNote(node.note) + "</span><br />");
        } else {
            outString = outString.replaceAll("__NOTE__", "");
        }
        outString = outString.replaceAll("__NEXT__", node.next.name + ".html");
        outString = outString.replaceAll("__PREV__", node.prev.name + ".html");
        outString = outString.replaceAll("__INDEX__", node.indexLink);
        outString = outString.replaceAll("__BANNER__", header);
        outString = outString.replaceAll("__NEXTNAME__", node.next.fishName);
        outString = outString.replaceAll("__PREVNAME__", node.prev.fishName);
        outString = outString.replaceAll("__NEXTIMG__", node.next.name + node.next.fishRef);
        outString = outString.replaceAll("__PREVIMG__", node.prev.name + node.prev.fishRef);
        outString = outString.replaceAll("__BASE__", base);
        if(analytics) {
            outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
        } else {
            outString = outString.replaceAll("__ANALYTICS__", "");
        }

        if(!node.size.equals("")) {
            text.append("<span class=\"details\">Size: </span><span class=\"details2\">");
            text.append(node.size);
            text.append("</span>&nbsp;&nbsp;");
        }
        outString = outString.replaceAll("__SIZE__", text.toString());
        text = new StringBuilder();
        if(!node.depth.equals("")) {
            text.append("<span class=\"details\">Depth: </span><span class=\"details2\">");
            text.append(node.depth);
            text.append("</span>");
        }
        outString = outString.replaceAll("__DEPTH__", text.toString());

        for(int i = 0; i < node.thumbList.size(); i++) {
            String thumbElem = node.thumbList.get(i);
            String thumbimg = node.name + getField(thumbElem, 0);

            output.append("<div class=\"galleryspan\">\n");
            if(node.thumbList.size() > 1) {
                output.append("<a class=\"pixsel\" href=\"pixhtml/").append(thumbimg).append(".html\">");
                String title = node.fishName + " - " + node.sname + " - " + getField(thumbElem, 1);
                output.append("<img class=\"selframe\" src=\"").append(base).append("pix/thumb2/").append(thumbimg).append(".jpg\" alt=\"").append(title).append("\" title=\"").append(title).append("\"/></a>\n");
                output.append(" <div class=\"main2\">").append(getField(thumbElem, 1)).append("</div>\n");
                if(isFieldEither(thumbElem, 3, 4)) {
                    output.append(" <div class=\"main3\">").append(getField(thumbElem, 3, 4)).append("</div>\n");
                } else {
                    output.append(" <div class=\"main3\">&nbsp;</div>\n");
                }
            } else {
                String title = node.fishName + " - " + node.sname + " - " + getField(thumbElem, 1);
                output.append("<img class=\"selframe\" src=\"").append(base).append("pix/").append(thumbimg).append(".jpg\" alt=\"").append(title).append("\" title=\"").append(title).append("\"/></a>\n");
                output.append("<div>");
                String div = "";
                if( ! getField(thumbElem, 4).isEmpty()) {
                    output.append(getField(thumbElem, 4));
                    div = " / ";
                }
                if( ! getField(thumbElem, 1).isEmpty()) {
                    output.append(div).append("Location: ").append(getField(thumbElem, 1));
                    div = " / ";
                }
                if( ! getField(thumbElem, 2).isEmpty())
                    output.append(div).append("Depth: ").append(getField(thumbElem, 2)).append(" feet");
                output.append("</div>");
            }
            output.append("</div>\n");
            if(count++ < 100000)
                genFishPixFile(node, i, baseIndex, reefRef, header);
        }

        text = new StringBuilder();
        StringBuilder afterText = new StringBuilder();

        if(numInCat(node.group, node.cat) > 0) {
            //text.append("<div class=\"ui-state-default ui-corner-all\"><br /><br /><br />");
            StringBuilder str;
            boolean before = true;
            for(int i = node.group.start; i < node.group.end; i++) {

                details same = detailsList.get(i);
                if(same.name.equals(node.name)) {
                    before = false;
                    //continue;
                }
                if(!same.cat.equals(node.cat)) {
                    continue;
                }
                str = new StringBuilder();
                int num = same.getCount();
                for(int j = 0; j < num; j++) {
                    if(same.name.equals(node.name)) {
                        str.append("<div class=\"infoimg\"><img src=\"").append(base).append("pix/thumb3/").append(same.name).append(same.getFishRef(j)).append(".jpg\" alt=\"\" title=\"\" /><div>").append(same.fishName).append("</div></div><br />");
                        break;
                    }
                    else
                        str.append("<div class=\"infoimg\"><a href=\"").append(same.name).append(".html\"><img src=\"").append(base).append("pix/thumb3/").append(same.name).append(same.getFishRef(j)).append(".jpg\" alt=\"\" title=\"\" /><div>").append(same.getName(j)).append("</div></a></div><br />");
                }
                if(before)
                    afterText.append(str);
                else
                    text.append(str);

            }
            text.append(afterText);

            java.util.Map<String,String> list = ListCatInGroup(node.group);
            for(String type : list.keySet()) {
                if( ! type.equals(node.cat))
                    text.append("<div class=\"navbox\" style=\"margin-left: 10px;\"><a href=\"").append(list.get(type)).append(".html\"><span class=\"ui-icon ui-icon-triangle-1-e\" style=\"display: inline-block; vertical-align: middle\"></span><span style=\"margin-left: 0px;\">").append(type).append("</span></a></div>");
            }




        }
        outString = outString.replaceAll("__SAMELIST__", text.toString());
        outString = outString.replaceAll("__COPYRIGHT__","<br />All Photographs<br />&copy; 2020 Florent Charpin");


        // Ext ref
        StringBuilder extRef = new StringBuilder();
        if(catSpecies.get(node.cat) != null) {
            if((catSpecies.get(node.cat).equals("Fish"))
                    || (catSpecies.get(node.cat).equals("Creature"))
                    || (catSpecies.get(node.cat).equals("Mammals"))) {
                if ((node.ref == null) || (!node.ref.equals("none"))) {
                    String[] sn;
                    if((node.ref == null)) {
                        sn = node.sname.split(" ");
                    } else {
                        sn = node.ref.split(" ");
                        if(sn.length == 3) {
                            sn[1] += " " + sn[2];
                        }
                    }
                    if(sn.length >= 2) {
                        if(!(sn[1].equals("sp.") || sn[1].equals("spp.")))
                            if (catSpecies.get(node.cat).equals("Fish")) {
                                extRef.append(FishBase(sn[0], sn[1]));
                            } else {
                                extRef.append(SeaLifeBase(sn[0], sn[1]));
                            }
                    }
                }
            }
        } else {
            System.out.println("Cat missing in cat.csv: " + node.cat);
        }

        if(extRef.length() > 0) {
            extRef.insert(0, "<div class=\"ui-state-default ui-corner-all infobox\"><div class=\"inforefs\">External Reference: ");
            extRef.append("</div></div>");
            outString = outString.replaceAll("__EXTREF__", extRef.toString());
        }
        else
            outString = outString.replaceAll("__EXTREF__", "");

//        splitKeywords(keywords, node.sname);
//        splitKeywords(keywords, node.fishName);
//        splitKeywords(keywords, node.cat);
//        splitKeywords(keywords, node.distribution);
//        addToSearch(node.name, keywords.toString());
        //genSearchFiles(baseIndex);

        outString = outString.replaceAll("__FISH_HTML__", output.toString());
        if(!compareToFile(outString, baseIndex + "/" + node.name + ".html")) {
            try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(baseIndex + "/" + node.name + ".html"))) {
                outFile.write(outString);
            }
            System.out.println(baseIndex + "/" + node.name + ".html");
        }
    }

    protected String FishBase(String genus, String species) {
        StringBuilder output = new StringBuilder();
        String species2 = null;
        if(species.contains("/")) {
            species2 = species.split("/")[1];
            species = species.split("/")[0];
        }
        String ref = "http://fishbase.us/Summary/speciesSummary.php?genusname=" + genus + "&amp;speciesname=" + species;
        String itis = "http://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=all&amp;search_kingdom=every&amp;search_span=exactly_for&amp;search_value=" + genus + "+" + species + "&amp;categories=All&amp;source=html&amp;search_credRating=All&amp;Go=Search";

        String ref2 = null;
        String itis2 = null;
        if(species2 != null) {
            ref2 = "http://fishbase.us/Summary/speciesSummary.php?genusname=" + genus + "&speciesname=" + species2;
            itis2 = "http://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=all&amp;search_kingdom=every&amp;search_span=exactly_for&amp;search_value=" + genus + "+" + species2 + "&amp;categories=All&amp;source=html&amp;search_credRating=All&amp;Go=Search";
        }

        output.append("<a target=\"_blank\" href=\"").append(ref).append("\">fishbase.org<img src=\"images/external.png\" alt=\"\" title=\"\" /></a> ");
        if(ref2 != null)
            output.append("<a target=\"_blank\" href=\"").append(ref2).append("\">fishbase.org<img src=\"images/external.png\" alt=\"\" title=\"\" /></a> ");
        output.append("<a target=\"_blank\" href=\"").append(itis).append("\">itis.gov<img src=\"images/external.png\" alt=\"\" title=\"\" /></a> ");
        if(itis2 != null)
            output.append("<a target=\"_blank\" href=\"").append(itis2).append("\">itis.gov<img src=\"images/external.png\" alt=\"\" title=\"\" /></a> ");

        return output.toString();


    }

    protected String SeaLifeBase(String genus, String species) {
        String ref = "http://sealifebase.org/Summary/speciesSummary.php?genusname=" + genus + "&amp;speciesname=" + species;
        String itis = "http://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=all&amp;search_kingdom=every&amp;search_span=exactly_for&amp;search_value=" + genus + "+" + species + "&amp;categories=All&amp;source=html&amp;search_credRating=All&amp;Go=Search";

        return "<a target=\"_blank\" href=\"" + ref + "\">sealifebase.org<img src=\"images/external.png\" alt=\"\" title=\"\" /></a> " +
                "<a target=\"_blank\" href=\"" + itis + "\">itis.gov<img src=\"images/external.png\" alt=\"\" title=\"\" /></a> ";
    }


    protected void genFishPixFile(genReef35.details node, int index, String baseIndex, int reefRef, String banner) throws IOException {
        numPhotos++;
        String outString = readFile("singlephoto.html");

        outString = processSelectedGuideMenu(outString, reefRef);

        String base = "";
        if(!baseIndex.equals(basepathIndexAll)) {
            base = "../";
        }
        outString = outString.replaceAll("__REEFREF__", Integer.toString(reefRef));
        outString = outString.replaceAll("__BANNER__", banner);
        outString = outString.replaceAll("__BASE__", base);
        if(isFieldEither(node.thumbList.get(index), 3, 4)) {
            outString = outString.replaceAll("__TITLE__", node.fishName + " - " + getField(node.thumbList.get(index), 3, 4) + " - " + node.sname + " - " + getField(node.thumbList.get(index), 1) + " - Photo " + getField(node.thumbList.get(index), 0));
        } else {
            outString = outString.replaceAll("__TITLE__", node.fishName + " - " + node.sname + " - " + getField(node.thumbList.get(index), 1) + " - Photo " + getField(node.thumbList.get(index), 0));
        }
        outString = outString.replaceAll("__REEF__", reefName[reefRef]);
        outString = outString.replaceAll("__PRENAME__", preReefName[reefRef]);

        if( ! getField(node.thumbList.get(index), 3).isEmpty()) {
            outString = outString.replaceAll("__NAME__", node.fishName + " - " + getField(node.thumbList.get(index), 3));
        } else {
            outString = outString.replaceAll("__NAME__", node.fishName);
        }
        outString = outString.replaceAll("__NAME2__", node.fishName);

        if(!node.sname.equals("")) {
            outString = outString.replaceAll("__SCINAME__", "<span class=\"details\">Scientific Name: </span><span class=\"sntitle\">" + node.sname + "</span>");
        } else {
            outString = outString.replaceAll("__SCINAME__", "");
        }

        String cat = node.cat;
        if(node.subCat != null) {
            cat = node.subCat;
        }
        outString = outString.replaceAll("__CAT__", cat);
        outString = outString.replaceAll("__MAINCAT__", node.cat);
        outString = outString.replaceAll("__DIST1__", node.distribution + " - " + node.aka);
        if(!node.distribution.equals("")) {
            outString = outString.replaceAll("__DIST__", "<span class=\"details\">Distribution: </span><span class=\"details2\">" + node.distribution + "</span>");
        } else {
            outString = outString.replaceAll("__DIST__", "");
        }
        if(!node.aka.equals("")) {
            outString = outString.replaceAll("__AKA__", "<span class=\"details\">Also known as: </span><span class=\"details2\">" + node.aka + "</span>");
        } else {
            outString = outString.replaceAll("__AKA__", "");
        }
        StringBuilder text = new StringBuilder();
        if(!node.size.equals("")) {
            text.append("<span class=\"details\">Size: </span><span class=\"details2\">");
            text.append(node.size);
            text.append("</span>&nbsp;&nbsp;");
        }
        outString = outString.replaceAll("__SIZE__", text.toString());
        text = new StringBuilder();
        if(!node.depth.equals("")) {
            text.append("<span class=\"details\">Depth: </span><span class=\"details2\">");
            text.append(node.depth);
            text.append("</span>");
        }
        outString = outString.replaceAll("__DEPTH__", text.toString());

        StringBuilder thumblist_before = new StringBuilder();
        StringBuilder thumblist_after = new StringBuilder();
        boolean before = true;
        for(int i = 0; i < node.thumbList.size(); i++) {
            StringBuilder thumblist = new StringBuilder();
            String thumb = node.name + getField(node.thumbList.get(i), 0);
            thumblist.append("<div class=\"infoimg\">");
            if(i == index) {
                thumblist.append("<img src=\"").append(base).append("../pix/thumb3/").append(thumb).append(".jpg\" />\n");
                before = false;
            } else {
                thumblist.append("<a href=\"").append(thumb).append(".html\"><img src=\"").append(base).append("../pix/thumb3/").append(thumb).append(".jpg\" /></a>\n");
            }
            thumblist.append("<div>");
            if(getField(node.thumbList.get(i), 3).isEmpty())
                thumblist.append("&nbsp;");
            else
                thumblist.append(getField(node.thumbList.get(i), 3));
            thumblist.append("</div>");
            thumblist.append("</div>");
            if(before)
                thumblist_before.append(thumblist);
            else
                thumblist_after.append(thumblist);
        }
        outString = outString.replaceAll("__THUMBS__", thumblist_after.append(thumblist_before).toString());


        String thumbimg = node.name + getField(node.thumbList.get(index), 0);
        StringBuilder output = new StringBuilder();
        output.append("<br /><img class=\"selframe\" src=\"").append(base).append("../pix/").append(thumbimg).append(".jpg\" alt=\"").append(node.fishName).append(" - ").append(node.sname).append("\" title=\"").append(node.fishName).append(" - ").append(node.sname).append("\" />\n");
        output.append("<div>");
        String thumbElem = node.thumbList.get(index);
        String div = "";
        if(!getField(thumbElem, 4).isEmpty()) {
            output.append(getField(thumbElem, 4));
            div = " / ";
        }
        if(!getField(thumbElem, 1).isEmpty()) {
            output.append(div).append("Location: ").append(getField(thumbElem, 1));
            div = " / ";
        }
        if(!getField(thumbElem, 2).isEmpty()) {
            output.append(div).append("Depth: ").append(getField(thumbElem, 2)).append(" feet");
        }
        output.append("</div><br />");

        outString = outString.replaceAll("__FISH_HTML__", output.toString());

        outString = outString.replaceAll("__INDEX__", "../" + node.name + ".html");

        if(analytics) {
            outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
        } else {
            outString = outString.replaceAll("__ANALYTICS__", "");
        }


        if(!compareToFile(outString, baseIndex + "/pixhtml/" + thumbimg + ".html")) {
            try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(baseIndex + "/pixhtml/" + thumbimg + ".html"))) {
                outFile.write(outString);
            }
            System.out.println(baseIndex + "/pixhtml/" + thumbimg + ".html");
        }

    }

    private int numInCat(group group, String cat) {
        int count1 = 0;
        for(int i = group.start; i < group.end; i++) {
            details node = detailsList.get(i);
            if(node.cat.equals(cat))
                count1++;
        }
        return count1;

    }

    private java.util.Map<String,String> ListCatInGroup(group group) {
        java.util.HashMap<String,String> ret = new java.util.HashMap<>();
        groupList.stream().filter((gr) -> (gr.name.equals(group.name))).forEach((gr) -> {
            for(int i = gr.start; i < gr.end; i++) {
                if(!ret.containsKey(detailsList.get(i).cat)) {
                    String link = detailsList.get(i).name;
                    ret.put(detailsList.get(i).cat, link);
                }
            }
        });
        return ret;
    }

    protected String processNote(String note) {
        StringBuilder ret = new StringBuilder();
        String[] lines = note.split("<br />");
        ret.append(lines[0]);
        for(int i = 1; i < lines.length; i++) {
            //ret.append("<br />&nbsp;&nbsp;");
            ret.append(lines[i]);
        }
        return ret.toString();

    }

    protected String processSelectedGuideMenu(String outString, int reefRef) {
        if(reefRef == 0)
            outString = outString.replaceAll("__CHECK_ALL__", "<div class=\"ui-icon ui-icon-check arrow\"></div>" + reefMenu[0]);
        else
            outString = outString.replaceAll("__CHECK_ALL__", "<div class=\"arrow2\">&nbsp;</div>" + reefMenu[0]);
        if(reefRef == 1)
            outString = outString.replaceAll("__CHECK_CAR__", "<div class=\"ui-icon ui-icon-check arrow\"></div>" + reefMenu[1]);
        else
            outString = outString.replaceAll("__CHECK_CAR__", "<div class=\"arrow2\">&nbsp;</div>" + reefMenu[1]);
        if(reefRef == 2)
            outString = outString.replaceAll("__CHECK_PAC__", "<div class=\"ui-icon ui-icon-check arrow\"></div>" + reefMenu[2]);
        else
            outString = outString.replaceAll("__CHECK_PAC__", "<div class=\"arrow2\">&nbsp;</div>" + reefMenu[2]);
        if(reefRef == 3)
            outString = outString.replaceAll("__CHECK_KEY__", "<div class=\"ui-icon ui-icon-check arrow\"></div>" + reefMenu[3]);
        else
            outString = outString.replaceAll("__CHECK_KEY__", "<div class=\"arrow2\">&nbsp;</div>" + reefMenu[3]);
        if(reefRef == 4)
            outString = outString.replaceAll("__CHECK_HAW__", "<div class=\"ui-icon ui-icon-check arrow\"></div>" + reefMenu[4]);
        else
            outString = outString.replaceAll("__CHECK_HAW__", "<div class=\"arrow2\">&nbsp;</div>" + reefMenu[4]);
        if(reefRef == 5)
            outString = outString.replaceAll("__CHECK_EPAC__", "<div class=\"ui-icon ui-icon-check arrow\"></div>" + reefMenu[5]);
        else
            outString = outString.replaceAll("__CHECK_EPAC__", "<div class=\"arrow2\">&nbsp;</div>" + reefMenu[5]);

        return outString;

    }


    protected void genFamilyIndex(java.util.Collection<details> fishList,
                                  String baseIndex,int reefRef,String header) throws IOException
    {
        genFamilyIndex(fishList, baseIndex, reefRef, header, SEC0);
        genFamilyIndex(fishList, baseIndex, reefRef, header, SEC1);
        genFamilyIndex(fishList, baseIndex, reefRef, header, SEC2);
        genFamilyIndex(fishList, baseIndex, reefRef, header, SEC3);
        genFamilyIndex(fishList, baseIndex, reefRef, header, SEC4);
    }

    protected void genFamilyIndex(Collection<details> fishList,
                                  String baseIndex,
                                  int reefRef,
                                  String header, String type) throws IOException {


        String preReefString = "";
        if(preReefName[reefRef].length() > 0) {
            preReefString = "<span class=\"pretitle\">" + preReefName[reefRef] + "</span>";
        }
        String base = "";

        if(!baseIndex.equals(basepathIndexAll)) {
            base = "../";
        }

        String title = "index";

        String outString;

        outString = readFile("index0.html");
        outString = processSelectedGuideMenu(outString, reefRef);

        //String treeMenu = buildTreeMenu(name, active);
        //outString = outString.replaceAll("__TREEMENU__", treeMenu);
        outString = outString.replaceAll("__REEF__", reefName[reefRef]);
        outString = outString.replaceAll("__PRENAME__", preReefString);
        outString = outString.replaceAll("__BASE__", base);
        outString = outString.replaceAll("__BANNER__", header);

        if(analytics) {
            outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
        } else {
            outString = outString.replaceAll("__ANALYTICS__", "");
        }

        StringBuilder html = new StringBuilder();

        for(int j = 0; j < typeList.length - 1; j++) {
            if(typeList[j].equals(type)) {
                html.append("<div class=\"buttonType1\">");
                html.append(typeList[j]).append("</div>");
            } else {
                html.append("<div class=\"buttonType\"><a  href=\"index_");
                html.append(typeList[j]).append(".html\">").append(typeList[j]).append("</a></div>");
            }
        }
        html.append("<br /><br />");

        String family="";
        category fam;
        Map<String, String> finalMap = new HashMap<>();
        List<String> finalList = new ArrayList<>();

        for(details node : fishList) {
            if( ! getSpeciesClass(node.cat).equals(type))
                continue;
            if(family.equals(node.cat)) {
                continue;
            } else {
                family = node.cat;
                fam = Family.get(family);
            }
            StringBuilder html_frag = new StringBuilder();
            String link = node.indexLink + "#" + node.cat.replaceAll(" ", "_");

//            if( ! speciesClass.equals(getSpeciesClass(node.cat))) {
//                if(!speciesClass.equals(""))
//                    html_frag.append("<br /><br />");
//                speciesClass = getSpeciesClass(node.cat);
//                html_frag.append("\n<div class=\"famheader\"> <span class=\"famheader\">");
//                html_frag.append(speciesClass);
//                html_frag.append("</span></div>\n");
//            }

            html_frag.append("<a href=\"").append(link).append("\">\n");
            html_frag.append("<div class=\"famInfo\">\n");
            html_frag.append("<p class=\"label\">").append(node.cat).append("</p>\n");
            String img;
            String img2 = "";
            String img3 = "";
            if( ! fam.img.equals(""))
                img = fam.img;
            else
                img = node.name + node.fishRef;
            if(fam.species.size() > 1)
                img2 = fam.species.get(1).name + fam.species.get(1).fishRef;
            if(fam.species.size() > 2)
                img3 = fam.species.get(2).name + fam.species.get(2).fishRef;
            html_frag.append("<img class=\"famPhoto\" alt=\"\" src=\"").append(base).append("pix/thumb/").append(img).append(".jpg\" alt=\"").append(node.fishName).append(" - ").append(node.sname).append("\" title=\"").append(node.fishName).append(" - ").append(node.sname).append("\" />");
            html_frag.append("\n<div class=\"famDetails\">\n");
            if(!img2.equals(""))
                html_frag.append("<img class=\"smallPhoto\" alt=\"\" src=\"").append("pix/thumb3/").append(img2).append(".jpg\" alt=\"").append(node.fishName).append(" - ").append(node.sname).append("\" title=\"").append(node.fishName).append(" - ").append(node.sname).append("\" />");
            if(!img3.equals(""))
                html_frag.append("<img class=\"smallPhoto\" alt=\"\" src=\"").append("pix/thumb3/").append(img3).append(".jpg\" alt=\"").append(node.fishName).append(" - ").append(node.sname).append("\" title=\"").append(node.fishName).append(" - ").append(node.sname).append("\" />");
            if( ! fam.catSname[0].equals(""))
                for(String catSname : fam.catSname) {
                    html_frag.append("<p class=\"label1\">").append(fam.catType).append(": <span class=\"label1\">").append(catSname).append("</span></p>\n");
                }
            if( ! fam.subSname[0].equals(""))
                for(String subSname : fam.subSname) {
                    html_frag.append("<p class=\"label1\">Subfamily: <span class=\"label1\">").append(subSname).append("</span></p>\n");
                }
            html_frag.append("<p class=\"label2\">").append(fam.count).append(" Species</p>\n");
            html_frag.append("</div></div>\n");
            html_frag.append("</a>\n");
            finalMap.put(node.cat, html_frag.toString());
            finalList.add(node.cat);

        }
        finalList.forEach((h) -> html.append(finalMap.get(h)));


        outString = outString.replaceAll("__INDEX_HTML__", html.toString());
        outString = outString.replaceAll("__TITLE__", title);
        String name = "index_" + type + ".html";
        boolean sameFile = compareToFile(outString, baseIndex + "/" + name);
        if(!sameFile) {
            try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(baseIndex + "/" + name))) {
                outFile.write(outString);
            }
            System.out.println(baseIndex + "/" + name);
        }


    }

    protected void genIndexFile(java.util.ArrayList<details> fishList,
                                String baseIndex,
                                int start, int end,
                                String name,
                                String nextname, String prevname,
                                String maxCol,
                                int reefRef,
                                String header) throws IOException {

        String preReefString = "";
        if(preReefName[reefRef].length() > 0) {
            preReefString = "<span class=\"pretitle\">" + preReefName[reefRef] + "</span>";
        }
        String base = "";
        //StringBuilder cat_count = new StringBuilder();
        int cat_count_num = 0;
        if(!baseIndex.equals(basepathIndexAll)) {
            base = "../";
        }
        String outString;

        outString = readFile("index_3.html");

        outString = processSelectedGuideMenu(outString, reefRef);

        StringBuilder img_reef = new StringBuilder();
        StringBuilder reef_name = new StringBuilder();
        StringBuilder sci_name = new StringBuilder();
        StringBuilder cat_reef = new StringBuilder();
        StringBuilder ref_reef = new StringBuilder();
        StringBuilder link_reef = new StringBuilder();
        StringBuilder title = new StringBuilder();
        String subdir = "";
        int extra = 0;
        StringBuilder simpleList = new StringBuilder();
        for(int i = start; i < end; i++) {
            details node = fishList.get(i);
            if(name.equals("index_all.html")) {
                simpleList.append("<img src=\"").append("pix/thumb3/").append(node.name).append(node.getFishRef(0)).append(".jpg\"").append("</img>");
                simpleList.append(node.fishName).append(" (").append(node.sname).append(")").append("\n");
                if((i % 1 ) == 0) {
                    simpleList.append("<br />");
                }
            }
            node.indexLink = name;
            int num = node.getCount();
            for(int j = 0; j < num; j++) {
                //String zxc = " (" + node.sname + ")";
                img_reef.append("\"").append(base).append("pix/thumb/").append(node.name).append(node.getFishRef(j)).append(".jpg\",");
                link_reef.append("\"").append(node.name).append(".html\",");
                //reef_name.append("\"").append(node.getName(j)).append(zxc).append("\",");
                reef_name.append("\"").append(node.getName(j)).append("\",");
                sci_name.append("\"").append(node.sname).append("\",");
                cat_count_num++;
            }

            if(!subdir.equals(node.cat)) {
                subdir = node.cat;
                cat_reef.append("\"").append(subdir).append("\",");
                ref_reef.append(i + extra - start).append(",");
//                if(i > start) {
//                    cat_count.append(cat_count_num).append("  ").append(subdir).append(": ");
//                } else {
//                    cat_count.append(subdir).append(": ");
//                }
                cat_count_num = 0;
            }

            extra += (num - 1);

        }
        //cat_count.append(cat_count_num + 1);
        ref_reef.append(extra + end - start);
        if(img_reef.length() > 0) {
            img_reef.deleteCharAt(img_reef.length() - 1);
            link_reef.deleteCharAt(link_reef.length() - 1);
            reef_name.deleteCharAt(reef_name.length() - 1);
            sci_name.deleteCharAt(sci_name.length() - 1);
            cat_reef.deleteCharAt(cat_reef.length() - 1);
        }
        //ref_reef.deleteCharAt(ref_reef.length() - 1);
        if(ref_reef.toString().equals("0")) {
            ref_reef.append(",").append(end - start);
        }
        outString = outString.replaceAll("__REEFREF__", Integer.toString(reefRef));
        outString = outString.replaceAll("__IMG_REEF__", img_reef.toString());
        outString = outString.replaceAll("__LINK_REEF__", link_reef.toString());
        outString = outString.replaceAll("__NAME_REEF__", reef_name.toString());
        outString = outString.replaceAll("__NAME_SCI__", sci_name.toString());
        outString = outString.replaceAll("__CAT_REEF_", cat_reef.toString());
        outString = outString.replaceAll("__REF_REEF__", ref_reef.toString());
        outString = outString.replaceAll("__MAX_COL__", maxCol);
        //outString = outString.replaceAll("__PREV__", prev);
        outString = outString.replaceAll("__PREVNAME__", prevname);
        //outString = outString.replaceAll("__PREVIMG__", previmg);
        //outString = outString.replaceAll("__NEXT__", next);
        outString = outString.replaceAll("__NEXTNAME__", nextname);
        //outString = outString.replaceAll("__NEXTIMG__", nextimg);
        outString = outString.replaceAll("__REEF__", reefName[reefRef]);
        outString = outString.replaceAll("__PRENAME__", preReefString);
        outString = outString.replaceAll("__BASE__", base);
        outString = outString.replaceAll("__BANNER__", header);
        if(analytics) {
            outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
        } else {
            outString = outString.replaceAll("__ANALYTICS__", "");
        }

        int[] active = new int[1];
        active[0] = -1;
        String treeMenu = buildTreeMenu(name, active);
        outString = outString.replaceAll("__TREEMENU__", treeMenu);

        StringBuilder html = new StringBuilder();
        html.append("<tbody>");
        subdir = "";
        int col = 0;
        for(int i = start; i < end; i++) {
            details node = fishList.get(i);
            if(!subdir.equals(node.cat)) {
                if(!subdir.equals("")) {
                    html.append("</tr></table>");
                }
                subdir = node.cat;
                html.append("<tr><td><div class=\"catheader\"><a href=\"").append(subdir.replaceAll(" ", "_")).append(".html\">").append(subdir).append("</a></div></td></tr>\n");
                html.append("<tr><td><table><tr>");
                col = 0;
//                if(!expandMenu) {
                title.append(" - ").append(subdir);
//                }
            }
            if((col != 0) && ((col % 3) == 0)) {
                html.append("</tr></table><table><tr>");
            }
            html.append("<td><img src=\"").append(base).append("pix/thumb/").append(node.name).append(node.fishRef).append(".jpg\" alt=\"").append(node.fishName).append(" - ").append(node.sname).append("\" title=\"").append(node.fishName).append(" - ").append(node.sname).append("\" />\n");
            html.append("<br /><div class=\"nameid\"><a href=\"").append(node.name).append(".html\">").append(node.fishName).append("</a></div></td>");
            col++;
        }
        html.append("</tr></table></td></tr></tbody>\n");
//        if(expandMenu) {
//            if(name.equals("latest.html")) {
//                title.append(" - Latest Updates");
//            }
//            if(name.equals("index_all.html")) {
//                title.append(" - List All Species");
//            }
//        }
        outString = outString.replaceAll("__INDEX_HTML__", html.toString());
        outString = outString.replaceAll("__TITLE__", title.toString());

        outString = outString.replaceAll("__ACTIVE__", Integer.toString(active[0]));


        if(name.equals("index_all.html") && baseIndex.endsWith("/clean")) {
            try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(baseIndex + "/simplelist.html"))) {
                outFile.write(simpleList.toString());
            }
        }

        boolean sameFile = compareToFile(outString, baseIndex + "/" + name);
        if(!sameFile) {
            try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(baseIndex + "/" + name))) {
                outFile.write(outString);
            }
            System.out.println(baseIndex + "/" + name);
        }

        //System.out.println(name + ": " + count + " --> " + cat_count);


    }

    protected void genCatalogFiles(java.util.ArrayList<details> detailsList,
                                   String baseIndex, int reefRef, String header) throws IOException {

        TreeMap<String, details> catName = new TreeMap<>();
        TreeMap<String, details> sciName = new TreeMap<>();
        TreeMap<String, TreeMap<String, details>> grpName = new TreeMap<>();

        String base = "";
        if(!baseIndex.equals(basepathIndexAll)) {
            base = "../";
        }

        String preReefString = "";
        if(preReefName[reefRef].length() > 0) {
            preReefString = "<span class=\"pretitle\">" + preReefName[reefRef] + "</span>";
        }

        String outString = readFile("index_catalog.html");

        outString = processSelectedGuideMenu(outString, reefRef);
        outString = outString.replaceAll("__BANNER__", header);

        detailsList.forEach(node -> {
            catName.put(node.fishName, node);
            if(!node.sname.equals("")) {
                sciName.put(node.sname, node);
            }
            if(!node.aka.equals("")) {
                String[] akas = node.aka.split(",");
                for(String aka : akas) {
                    catName.put(aka.trim() + " [" + node.fishName + "]", node);
                }
            }
            if(!grpName.containsKey(node.cat)) {
                grpName.put(node.cat, new TreeMap<>());
            }
            grpName.get(node.cat).put(node.fishName, node);
        });

        StringBuilder html = new StringBuilder();
        char alpha = '.';
        int split = catName.size() / 2;
        for(String name : catName.keySet()) {
            details elem = catName.get(name);
            if(name.charAt(0) != alpha) {
                if(split <= 0) {
                    html.append("\n</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>\n");
                    split = catName.size() / 1;
                }
                alpha = name.charAt(0);
                html.append("<div class=\"bigalpha\">").append(alpha).append("</div>");
            }
            html.append("<a class=\"tocname\" href=\"").append(elem.name).append(".html\">").append(name).append("</a><br />");
            split--;
        }
        outString = outString.replaceAll("__HTML__", html.toString());
        outString = outString.replaceAll("__BASE__", base);
        outString = outString.replaceAll("__REEF__", reefName[reefRef]);
        outString = outString.replaceAll("__PRENAME__", preReefString);
        outString = outString.replaceAll("__TITLE__", " - Index of Species by Common Names");
        if(analytics) {
            outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
        } else {
            outString = outString.replaceAll("__ANALYTICS__", "");
        }

        outString = outString.replaceAll("__LINKS__", "By Common Name | <a class=\"catalog\" href=\"cat_grp.html\">By Category</a> | <a class=\"catalog\" href=\"cat_sci.html\">By Scientific Names</a>");

        boolean sameFile = compareToFile(outString, baseIndex + "/cat.html");
        if(!sameFile) {
            try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(baseIndex + "/cat.html"))) {
                outFile.write(outString);
            }
            System.out.println(baseIndex + "/cat.html");
        }


        outString = readFile("index_catalog.html");
        outString = processSelectedGuideMenu(outString, reefRef);
        outString = outString.replaceAll("__BANNER__", header);
        html = new StringBuilder();
        alpha = '.';
        String first = "";
        split = sciName.size() / 3 - 0;
        for(details elem : sciName.values()) {
            if(elem.sname.charAt(0) != alpha) {
                if(split <= 0) {
                    html.append("\n</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>\n");
                    split = sciName.size() / 3 - 0;
                }
                alpha = elem.sname.charAt(0);
                html.append("<div class=\"bigalpha\">").append(alpha).append("</div>");
            }
            if(elem.sname.split(" ")[0].equals(first)) {
                html.append("<a class=\"tocnamesci\" href=\"").append(elem.name).append(".html\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").append(elem.sname.split(" ")[1]);
                if(elem.sname.split(" ").length > 2)
                    html.append(" ").append(elem.sname.split(" ")[2]);
                html.append("</a><br />");
            } else {
                html.append("<a class=\"tocnamesci\" href=\"").append(elem.name).append(".html\">").append(elem.sname).append("</a><br />");
                first = elem.sname.split(" ")[0];
            }
            split--;
        }
        outString = outString.replaceAll("__HTML__", html.toString());
        outString = outString.replaceAll("__BASE__", base);
        outString = outString.replaceAll("__REEF__", reefName[reefRef]);
        outString = outString.replaceAll("__PRENAME__", preReefString);
        outString = outString.replaceAll("__TITLE__", " - Index of Species by Scientific Names");
        if(analytics) {
            outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
        } else {
            outString = outString.replaceAll("__ANALYTICS__", "");
        }
        outString = outString.replaceAll("__LINKS__", "<a class=\"catalog\" href=\"cat.html\">By Common Name</a> | <a class=\"catalog\" href=\"cat_grp.html\">By Category</a> | By Scientific Names");

        sameFile = compareToFile(outString, baseIndex + "/cat_sci.html");
        if(!sameFile) {
            try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(baseIndex + "/cat_sci.html"))) {
                outFile.write(outString);
            }
            System.out.println(baseIndex + "/cat_sci.html");
        }


        outString = readFile("index_catalog.html");
        outString = processSelectedGuideMenu(outString, reefRef);
        outString = outString.replaceAll("__BANNER__", header);
        html = new StringBuilder();
        String curgrp = "";
        split = detailsList.size() / 3;
        for(String elem : grpName.keySet()) {
            if(!elem.equals(curgrp)) {
                if(split <= 0) {
                    html.append("\n</td><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>\n");
                    split = detailsList.size() / 3;
                }
                curgrp = elem;
                html.append("<div class=\"biggrp\">").append(curgrp).append("</div>");
            }
            TreeMap<String, details> nameTree = grpName.get(elem);
            for(details node : nameTree.values()) {
                split--;
                html.append("<a class=\"tocnamegrp\" href=\"").append(node.name).append(".html\">").append(node.fishName).append("</a><br />");
            }

        }
        outString = outString.replaceAll("__HTML__", html.toString());
        outString = outString.replaceAll("__REEF__", reefName[reefRef]);
        outString = outString.replaceAll("__PRENAME__", preReefString);
        outString = outString.replaceAll("__BASE__", base);
        outString = outString.replaceAll("__TITLE__", " - Index of Species by Categories");
        if(analytics) {
            outString = outString.replaceAll("__ANALYTICS__", readFile("analytics.xml"));
        } else {
            outString = outString.replaceAll("__ANALYTICS__", "");
        }

        outString = outString.replaceAll("__LINKS__", "<a class=\"catalog\" href=\"cat.html\">By Common Name</a> |By Category | <a class=\"catalog\" href=\"cat_sci.html\">By Scientific Names</a>");

        sameFile = compareToFile(outString, baseIndex + "/cat_grp.html");
        if(!sameFile) {
            try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(baseIndex + "/cat_grp.html"))) {
                outFile.write(outString);
            }
            System.out.println(baseIndex + "/cat_grp.html");
        }

    }




    static final String SEC0 = "Fish";
    static final String SEC1 = "Invertebrates";
    static final String SEC2 = "Sponges";
    static final String SEC3 = "Corals";
    static final String SEC4 = "Algae";
    static final String SEC5 = "Marine Reptiles &amp; Mammals";
    static final String[] typeList = new String[] {SEC0, SEC1, SEC2, SEC3, SEC4, SEC5 };

    private String getSpeciesClass(String cat) {
        if(catSpecies.get(cat) == null) {
            System.out.println(cat);
            System.exit(0);
        }
        String type = catSpecies.get(cat).replace("_", "");
        if(type.equals("Fish")) {
            return genReef35.SEC0;
        }
        if(type.equals("Creature")) {
            return SEC1;
        }
        if(type.equals("Sponge")) {
            return SEC2;
        }
        if(type.equals("Coral")) {
            return SEC3;
        }
        if(type.equals("Algae")) {
            return SEC4;
        }
        if(type.equals("Mammals")) {
            return SEC5;
        }

        return "";

    }

    private boolean isSingleList(String speciesClass) {
        if(speciesClass.equals(genReef35.SEC2)) {
            return true;
        }
        if(speciesClass.equals(genReef35.SEC3)) {
            return true;
        }
        if(speciesClass.equals(genReef35.SEC4)) {
            return true;
        }
        return speciesClass.equals(genReef35.SEC5);
    }

    private String buildTreeMenu(String name, int[] activeSel) {

        StringBuilder str = new StringBuilder();

        String speciesClass = "";
        boolean singleClass = false;
        int ul_fam_open_counter = 0;
        int ul_fam_open = 0;
        int active_count = -1;

        for(group elem : groupList) {

            if(elem.page == 1) {
                if(elem.start != 0) {
                    str.append("</ul>");
                    str.append("</li>");
                }
            }

            if(!getSpeciesClass(detailsList.get(elem.start).cat).equals(speciesClass)) {
                // New Family Header
                if(!speciesClass.equals("")) {
                    str.append("</ul></div>\n");
                }
                speciesClass = getSpeciesClass(detailsList.get(elem.start).cat);
                singleClass = isSingleList(speciesClass);
                str.append("<h3><a>").append(speciesClass).append("</a></h3><div><ul class=\"menusec1\">");
                active_count++;
            }


            if(elem.page == 1) {
                // New family
                if(singleClass) {
                    str.append("<li>");
                    str.append("<ul class=\"menusecopen single\">");
                    ul_fam_open_counter++;
                } else {
                    ul_fam_open_counter++;
                    str.append("<li><a>").append(elem.name).append("</a>");
                    str.append("_ULFAMOPEN_").append(ul_fam_open_counter).append("_");

                }

            }
            boolean active = false;
            if(name.equals("index" + elem.index + ".html")) {
                ul_fam_open = ul_fam_open_counter;
                active = true;
                activeSel[0] = active_count;
            }

            String prev = "";
//            if(elem.page == 1) {
//                str.append("<div class=\"submenu\" style=\"border-width: 0px;\">\n");
//            } else {
//                str.append("<div class=\"submenu\">\n");
//            }
            if(active) {
                //str.append("<a  href=\"index").append(elem.index).append(".html\"></a>");
                //str.append("<div id=\"selactive\">\n");
            } else {
                str.append("<li><a  href=\"index").append(elem.index).append(".html\"><ul>");
            }
            for(int j = elem.start; j < elem.end; j++) {

                if(!prev.equals(detailsList.get(j).cat)) {
                    prev = detailsList.get(j).cat;
                    if(active)
                        str.append("<li class=\"selactive\">").append(prev).append("</li>");
                    else
                        str.append("<li>").append(prev).append("</li>");
                }
            }
            if(active) {
                //str.append("</div>\n");
            } else {
                str.append("</ul></a></li>\n");
            }
            //str.append("</div>\n");

        }
        str.append("</ul>");
        str.append("</li>");
        str.append("</ul></div>\n"); //str.append("</ul>\n</div>\n");

        String ret = str.toString().replaceAll("_ULFAMOPEN_" + ul_fam_open + "_", "<ul class=\"menusecopen\">");
        ret = ret.replaceAll("_ULFAMOPEN_.*_", "<ul>");

        return ret;
    }


    protected String readFile(String name) throws IOException {
        byte[] b;
        try (java.io.InputStream fis = getClass().getResourceAsStream(name)) {
            b = new byte[fis.available()];
            if(fis.read(b) != b.length)
                throw new IOException();
        }
        return new String(b);
    }

    static protected boolean compareToFile(String fileString, String fileName) {

        int c1;
        int i = 0;
        try (java.io.BufferedInputStream bis = new java.io.BufferedInputStream(new FileInputStream(fileName))) {

            while ((c1 = bis.read()) != -1) {
                if (fileString.length() == i) {
                    return false;
                }
                int c2 = fileString.codePointAt(i++);
                if (c1 != c2) {
                    return false;
                }
            }

            return fileString.length() == i;

        } catch (IOException exp) {
            return false;
        }
    }

    static String configpath = "/home/fc/web/reef3";

    public static void main(String[] args) {

        genReef35 reef = new genReef35();
        reef.basepathIndexAll = "/home/fc/web/reef3";
        if(args.length == 1) {
            reef.basepathIndexAll = args[0];
            configpath = args[0];
        }
        //String captions = reef.buildCaptionFile("/home/fc/web/reef3");
        try {
            reef.buildCatSpecies(reef.basepathIndexAll + "/config/cat.csv");

//            if(genReef3.compareToFile(captions, reef.basepathIndexAll + "/pix/captions") == false) {
//                try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(reef.basepathIndexAll + "/pix/captions"))) {
//                    outFile.write(captions);
//                }
//                System.out.println(reef.basepathIndexAll + "/pix/captions");
//            }
        } catch(IOException ex) {
            Logger.getLogger(genReef35.class.getName()).log(Level.SEVERE, null, ex);
        }

        reef.createSite(genReef35.configpath + "/clean", false);
        reef.createSite(genReef35.configpath, true);
        //reef.createSite(genReef3.configpath, false);
    }

    protected String getField(String str, int index) {
        String[] lst = str.split(":");
        if(lst.length <= index) {
            return "";
        } else {
            return lst[index];
        }
    }

    private String getField(String str, int index, int index2) {
        String[] lst = str.split(":");
        if(lst.length <= index) {
            return "";
        } else {
            if(lst.length > index2) {
                return lst[index] + " " + lst[index2];
            }
        }
        return lst[index];
    }

    private Boolean isFieldEither(String str, int index, int index2) {
        String[] lst = str.split(":");
        if(lst.length <= index) {
            return false;
        }
        if(lst.length <= index2) {
            if(lst[index].equals("")) {
                return false;
            }
        }
        return !lst[index].equals("") || !lst[index2].equals("");
    }

    private void copyFile(String source, String dest) throws IOException {
        FileChannel in = null, out = null;
        try {
            in = new FileInputStream(source).getChannel();
            out = new FileOutputStream(dest).getChannel();

            long size = in.size();
            MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);

            out.write(buf);

        } finally {
            if(in != null) {
                in.close();
            }
            if(out != null) {
                out.close();
            }
        }
    }

    @SuppressWarnings("unused")
    protected List<String> getDistributionFromJSON() {

        List<String> ret = new ArrayList<>();

        detailsList.stream().map((elem) -> Arrays.asList(elem.distributionRaw.split(","))).flatMap(Collection::stream)
                .map(String::trim).filter((d) -> (!ret.contains(d))).forEach(ret::add);
        return ret;
    }

    protected String getDistribution(String list) {
        StringBuilder dist = new StringBuilder();
        String[] code = list.split(",");
        for(String code1 : code) {
            if(code1.equals("1")) {
                dist.append("Caribbean, ");
            } else if(code1.equals("2")) {
                dist.append("Bahamas, ");
            } else if(code1.equals("3")) {
                dist.append("Florida, ");
            } else if(code1.equals("4")) {
                dist.append("Bermuda, ");
            } else if(code1.equals("5")) {
                dist.append("Cozumel, ");
            } else if(code1.equals("6")) {
                dist.append("Eastern Caribbean, ");
            } else if(code1.equals("7")) {
                dist.append("Gulf of Mexico, ");
            } else if(code1.equals("8")) {
                dist.append("Southern Caribbean, ");
            } else if(code1.equals("9")) {
                dist.append("Indo-Pacific, ");
            } else if(code1.equals("10")) {
                dist.append("Hawaii, ");
            } else if(code1.equals("11")) {
                dist.append("Bay Islands (Honduras), ");
            } else if(code1.equals("12")) {
                dist.append("Western Caribbean, ");
            } else if(code1.equals("13")) {
                dist.append("Circumtropical, ");
            } else if(code1.equals("14")) {
                dist.append("Northwest Caribbean, ");
            } else if(code1.equals("15")) {
                dist.append("Warm and temperate seas worldwide, ");
            } else if(code1.equals("16")) {
                dist.append("Belize, ");
            } else if(code1.equals("17")) {
                dist.append("Venezuela, ");
            } else if(code1.equals("18")) {
                dist.append("Worldwide, ");
            } else if(code1.equals("19")) {
                dist.append("Cayman Islands, ");
            } else if(code1.equals("20")) {
                dist.append("Red Sea, ");
            } else if(code1.equals("21")) {
                dist.append("Eastern Indo-Pacific, ");
            } else if(code1.equals("22")) {
                dist.append("Nothern Pacific, ");
            } else if(code1.equals("23")) {
                dist.append("Polynesia, ");
            } else if(code1.equals("24")) {
                dist.append("Northwest Pacific, ");
            } else if(code1.equals("25")) {
                dist.append("Pacific, ");
            } else if(code1.equals("26")) {
                dist.append("Indo-West Pacific, ");
            } else if(code1.equals("27")) {
                dist.append("Central Pacific, ");
            } else if(code1.equals("28")) {
                dist.append("East Pacific, ");
            } else if(code1.equals("29")) {
                dist.append("Gulf of California, ");
            } else if(code1.equals("30")) {
                dist.append("Pacific Coast of Mexico to Panama, ");
            } else if(code1.equals("31")) {
                dist.append("Pacific Coast of Costa Rica to Ecuador, ");
            } else if(code1.equals("32")) {
                dist.append("Micronesia, ");
            } else if(code1.equals("33")) {
                dist.append("Florida Keys, ");
            } else if(code1.equals("34")) {
                dist.append("South Florida, ");
            } else if(code1.equals("35")) {
                dist.append("Pacific Coast of Mexico to Peru, ");
            } else if(code1.equals("36")) {
                dist.append("Brazil, ");
            } else if(code1.equals("37")) {
                dist.append("Indonesia, ");
            } else if(code1.equals("38")) {
                dist.append("Great Barrier Reef, ");
            } else if(code1.equals("39")) {
                dist.append("West Pacific, ");
            } else if(code1.equals("40")) {
                dist.append("Australia, ");
            } else if(code1.equals("41")) {
                dist.append("Philippines, ");
            } else if(code1.equals("42")) {
                dist.append("Fiji, ");
            } else if(code1.equals("43")) {
                dist.append("Asian Pacific, ");
            } else if(code1.equals("E")) {
                if(dist.length() > 2) {
                    dist.delete(dist.length() - 2, dist.length());
                }
                dist.append(" (Endemic), ");
            } else if(code1.length() > 3) {
                dist.append(code1).append(", ");
            } else {
                throw new RuntimeException("Unknow distribution: " + list);
            }
        }
        return dist.toString().substring(0, dist.length() - 2);
    }




    private String getUnknowSpecies(String config) throws IOException {
        StringBuilder str = new StringBuilder();

        java.io.BufferedReader file = new java.io.BufferedReader(new java.io.FileReader(config));
        String line;
        while((line = file.readLine()) != null) {
            String[] field = line.split(":");
            String img = field[0];
            String cat = field[1];
            String loc = field[2];
            String depth  = "";
            String size = "";
            String comment = "";
            if(field.length > 3)
                depth = field[3];
            if(field.length > 4)
                size = field[4];
            if(field.length > 5)
                comment = field[5];
            String id = img.replace("IMG_", "").replace(".JPG", "");
            str.append("<div class=\"tblock\"><div class =\"utitle\"><span>").append(cat).append(" #").append(id).append("</span>&nbsp;");
            str.append("<a href=\"mailto:id@reefguide.org?subject=ID for species ").append(cat).append("#").append(id).append("\"><img style=\"vertical-align: middle;\" title=\"\" alt=\"\" src=\"unknown/Mail-icon.png\" /></a></div>");
            str.append("<div class=\"pix\"><a class=\"single_image\" href=\"unknown/").append(img).append("\"><img title=\"\" alt=\"\" src=\"unknown/thumb2/").append(img).append("\" /></a></div>");
            str.append("<div class=\"comment\">Location: ").append(loc).append("</div>");
            if(depth.length() > 0)
                str.append("<div class=\"comment\">Depth: ").append(depth).append(" ft.</div>");
            if(size.length() > 0)
                str.append("<div class=\"comment\">Size: ").append(size).append("</div>");
            if(comment.length() > 0)
                str.append("<div class=\"comment\">").append(comment).append("</div>");
            if(depth.length() == 0)
                str.append("<div class=\"comment\">&nbsp</div>\n");
            if(size.length() == 0)
                str.append("<div class=\"comment\">&nbsp</div>\n");
            if(comment.length() == 0)
                str.append("<div class=\"comment\">&nbsp</div>\n");
            str.append("</div>\n");

        }


        return str.toString();
    }

}
