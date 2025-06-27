package us.florent;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class SpeciesTree {

    //[, Class, Infraclass, Infraorder, Order, Phylum, SubClass, Suborder, Superfamily, Superorder]
    // [Kingdom, Phylum, Class, Order, Family, Genus, Species]

    public static class Taxon {
        private String name;
        private final String rank;
        boolean wasInserted = false;
        private String category = null;
        int AphiaID;

        public int getAphiaID() {
            return AphiaID;
        }

        public void setAphiaID(int aphiaID) {
            AphiaID = aphiaID;
        }


        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }


        public Taxon(String name, String rank) {
            this.name = name.trim();
            this.rank = rank.trim();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSortName() {
            return getName();
        }

        public String getSciName() {
            return getName();
        }


        public String getRank() {
            return rank;
        }
    }

    public static class Species extends Taxon {

        String id = null;
        String genus = null;
        String epithet = null;
        List<Taxon> path;

        public Species(String name, String rank) {
            super(name, rank);
        }

        @Override
        public String getSortName() {
            return epithet;
        }

        @Override
        public String getSciName() {
            return genus + " " + epithet;
        }

    }

    public static class TreeNode<T> {
        private final T value;
        private final List<TreeNode<T>> children;

        public TreeNode(T value) {
            this.value = value;
            this.children = new ArrayList<>();
        }

        public T getValue() {
            return value;
        }

        public List<? extends TreeNode<T>> getChildren() {
            return children;
        }

        public void addChild(TreeNode<T> child) {
            children.add(child);
        }
    }

    private final TreeNode<Taxon> root = new TreeNode<>(new Taxon("Biota", "Domain"));


    public TreeNode<Taxon> breadthFirstSearch(TreeNode<Taxon> root, String targetName) {
        if(root == null) return null;
        Queue<TreeNode<Taxon>> queue = new LinkedList<>();
        queue.add(root);

        while(!queue.isEmpty()) {
            TreeNode<Taxon> current = queue.poll();
            if(current.getValue().getName().equals(targetName)) {
                return current;
            }
            queue.addAll(current.getChildren());
        }
        return null; // Not found
    }


    public TreeNode<Taxon> depthFirstSearch(TreeNode<Taxon> node, String targetName) {
        if(node == null) return null;
        if(node.getValue().getName().equals(targetName)) {
            return node;
        }
        for(TreeNode<Taxon> child : node.getChildren()) {
            TreeNode<Taxon> result = depthFirstSearch(child, targetName);
            if(result != null) {
                return result;
            }
        }
        return null; // Not found
    }

    public Species findSpecies(TreeNode<Taxon> node, String name) {
        if(node == null)
            return null;
        if(node.getValue() instanceof Species sp) {
            if(sp.getSciName().equals(name))
                return sp;
        }

        for(TreeNode<Taxon> child : node.getChildren()) {
            var result = findSpecies(child, name);
            if(result != null) {
                return result;
            }
        }
        return null; // Not found
    }

    public Species addSpecies(String genus, String epithet, String speciesName) {
        TreeNode<Taxon> genusNode = depthFirstSearch(root, genus);
        if(genusNode == null) {
            return null; // Genus not found
        }
        // Check if the species already exists
        for(TreeNode<Taxon> child : genusNode.getChildren()) {
            if(child.getValue() instanceof Species species) {
                if(species.epithet.equals(epithet)) {
                    System.out.println("Species already exists: " + speciesName);
                    return species;
                }
            }
        }
        Species sp = new Species(speciesName, "Species");
        sp.genus = genus;
        sp.epithet = epithet;
        TreeNode<Taxon> speciesNode = new TreeNode<>(sp);
        if(epithet.equals("Unknown")) {
            sp.genus = "Unknown";
            genusNode.getValue().setName("Unknown");
        }
        genusNode.addChild(speciesNode);
        return sp;
    }

    public TreeNode<Taxon> addLeaf(String parentName, String leafName, String leafRank) {
        TreeNode<Taxon> parent = breadthFirstSearch(root, parentName);
        if(parent != null) {
            // Check if the leaf already exists
            for(TreeNode<Taxon> child : parent.getChildren()) {
                if(child.getValue().getName().equals(leafName)) {
                    //System.out.println("Leaf already exists: " + leafName);
                    return child;
                }
            }
            TreeNode<Taxon> leaf = leafRank.equals("Species") ? new TreeNode<>(new Species(leafName, leafRank)) : new TreeNode<>(new Taxon(leafName, leafRank));
            parent.addChild(leaf);
            return leaf;
        }
        return null; // Parent not found
    }

    public boolean insertNodeBetween(String parentName, String childName, String newNodeName, String newNodeRank) {
        TreeNode<Taxon> parent = breadthFirstSearch(root, parentName);
        if(parent == null) return false;

        TreeNode<Taxon> child = null;
        for(TreeNode<Taxon> c : parent.getChildren()) {
            if(c.getValue().getName().equals(childName)) {
                child = c;
                break;
            }
        }
        if(child == null) return false;

        // Remove child from parent's children
        parent.getChildren().remove(child);

        // Create new node and insert between
        TreeNode<Taxon> newNode = depthFirstSearch(root, newNodeName);
        if(newNode == null) {
            newNode = new TreeNode<>(new Taxon(newNodeName, newNodeRank));
            newNode.getValue().wasInserted = true;
            parent.addChild(newNode);
        }

        newNode.addChild(child);

        return true;
    }


    void printTree(TreeNode<Taxon> node, String prefix) {
        if(node == null) return;
        System.out.print(prefix + node.getValue().getName() + " (" + node.getValue().getRank() + ")");
        if(node.getValue().getCategory() != null) {
            System.out.print(" [Category: " + node.getValue().getCategory() + "]");
        }
        System.out.println();
        for(TreeNode<Taxon> child : node.getChildren()) {
            printTree(child, prefix + "..");
        }
    }

    public void sortTreeByName(TreeNode<Taxon> node) {
        if(node == null) return;
        if(node.getValue().getName().startsWith("_")) {
            node.getValue().setName("Unknown");
        }
        node.getChildren().sort((a, b) -> a.getValue().getSortName().compareToIgnoreCase(b.getValue().getSortName()));
        for(TreeNode<Taxon> child : node.getChildren()) {
            sortTreeByName(child);
        }
    }

    public String getRestReply(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if(conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while((output = br.readLine()) != null) {
            sb.append(output);
        }
        conn.disconnect();
        return sb.toString();
    }

    void createTree(MongoDatabase db) throws Exception {

        MongoCollection<Document> collection = db.getCollection("taxon");
        for(Document doc : collection.find()) {
            String name = doc.get("name").toString();
            String rank = doc.get("rank").toString();
            String parent = doc.get("parent").toString();
            String category = doc.get("category") != null ? doc.get("category").toString() : null;
            Taxon taxon = new Taxon(name, rank);
            taxon.setCategory(category);
            TreeNode<Taxon> node = new TreeNode<>(taxon);
            if(!parent.isEmpty()) {
                TreeNode<Taxon> parentNode = breadthFirstSearch(root, parent);
                if(parentNode != null) {
                    parentNode.addChild(node);
                } else {
                    System.out.println("Parent not found: " + parent);
                    throw new Exception("Parent not found: " + parent);
                }
            } else {
                root.addChild(node);
            }
        }

//        MongoCollection<Document> collection = db.getCollection("categories");
//        for(Document doc : collection.find()) {
//            TreeNode<Taxon> node = root;
//            if(!doc.get("alttype").toString().isEmpty()) {
//                String alttype = doc.get("alttype").toString();
//                String name = doc.get("altclassification").toString();
//                node = addLeaf("Biota", name, alttype);
//            }
//            // family and subfamily
//
//            String family = doc.get("family").toString();
//            String subfamily = doc.get("subfamily").toString();
//            TreeNode<Taxon> familyNode = addLeaf(node.getValue().getName(), family, "Family");
//            if(!subfamily.isEmpty()) {
//                TreeNode<Taxon> subfamilyNode = addLeaf(familyNode.getValue().getName(), subfamily, "Subfamily");
//                subfamilyNode.getValue().setCategory(doc.get("category").toString());
//            } else {
//                familyNode.getValue().setCategory(doc.get("category").toString());
//            }
//
//        }

        //topTaxonomy();

        collection = db.getCollection("genus");
        for(Document doc : collection.find()) {
            if(doc.get("subfamily").toString().isEmpty()) {
                String family = doc.get("family").toString();
                String genus = doc.get("genus").toString();
                TreeNode<Taxon> genusNode = addLeaf(family, genus, "Genus");
            } else {
                String subfamily = doc.get("subfamily").toString();
                String genus = doc.get("genus").toString();
                TreeNode<Taxon> genusNode = addLeaf(subfamily, genus, "Genus");
            }
        }

        collection = db.getCollection("species");
        for(Document doc : collection.find()) {
            String[] sciName = doc.get("sciName").toString().split(" ", 2);
            if(sciName.length < 2) {
                System.out.println("Invalid scientific name: " + doc.get("Name").toString());
                //addSpecies("Unknown", "Unknown", doc.get("Name").toString());
                sciName = new String[]{doc.get("id").toString(), "Unknown"}; // Default to Unknown if invalid
                // continue; // Skip invalid names
            }
            addSpecies(sciName[0], sciName[1], doc.get("Name").toString());
        }

        sortTreeByName(depthFirstSearch(root, "Animalia"));

    }

    void topTaxonomy() {

        insertNodeBetween("Biota", "Balistidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Biota", "Monacanthidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Biota", "Tetraodontidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Biota", "Diodontidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Biota", "Ostraciidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Biota", "Balistidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Biota", "Ogcocephalidae", "Lophiiformes", "Order");
        insertNodeBetween("Biota", "Antennariidae", "Lophiiformes", "Order");
        insertNodeBetween("Biota", "Dactylopteridae", "Dactylopteriformes", "Order");
        insertNodeBetween("Biota", "Pegasidae", "Dactylopteriformes", "Order");
        insertNodeBetween("Biota", "Bothidae", "Pleuronectiformes", "Order");
        insertNodeBetween("Biota", "Samaridae", "Pleuronectiformes", "Order");
        insertNodeBetween("Biota", "Paralichthyidae", "Pleuronectiformes", "Order");
        insertNodeBetween("Biota", "Soleidae", "Pleuronectiformes", "Order");
        insertNodeBetween("Biota", "Cynoglossidae", "Pleuronectiformes", "Order");
        insertNodeBetween("Biota", "Gobiesocidae", "Gobiesociformes", "Order");

        insertNodeBetween("Biota", "Scorpaenidae", "Scorpaenoidei", "Suborder");
        insertNodeBetween("Biota", "Platycephalidae", "Scorpaenoidei", "Suborder");
        insertNodeBetween("Biota", "Synanceiidae", "Scorpaenoidei", "Suborder");
        insertNodeBetween("Biota", "Tetrarogidae", "Scorpaenoidei", "Suborder");
        insertNodeBetween("Biota", "Aploactinidae", "Scorpaenoidei", "Suborder");
        insertNodeBetween("Biota", "Uranoscopidae", "Uranoscopoidei", "Suborder");
        insertNodeBetween("Biota", "Pinguipedidae", "Uranoscopoidei", "Suborder");
        insertNodeBetween("Biota", "Uranoscopoidei", "Perciformes", "Order");
        insertNodeBetween("Biota", "Percoidei", "Perciformes", "Order");
        insertNodeBetween("Biota", "Scorpaenoidei", "Perciformes", "Order");
        insertNodeBetween("Biota", "Cirrhitidae", "Centrarchiformes", "Order");
        insertNodeBetween("Biota", "Callionymidae", "Callionymiformes", "Order");
        insertNodeBetween("Biota", "Microdesmidae", "Gobiiformes", "Order");
        insertNodeBetween("Biota", "Trichonotidae", "Gobiiformes", "Order");
        insertNodeBetween("Biota", "Synodontidae", "Aulopiformes", "Order");
        insertNodeBetween("Biota", "Ephippidae", "Acanthuriformes", "Order");
        insertNodeBetween("Biota", "Chelonioidea", "Testudines", "Order");
        insertNodeBetween("Biota", "Elapidae", "Serpentes", "Suborder");
        insertNodeBetween("Biota", "Stenopodidea", "Decapoda", "Order");
        insertNodeBetween("Biota", "Caridea", "Decapoda", "Order");
        insertNodeBetween("Biota", "Dendrobranchiata", "Decapoda", "Order");
        insertNodeBetween("Biota", "Axiidea", "Decapoda", "Order");
        insertNodeBetween("Biota", "Achelata", "Decapoda", "Order");
        insertNodeBetween("Biota", "Astacidea", "Decapoda", "Order");
        insertNodeBetween("Biota", "Anomura", "Decapoda", "Order");
        insertNodeBetween("Biota", "Brachyura", "Decapoda", "Order");
        insertNodeBetween("Biota", "Corophiida", "Amphipoda", "Order");

        insertNodeBetween("Biota", "Galatheoidea", "Anomura", "Infraorder");

        insertNodeBetween("Biota", "Spionida", "Canalipalpata", "Order");
        insertNodeBetween("Biota", "Sabellida", "Canalipalpata", "Order");
        insertNodeBetween("Biota", "Terebellida", "Canalipalpata", "Order");
        insertNodeBetween("Biota", "Doridina", "Nudibranchia", "Order");
        insertNodeBetween("Biota", "Cladobranchia", "Nudibranchia", "Order");
        insertNodeBetween("Biota", "Arminoidea", "Cladobranchia", "Suborder");
        insertNodeBetween("Biota", "Dendronotoidea", "Cladobranchia", "Suborder");
        insertNodeBetween("Biota", "Tritonioidea", "Cladobranchia", "Suborder");
        insertNodeBetween("Biota", "Amphilepidida", "Ophintegrida", "Superorder");
        insertNodeBetween("Biota", "Ophiacanthida", "Ophintegrida", "Superorder");
        insertNodeBetween("Biota", "Euryalida", "Euryophiurida", "Superorder");


        insertNodeBetween("Biota", "Tetraodontiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Lophiiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Batrachoidiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Dactylopteriformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Perciformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Centrarchiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Pleuronectiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Gobiesociformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Callionymiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Gobiiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Aulopiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Mulliformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Blenniiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Holocentriformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Kurtiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Carangiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Scombriformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Acanthuriformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Elopiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Albuliformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Beloniformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Siluriformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Syngnathiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Anguilliformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Eupercaria", "Teleostei", "Class");
        insertNodeBetween("Biota", "Acropomatiformes", "Teleostei", "Class");
        insertNodeBetween("Biota", "Carangaria", "Teleostei", "Class");
        insertNodeBetween("Biota", "Ovalentaria", "Teleostei", "Class");
        insertNodeBetween("Biota", "Canalipalpata", "Polychaeta", "Class");
        insertNodeBetween("Biota", "Eunicida", "Polychaeta", "Class");
        insertNodeBetween("Biota", "Phyllodocida", "Polychaeta", "Class");
        insertNodeBetween("Biota", "Nudibranchia", "Euthyneura", "Infraclass");
        insertNodeBetween("Biota", "Sacoglossa", "Euthyneura", "Infraclass");
        insertNodeBetween("Biota", "Cephalaspidea", "Euthyneura", "Infraclass");
        insertNodeBetween("Biota", "Euthyneura", "Heterobranchia", "Subclass");
        insertNodeBetween("Biota", "Architectonicoidea", "Heterobranchia", "Subclass");
        insertNodeBetween("Biota", "Heterobranchia", "Gastropoda", "Class");
        insertNodeBetween("Biota", "Trochida", "Vetigastropoda", "Subclass");
        insertNodeBetween("Biota", "Vetigastropoda", "Gastropoda", "Class");
        insertNodeBetween("Biota", "Acteonimorpha", "Euthyneura", "Infraclass");
        insertNodeBetween("Biota", "Aplysiida", "Euthyneura", "Infraclass");
        insertNodeBetween("Biota", "Pteropoda", "Euthyneura", "Infraclass");
        insertNodeBetween("Biota", "Pleurobranchida", "Euthyneura", "Infraclass");
        insertNodeBetween("Biota", "Myopsida", "Cephalopoda", "Class");
        insertNodeBetween("Biota", "Idiosepida", "Cephalopoda", "Class");
        insertNodeBetween("Biota", "Sepiida", "Cephalopoda", "Class");
        insertNodeBetween("Biota", "Octopoda", "Cephalopoda", "Class");
        insertNodeBetween("Biota", "Cardiida", "Bivalvia", "Class");
        insertNodeBetween("Biota", "Ostreida", "Bivalvia", "Class");
        insertNodeBetween("Biota", "Pectinida", "Bivalvia", "Class");
        insertNodeBetween("Biota", "Limida", "Bivalvia", "Class");
        insertNodeBetween("Biota", "Euryophiurida", "Ophiuroidea", "Class");
        insertNodeBetween("Biota", "Ophintegrida", "Ophiuroidea", "Class");
        insertNodeBetween("Biota", "Chitonida", "Polyplacophora", "Class");
        insertNodeBetween("Biota", "Heteronemertea", "Pilidiophora", "Class");
        insertNodeBetween("Biota", "Scleralcyonacea", "Octocorallia", "Class");
        insertNodeBetween("Biota", "Malacalcyonacea", "Octocorallia", "Class");
        insertNodeBetween("Biota", "Scleralcyonacea", "Octocorallia", "Class");
        insertNodeBetween("Biota", "Dendrochirotida", "Holothuroidea", "Class");
        insertNodeBetween("Biota", "Apodida", "Holothuroidea", "Class");
        insertNodeBetween("Biota", "Holothuriida", "Holothuroidea", "Class");
        insertNodeBetween("Biota", "Synallactida", "Holothuroidea", "Class");
        insertNodeBetween("Biota", "Camarodonta", "Echinoidea", "Class");
        insertNodeBetween("Biota", "Cidaroida", "Echinoidea", "Class");
        insertNodeBetween("Biota", "Diadematoida", "Echinoidea", "Class");
        insertNodeBetween("Biota", "Echinothurioida", "Echinoidea", "Class");
        insertNodeBetween("Biota", "Spatangoida", "Echinoidea", "Class");
        insertNodeBetween("Biota", "Valvatida", "Asteroidea", "Class");
        insertNodeBetween("Biota", "Paxillosida", "Asteroidea", "Class");
        insertNodeBetween("Biota", "Spinulosida", "Asteroidea", "Class");
        insertNodeBetween("Biota", "Rhizostomeae", "Scyphozoa", "Class");
        insertNodeBetween("Biota", "Semaeostomeae", "Scyphozoa", "Class");
        insertNodeBetween("Biota", "Coronatae", "Scyphozoa", "Class");
        insertNodeBetween("Biota", "Comatulida", "Crinoidea", "Class");
        insertNodeBetween("Biota", "Balanomorpha", "Thecostraca", "Class");
        insertNodeBetween("Biota", "Pantopoda", "Pycnogonida", "Class");
        insertNodeBetween("Biota", "Xiphosurida", "Merostomata", "Class");
        insertNodeBetween("Biota", "Lobata", "Tentaculata", "Class");
        insertNodeBetween("Biota", "Cestida", "Tentaculata", "Class");
        insertNodeBetween("Biota", "Platyctenida", "Tentaculata", "Order");


        insertNodeBetween("Biota", "Myliobatiformes", "Batoidea", "Infraclass");
        insertNodeBetween("Biota", "Carcharhiniformes", "Selachii", "Infraclass");
        insertNodeBetween("Biota", "Orectolobiformes", "Selachii", "Infraclass");
        insertNodeBetween("Biota", "Batoidea", "Elasmobranchii", "Class");
        insertNodeBetween("Biota", "Selachii", "Elasmobranchii", "Class");
        insertNodeBetween("Biota", "Orectolobiformes", "Elasmobranchii", "Class");
        insertNodeBetween("Biota", "Torpediniformes", "Batoidea", "Infraclass");
        insertNodeBetween("Biota", "Testudines", "Reptilia", "Class");
        insertNodeBetween("Biota", "Serpentes", "Reptilia", "Class");
        insertNodeBetween("Biota", "Cetacea", "Cetartiodactyla", "Order");
        insertNodeBetween("Biota", "Cetartiodactyla", "Mammalia", "Class");
        insertNodeBetween("Biota", "Sirenia", "Mammalia", "Class");
        insertNodeBetween("Biota", "Decapoda", "Malacostraca", "Class");
        insertNodeBetween("Biota", "Stomatopoda", "Malacostraca", "Class");
        insertNodeBetween("Biota", "Amphipoda", "Malacostraca", "Class");
        insertNodeBetween("Biota", "Malacostraca", "Crustacea", "Subphylum");
        insertNodeBetween("Biota", "Mysida", "Crustacea", "Subphylum");
        insertNodeBetween("Biota", "Cirripedia", "Crustacea", "Subphylum");
        insertNodeBetween("Biota", "Thecostraca", "Crustacea", "Subphylum");
        insertNodeBetween("Biota", "Isopoda", "Malacostraca", "Subphylum");
        insertNodeBetween("Biota", "Mysida", "Malacostraca", "Subphylum");
        insertNodeBetween("Biota", "Littorinimorpha", "Caenogastropoda", "Subclass");
        insertNodeBetween("Biota", "Caenogastropoda incertae sedis", "Caenogastropoda", "Subclass");
        insertNodeBetween("Biota", "Caenogastropoda", "Gastropoda", "Class");
        insertNodeBetween("Biota", "Neogastropoda", "Caenogastropoda", "Subclass");
        insertNodeBetween("Biota", "Leptothecata", "Hydrozoa", "Class");
        insertNodeBetween("Biota", "Siphonophorae", "Hydrozoa", "Class");
        insertNodeBetween("Biota", "Anthoathecata", "Hydrozoa", "Class");
        insertNodeBetween("Biota", "Zoantharia", "Hexacorallia", "Class");
        insertNodeBetween("Biota", "Actiniaria", "Hexacorallia", "Class");
        insertNodeBetween("Biota", "Ceriantharia", "Hexacorallia", "Class");
        insertNodeBetween("Biota", "Corallimorpharia", "Hexacorallia", "Class");
        insertNodeBetween("Biota", "Scleractinia", "Hexacorallia", "Class");
        insertNodeBetween("Biota", "Antipatharia", "Hexacorallia", "Class");
        insertNodeBetween("Biota", "Tentaculata", "Ctenophora", "Phylum");
        insertNodeBetween("Biota", "Tentaculata", "Ctenophora", "Phylum");
        insertNodeBetween("Biota", "Asteroidea", "Asterozoa", "Subphylum");
        insertNodeBetween("Biota", "Ophiuroidea", "Asterozoa", "Subphylum");
        insertNodeBetween("Biota", "Asterozoa", "Echinodermata", "Phylum");
        insertNodeBetween("Biota", "Crinoidea", "Crinozoa", "Subphylum");
        insertNodeBetween("Biota", "Crinozoa", "Echinodermata", "Phylum");
        insertNodeBetween("Biota", "Echinoidea", "Echinozoa", "Subphylum");
        insertNodeBetween("Biota", "Holothuroidea", "Echinozoa", "Subphylum");
        insertNodeBetween("Biota", "Echinozoa", "Echinodermata", "Phylum");
        insertNodeBetween("Biota", "Ascidiacea", "Tunicata", "Subphylum");
        insertNodeBetween("Biota", "Thaliacea", "Tunicata", "Subphylum");
        insertNodeBetween("Biota", "Gymnolaemata", "Bryozoa", "Phylum");
        insertNodeBetween("Biota", "Heteroscleromorpha", "Demospongiae", "Class");
        insertNodeBetween("Biota", "Verongimorpha", "Demospongiae", "Class");
        insertNodeBetween("Biota", "Keratosa", "Demospongiae", "Class");
        insertNodeBetween("Biota", "Demospongiae", "Porifera", "Phylum");
        insertNodeBetween("Biota", "Homoschleromorpha", "Porifera", "Phylum");
        insertNodeBetween("Biota", "Calcarea", "Porifera", "Phylum");
        insertNodeBetween("Biota", "Merostomata", "Chelicerata", "Subphylum");
        insertNodeBetween("Biota", "Pycnogonida", "Chelicerata", "Subphylum");
        insertNodeBetween("Biota", "Pilidiophora", "Nemertea", "Phylum");

        insertNodeBetween("Biota", "Teleostei", "Actinopteri", "Superclass");
        insertNodeBetween("Biota", "Actinopteri", "Actinopterygii", "Gigaclass");
        insertNodeBetween("Biota", "Actinopterygii", "Osteichthyes", "Parvphylum");
        insertNodeBetween("Biota", "Elasmobranchii", "Chondrichthyes", "Parvphylum");
        insertNodeBetween("Biota", "Osteichthyes", "Chordata", "Phylum");
        insertNodeBetween("Biota", "Chondrichthyes", "Chordata", "Phylum");
        insertNodeBetween("Biota", "Reptilia", "Chordata", "Phylum");
        insertNodeBetween("Biota", "Mammalia", "Chordata", "Phylum");
        insertNodeBetween("Biota", "Tunicata", "Chordata", "Phylum");
        insertNodeBetween("Biota", "Chordata", "Animalia", "Kingdom");
        insertNodeBetween("Biota", "Crustacea", "Arthropoda", "Phylum");
        insertNodeBetween("Biota", "Chelicerata", "Arthropoda", "Phylum");
        insertNodeBetween("Biota", "Arthropoda", "Animalia", "Kingdom");
        insertNodeBetween("Biota", "Polycladida", "Platyhelminthes", "Phylum");
        insertNodeBetween("Biota", "Acoela", "Xenacoelomorpha", "Phylum");
        insertNodeBetween("Biota", "Amphinomida", "Annelida", "Phylum");
        insertNodeBetween("Biota", "Platyhelminthes", "Animalia", "Kingdom");
        insertNodeBetween("Biota", "Xenacoelomorpha", "Animalia", "Kingdom");
        insertNodeBetween("Biota", "Annelida", "Animalia", "Kingdom");
        insertNodeBetween("Biota", "Polychaeta", "Annelida", "Kingdom");
        insertNodeBetween("Biota", "Gastropoda", "Mollusca", "Phylum");
        insertNodeBetween("Biota", "Polyplacophora", "Mollusca", "Phylum");
        insertNodeBetween("Biota", "Mollusca", "Animalia", "Kingdom");
        insertNodeBetween("Biota", "Cephalopoda", "Mollusca", "Phylum");
        insertNodeBetween("Biota", "Bivalvia", "Mollusca", "Phylum");
        insertNodeBetween("Biota", "Scyphozoa", "Medusozoa", "Subphylum");
        insertNodeBetween("Biota", "Hydrozoa", "Medusozoa", "Subphylum");
        insertNodeBetween("Biota", "Hexacorallia", "Anthozoa", "Subphylum");
        insertNodeBetween("Biota", "Octocorallia", "Anthozoa", "Subphylum");
        insertNodeBetween("Biota", "Medusozoa", "Cnidaria", "Phylum");
        insertNodeBetween("Biota", "Anthozoa", "Cnidaria", "Phylum");
        insertNodeBetween("Biota", "Cnidaria", "Animalia", "Phylum");
        insertNodeBetween("Biota", "Ctenophora", "Animalia", "Class");
        insertNodeBetween("Biota", "Echinodermata", "Animalia", "Kingdom");
        insertNodeBetween("Biota", "Bryozoa", "Animalia", "Kingdom");
        insertNodeBetween("Biota", "Porifera", "Animalia", "Kingdom");
        insertNodeBetween("Biota", "Phoronida", "Animalia", "Kingdom");
        insertNodeBetween("Biota", "Nemertea", "Animalia", "Kingdom");
        //insertNodeBetween("Biota", "Animalia", "Eukaryota", "Domain");

        insertNodeBetween("Biota", "Ulvophyceae", "Chlorophyta", "Division");
        insertNodeBetween("Biota", "Chlorophyta", "Viridiplantae", "Subkingdom");
        insertNodeBetween("Biota", "Viridiplantae", "Plantae", "Kingdom");
        //insertNodeBetween("Biota", "Plantae", "Eukaryota", "Domain");
        insertNodeBetween("Biota", "Florideophyceae", "Rhodophyta", "Division");
        insertNodeBetween("Biota", "Rhodophyta", "Biliphyta", "Subkingdom");
        insertNodeBetween("Biota", "Biliphyta", "Plantae", "Kingdom");
        insertNodeBetween("Biota", "Phaeophyceae", "Ochrophyta", "Phylum");
        insertNodeBetween("Biota", "Ochrophyta", "Chromista", "Kingdom");
        //insertNodeBetween("Biota", "Chromista", "Eukaryota", "Domain");

        insertNodeBetween("Cladobranchia", "Flabellinidae", "Aeolidioidea", "Superfamily");
        insertNodeBetween("Cladobranchia", "Facelinidae", "Aeolidioidea", "Superfamily");
        insertNodeBetween("Cladobranchia", "Myrrhinidae", "Aeolidioidea", "Superfamily");
        insertNodeBetween("Cladobranchia", "Glaucidae", "Aeolidioidea", "Superfamily");
        insertNodeBetween("Cladobranchia", "Trinchesiidae", "Fionoidea", "Superfamily");
        insertNodeBetween("Cladobranchia", "Samlidae", "Fionoidea", "Superfamily");
        insertNodeBetween("Cladobranchia", "Eubranchidae", "Fionoidea", "Superfamily");
        insertNodeBetween("Sepiida", "Sepiadariidae", "Sepioloidea", "Superfamily");
        insertNodeBetween("Sepiida", "Sepiolidae", "Sepioloidea", "Superfamily");
        insertNodeBetween("Sepiida", "Sepiidae", "Sepioidea", "Superfamily");
        insertNodeBetween("Anomura", "Diogenidae", "Paguroidea", "Superfamily");
        insertNodeBetween("Anomura", "Paguridae", "Paguroidea", "Superfamily");
        insertNodeBetween("Anomura", "Porcellanidae", "Galatheoidea", "Superfamily");

        breadthFirstSearch(root, "Paguroidea").getValue().setCategory("Hermit Crab");
        breadthFirstSearch(root, "Demospongiae").getValue().setCategory("Common Sponges");
        breadthFirstSearch(root, "Porifera").getValue().setCategory("Sponges");
        breadthFirstSearch(root, "Batoidea").getValue().setCategory("Rays");
        breadthFirstSearch(root, "Crustacea").getValue().setCategory("Crustaceans");

    }


    public void printNodeJson(TreeNode<Taxon> node, String parentName, StringBuilder jsonOutput ) {
        JSONObject obj = new JSONObject();
        obj.put("name", node.getValue().getName());
        obj.put("rank", node.getValue().getRank());
        obj.put("category", node.getValue().getCategory());
        obj.put("parent", parentName);
        jsonOutput.append(obj.toString()).append("\n");

        for (TreeNode<Taxon> child : node.getChildren()) {
            if(child.getValue() instanceof Species
            //|| child.getValue().getRank().equals("Subfamily")
            || child.getValue().getRank().equals("Genus")) {
                continue;
            }
            printNodeJson(child, node.getValue().getName(), jsonOutput);
        }
    }

    public TreeNode<Taxon> buildTaxonomy() throws Exception {
        System.setProperty("org.slf4j.simpleLogger.log.org.mongodb.driver", "warn");

        MongoDatabase db = null;
        MongoClient mongoClient = MongoClients.create();
        db = mongoClient.getDatabase("reef4");

        createTree(db);

        mongoClient.close();

        return root;
    }

    public String displayTree(TreeNode<Taxon> root) {
        StringBuilder out = new StringBuilder();
        displayTree(root, "", true, out);
        return out.toString();
    }

    private void displayTree(TreeNode<Taxon> node, String indent, boolean isLast, StringBuilder out) {
        out.append(indent);
        if(isLast) {
            if(!indent.isEmpty())
                out.append("└─");
            indent += "  ";
        } else {
            out.append("├─");
            indent += "│ ";
        }

        if(node.getValue() instanceof Species species) {
            out.append(species.genus.charAt(0) + ". " + species.epithet + " -  " + species.getName()).append("\n");
        } else {
            String cat = (node.getValue().getCategory() == null) ? "" : " [" + node.getValue().getCategory() + "]";
            out.append(node.getValue().getName() + " (" + node.getValue().getRank() + ")" + cat).append("\n");
        }

        for(int i = 0; i < node.children.size(); i++) {
            displayTree(node.children.get(i), indent, i == node.children.size() - 1, out);
        }
    }

    public void exportTreeToCSV(TreeNode<Taxon> root, String filename) throws IOException {
        List<String> ranks = Arrays.asList("Kingdom", "Subkingdom", "Division", "Phylum", "Subphylum", "Parvphylum", "Gigaclass", "Superclass", "Class", "Subclass",
                "Infraclass", "Subterclass", "Superorder", "Order", "Suborder", "Infraorder", "Superfamily", "Family", "Subfamily", "Genus", "Species", "Common Name", "Category");
        FileWriter writer = new FileWriter(filename);
        writer.write(String.join(",", ranks) + "\n");

        List<String> path = new ArrayList<>(Collections.nCopies(ranks.size(), ""));
        exportTreeToCSVHelper(root, path, ranks, writer);
        writer.close();
    }

    private void exportTreeToCSVHelper(TreeNode<Taxon> node, List<String> path, List<String> ranks, FileWriter writer) throws IOException {
        int idx = ranks.indexOf(node.getValue().getRank());
        if(idx >= 0) path.set(idx, node.getValue().getSortName());
        else
            System.out.println("Unknown rank: " + node.getValue().getRank() + " for " + node.getValue().getName());
        if(!Objects.isNull(node.getValue().getCategory()))
            path.set(ranks.indexOf("Category"), node.getValue().getCategory());
        if(node.getValue() instanceof Species sp) {
            path.set(ranks.indexOf("Common Name"), node.getValue().getName());
        }

        if(node.getChildren().isEmpty()) {
            writer.write(String.join(",", path) + "\n");
        } else {
            for(TreeNode<Taxon> child : node.getChildren()) {
                exportTreeToCSVHelper(child, new ArrayList<>(path), ranks, writer);
            }
        }
    }

    public List<String> getAlldangelingLeaf(TreeNode<Taxon> node) {
        List<String> leaves = new ArrayList<>();
        if(node.getChildren().isEmpty()) {
            if(!node.getValue().getRank().equals("Species")) {
                leaves.add(node.getValue().getName() + " (" + node.getValue().getRank() + ")");
            }
        } else {
            for(TreeNode<Taxon> child : node.getChildren()) {
                leaves.addAll(getAlldangelingLeaf(child));
            }
        }
        return leaves;
    }

    public List<Taxon> getPathToSpecies(TreeNode<Taxon> node, Species species) {
        List<Taxon> path = new ArrayList<>();
        if(findPathHelper(node, species, path)) {
            return path;
        }
        return Collections.emptyList(); // Not found
    }

    private boolean findPathHelper(TreeNode<Taxon> node, Species species, List<Taxon> path) {
        path.add(node.getValue());
        if(node.getValue() instanceof Species sp && sp.equals(species)) {
            return true;
        }
        for(TreeNode<Taxon> child : node.getChildren()) {
            if(findPathHelper(child, species, path)) {
                return true;
            }
        }
        path.removeLast();
        return false;
    }


    public List<String> getAllSpeciesSciNAmes(TreeNode<Taxon> node) {
        List<String> leaves = new ArrayList<>();
        if(node.getChildren().isEmpty()) {
            if(node.getValue().getRank().equals("Species")) {
                Species sp = (Species) node.getValue();
                leaves.add(sp.genus + " " + sp.epithet);
            }
        } else {
            for(TreeNode<Taxon> child : node.getChildren()) {
                leaves.addAll(getAllSpeciesSciNAmes(child));
            }
        }
        return leaves;
    }

    void addAphiaIDB() {
        try(BufferedReader br = new BufferedReader(new FileReader("worms.txt"))) {
            String line;
            while((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                //System.out.println("Adding AphiaID " + fields[1] + " to " + fields[0]);
                Species sp = findSpecies(root, fields[0]);
                sp.setAphiaID(Integer.parseInt(fields[1]));
                List<Taxon> list = getPathToSpecies(root, sp);
                sp.path = list;
                //list.forEach(t -> System.out.print(t.getName() + "[" + t.getRank() + "] > "));
                //System.out.println();

                List<Taxon> result = new ArrayList<>();
                JSONObject json = new JSONObject(fields[3]);
                collectNames(json, result);
                //result.forEach(t -> System.out.print(t.getName() + "[}" + t.getRank() + "] - "));
                //System.out.println();

                //compareTaxonLists(sp.getName(), list, result);
                compareTaxonLists(sp.getName(), result, list);

            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void compareTaxonLists(String id, List<Taxon> list1, List<Taxon> list2) {

        for (int i = 2; i < list1.size(); i++) {
            String name =list1.get(i).getSciName();
            String rank =list1.get(i).getRank();

            var match = list2.stream().filter(t -> t.getSciName().startsWith(name) && t.getRank().equals(rank)).findFirst();
            if(match.isEmpty()) {
                System.out.println(id + "--> No match for: " + name + " (" + rank + ")");
            }
        }
    }

    private void collectNames(JSONObject node, List<Taxon> result) {
        if(node.has("scientificname")) {
            result.add(new Taxon(node.getString("scientificname"), node.getString("rank")));
        }
        if(node.has("child")) {
            Object child = node.get("child");

            if(child instanceof JSONObject jsonChild) {
                collectNames(jsonChild, result);
            }
        }
    }


    void worms() throws IOException {

        List<String> speciesList = getAllSpeciesSciNAmes(root);
        StringBuilder sb = new StringBuilder();
        AtomicInteger count = new AtomicInteger();
        speciesList.stream().limit(5000).forEach(sp -> {
            String wsp = sp.replace(" ", "%20");
            try {
                System.out.print(count.incrementAndGet() + "\t");
                String id = getRestReply("https://www.marinespecies.org/rest/AphiaIDByName/" + wsp + "?marine_only=true&extant_only=true");
                System.out.print(sp + "\t" + id + "\t");
                sb.append(sp).append("\t").append(id).append("\t");
                String rec = getRestReply("https://www.marinespecies.org/rest/AphiaRecordByAphiaID/" + id);
                JSONObject json = new JSONObject(rec);
                String status = json.get("status").toString();
                System.out.print(status + "\t");
                sb.append(status).append("\t");
                String taxon = getRestReply("https://www.marinespecies.org/rest/AphiaClassificationByAphiaID/" + id);
                System.out.println(taxon);
                sb.append(taxon).append("\n");

            } catch(Exception e) {
                System.out.println("Species: " + sp + " UNKNOWN");
            }
        });
        try(FileWriter writer = new FileWriter("/tmp/worms.txt")) {
            writer.write(sb.toString());
        }
    }


    public static void main(String[] args) throws Exception {

        SpeciesTree speciesTree = new SpeciesTree();
        speciesTree.buildTaxonomy();
        speciesTree.addAphiaIDB();
        // speciesTree.printTree(speciesTree.root, "");

        System.out.println();
        System.out.println();
        speciesTree.sortTreeByName(speciesTree.root);
        String tree = speciesTree.displayTree(speciesTree.depthFirstSearch(speciesTree.root, "Biota"));
        System.out.println(tree);
        try(FileWriter writer = new FileWriter("/tmp/taxonomy_tree.txt")) {
            writer.write(tree);
        }

        speciesTree.exportTreeToCSV(speciesTree.root, "/tmp/species.csv");

        List<String> leafNames = speciesTree.getAlldangelingLeaf(speciesTree.root);
        leafNames.forEach(System.out::println);
        System.out.println();
        System.out.println();

        StringBuilder out = new StringBuilder();
        speciesTree.printNodeJson(speciesTree.root, null, out);
        try(FileWriter writer = new FileWriter("/tmp/taxonomy_nodes.json")) {
            writer.write(out.toString());
        }

        System.exit(0);

        speciesTree.worms();


    }

}
