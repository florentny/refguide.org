package us.florent;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class exportToReef4 extends genReef35 {

    public static void main(String[] args) {
        exportToReef4 reef = new exportToReef4();
        try {
            reef.genExportFiles();
        } catch (Exception e) {
            System.out.println("Ooops....");
        }
    }

    private void genExportFiles() throws Exception {

        String[] configFile = {"/config/reeflist.xml"};
        try {
            this.buildCatSpecies(genReef35.configpath + "/config/cat.csv");
        } catch (IOException ex) {
            Logger.getLogger(exportToReef4.class.getName()).log(Level.SEVERE, null, ex);
        }
        buildAllData(configFile[0]);

        Map<String, List<String>> family = new HashMap<>();

        for (details elem : detailsList) {
            //System.out.println(elem.name + " - " + elem.sname + " - " + elem.family);
            category fam = Family.get(elem.cat);
            String genus = elem.sname.split(" ")[0];
            if(genus.isBlank()) {
                System.out.println(elem.name);
                genus = elem.name;
            }
            String fam_sname;
            if (!elem.family.isBlank())
                fam_sname = elem.family;
            else if(fam.isFamily())
                fam_sname = fam.catSname[0];
            else
                fam_sname = "";
            if (!family.containsKey(genus)) {
                family.put(genus, new ArrayList<>());
                family.get(genus).add(0, fam_sname);
            }  else if (!family.get(genus).contains(fam_sname) && !fam_sname.isBlank()) {
                if (family.get(genus).get(0).isBlank()) {
                    family.get(genus).remove(0);
                    family.get(genus).add(0, fam_sname);
                } else if(!fam_sname.contains("/"))
                    throw new Exception();
            }

            if(family.get(genus).get(0).contains("/")) {
                String[] s = family.get(genus).get(0).split("/");
                family.get(genus).remove(0);
                family.get(genus).add(0, s[0]);
                family.get(genus).add(1, s[1]);
            }
            else {
                if (!fam.subSname[0].isBlank()) {
                    //System.out.println(" ===> " + fam.subSname[0]);
                    if (!family.get(genus).contains(fam.subSname[0]))
                        family.get(genus).add(1, fam.subSname[0]);
                }
                if (family.get(genus).size() == 1)
                    family.get(genus).add(1, "");
            }


            if (family.get(genus).size() == 2) {
                if (elem.subCat != null) {
                    family.get(genus).add(2, elem.subCat);
                }
                else {
                    family.get(genus).add(2, elem.cat);
                }
            } else if (elem.subCat != null && !elem.subCat.equals(elem.cat)) {
                System.out.println(" !!!!! " + genus + " - " + elem.subCat + "/" + elem.cat);
            }

            if (family.get(genus).size() == 3) {
                if(!fam.catType.equals("Family")) {
                    family.get(genus).add(3, fam.catType);
                    family.get(genus).add(4, fam.catSname[0]);
                } else {
                    family.get(genus).add(3, "");
                    family.get(genus).add(4, "");
                }

            }

        }
        System.out.println("====================");
        System.out.println("genus,family,subfamily,category,alttype,altclassification");
        family.forEach((b,c) -> System.out.println(b + "," + c.get(0) + "," + c.get(1)+  "," + c.get(2) +  "," + c.get(3) +  "," + c.get(4)));

        String[] words = {"one", "two", "three", "four", "five"};

        Set<String> set = new HashSet<>( Arrays.asList(words));System.out.println(set);
        System.out.println(set);

    }
}

