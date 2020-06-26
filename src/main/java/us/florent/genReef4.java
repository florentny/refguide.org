package us.florent;

public class genReef4 extends genReef35{

    public static void main(String[] args) {

        genReef35 reef = new genReef35();
        reef.basepathIndexAll = "/home/fc/web/reef4";
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
        reef.createSite(genReef4.configpath, true);
        //reef.createSite(genReef4.configpath, false);
    }

}
