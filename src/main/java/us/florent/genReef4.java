package us.florent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

public class genReef4 extends genReef35{

    //static String configpath = "/data3/newreef";

    public static void main(String[] args) {
        genReef4 reef = new genReef4();
        configpath = "/data3/newreef";
        reef.basepathIndexAll = "/data3/newreef";
        if(args.length == 1) {
            reef.basepathIndexAll = args[0];
            configpath = args[0];
        }
        //String captions = reef.buildCaptionFile("/home/fc/web/reef3");
//        try {
//            reef.buildCatSpecies(reef.basepathIndexAll + "/config/cat.csv");

//            if(genReef3.compareToFile(captions, reef.basepathIndexAll + "/pix/captions") == false) {
//                try (java.io.BufferedWriter outFile = new java.io.BufferedWriter(new java.io.FileWriter(reef.basepathIndexAll + "/pix/captions"))) {
//                    outFile.write(captions);
//                }
//                System.out.println(reef.basepathIndexAll + "/pix/captions");
//            }
//        } catch(IOException ex) {
//            Logger.getLogger(genReef35.class.getName()).log(Level.SEVERE, null, ex);
//        }

        //reef.createSite(genReef4.configpath + "/clean", false);
        reef.createSite(genReef4.configpath, false);
        //reef.createSite(genReef4.configpath, false);
    }

    protected int process(String configFile,
                          String baseIndex,
                          int reefRef,
                          String[] headers) {

        int headercount = 0;
        numPhotos = 0;
        buildAllData(configFile, reefRef);
        try {
            genCatalogFiles(species_collection.getAllSpecies(), baseIndex, reefRef, headers[0]);
            for(var g : pageList) {
                genIndexFile(baseIndex, g, reefRef, headers[headercount]);
                if(++headercount == headers.length) {
                    headercount = 0;
                }
                headercount = 0;
                for(Species sp : g.species) {
                    genFishFile(sp, baseIndex, reefRef, headers[headercount], g);
                    if(++headercount == headers.length) {
                        headercount = 0;
                    }
                }
            }
            genFamilyIndex(baseIndex, reefRef, headers[0]);

            for(var cat : genus_classification.getAllCat()) {
                var z = species_collection.getSpeciesFromCat(cat);
                genCatFile(z, baseIndex, reefRef, headers[0], cat);

            }

            String latestline;
            String date;
            var latest_list = species_collection.getLatest();
            var latestGroup = new page();
            String pattern = "dd MMM YYYY HH:mm Z";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            for(var sp : latest_list) {
                if(sp == null)
                    continue;
                if(!checkRegion(reefRef, sp.dist()))
                    continue;
                if(latestGroup.species.size() > 100)
                    continue;
                date = simpleDateFormat.format(sp.update());
                latestGroup.species.add(sp);
                latestGroup.dates.add(date);

            }
            latestGroup.index = -1;
            genIndexFile(baseIndex, latestGroup, reefRef, headers[0]);
            //genRSS(latestGroup, baseIndex);
            //updateMongo(reefRef, baseIndex);
            //copyFile(baseIndex + "/index1.html", baseIndex + "/index.html");

        } catch(IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("Number of species: " + species_collection.getAllSpecies().size());
        return species_collection.getAllSpecies().size();
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
            int all = process("/config/reeflist5.xml", basepathIndexAll, 0, hearderAll);
            int all_pic = numPhotos;
//            System.out.println("Processing Caribbean:");
//            int carib = process("/config/reeflistcarib4.xml", basepathIndexCarib, 1, hearderCarib);
//            int carib_pic = numPhotos;
//            System.out.println("Processing Indo-Pacific:");
//            int indopac = process("/config/reeflist4.xml", basepathIndexIndoPac, 2,  hearderIndopac);
//            int indopac_pic = numPhotos;
//            System.out.println("Processing Florida Keys:");
//            int keys = process("/config/reeflistcarib4.xml", basepathIndexKeys, 3, hearderKeys);
//            int key_pics = numPhotos;
//            System.out.println("Processing Hawaii:");
//            int hawaii = process("/config/reeflisthawaii4.xml", basepathIndexHawaii, 4, hearderHawaii);
//            int hawaii_pics = numPhotos;
//            System.out.println("Processing Baja:");
//            int baja = process("/config/reeflistbaja4.xml", basepathIndexBaja, 5, hearderHawaii);
//            int baja_pics = numPhotos;

            String outString = readFile("about.html");
            outString = outString.replace("__ALL__", Integer.toString(all));
            outString = outString.replace("__ALLPIC__", Integer.toString(all_pic));
//            outString = outString.replace("__CARIB__", Integer.toString(carib));
//            outString = outString.replace("__CARIBPIC__", Integer.toString(carib_pic));
//            outString = outString.replace("__INDOPAC__", Integer.toString(indopac));
//            outString = outString.replace("__INDOPACPIC__", Integer.toString(indopac_pic));
//            outString = outString.replace("__KEYS__", Integer.toString(keys));
//            outString = outString.replace("__KEYSPIC__", Integer.toString(key_pics));
//            outString = outString.replace("__HAWAII__", Integer.toString(hawaii));
//            outString = outString.replace("__HAWAIIPIC__", Integer.toString(hawaii_pics));
//            outString = outString.replace("__BAJA__", Integer.toString(baja));
//            outString = outString.replace("__BAJAPIC__", Integer.toString(baja_pics));

//            if(analytics) {
//                outString = outString.replace("__ANALYTICS__", readFile("analytics.xml"));
//            } else {
//                outString = outString.replace("__ANALYTICS__", "");
//            }
//
////            writeToFile(outString, basepathIndexAll + "/about.html");
//
//            outString = readFile("home.html");
//            outString = outString.replace("__ALL__", Integer.toString(all));
////            outString = outString.replace("__CARIB__", Integer.toString(carib));
////            outString = outString.replace("__INDOPAC__", Integer.toString(indopac));
////            outString = outString.replace("__KEYS__", Integer.toString(keys));
////            outString = outString.replace("__HAWAII__", Integer.toString(hawaii));
//
//            if(analytics) {
//                outString = outString.replace("__ANALYTICS__", readFile("analytics.xml"));
//            } else {
//                outString = outString.replace("__ANALYTICS__", "");
//            }
//
//            writeToFile(outString, basepathIndexAll + "/home.html");
//
//            outString = readFile("search.html");
//            if(analytics) {
//                outString = outString.replace("__ANALYTICS__", readFile("analytics.xml"));
//            } else {
//                outString = outString.replace("__ANALYTICS__", "");
//            }
//
//            writeToFile(outString, basepathIndexAll + "/search.html");
//
//            outString = readFile("unknow.html");
//            //outString = outString.replace("__MAIN__", getUnknowSpecies(configpath + "/config/unknow.txt"));
//
//            if(analytics) {
//                outString = outString.replace("__ANALYTICS__", readFile("analytics.xml"));
//            } else {
//                outString = outString.replace("__ANALYTICS__", "");
//            }
//
//            writeToFile(outString, basepathIndexAll + "/unknow.html");

        } catch(IOException ex) {
            java.util.logging.Logger.getLogger(genReef35.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
