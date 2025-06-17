package us.florent;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONObject;


import java.io.BufferedReader;
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
            return name;
        }


        public String getRank() {
            return rank;
        }
    }

    public static class Species extends Taxon {

        String id = null;
        String genus = null;
        String epithet = null;

        public Species(String name, String rank) {
            super(name, rank);
        }

        @Override
        public String getSortName() {
            return epithet;
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

    private final TreeNode<Taxon> root = new TreeNode<>(new Taxon("Root", "Domain"));


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

    void createTree(MongoDatabase db) {
        MongoCollection<Document> collection = db.getCollection("categories");
        for(Document doc : collection.find()) {
            TreeNode<Taxon> node = root;
            if(!doc.get("alttype").toString().isEmpty()) {
                String alttype = doc.get("alttype").toString();
                String name = doc.get("altclassification").toString();
                node = addLeaf("Root", name, alttype);
            }
            // family and subfamily

            String family = doc.get("family").toString();
            String subfamily = doc.get("subfamily").toString();
            TreeNode<Taxon> familyNode = addLeaf(node.getValue().getName(), family, "Family");
            if(!subfamily.isEmpty()) {
                TreeNode<Taxon> subfamilyNode = addLeaf(familyNode.getValue().getName(), subfamily, "Subfamily");
                subfamilyNode.getValue().setCategory(doc.get("category").toString());
            } else {
                familyNode.getValue().setCategory(doc.get("category").toString());
            }

        }

        topTaxonomy();

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
                sciName = new String[]{ doc.get("id").toString(), "Unknown"}; // Default to Unknown if invalid
               // continue; // Skip invalid names
            }
            addSpecies(sciName[0], sciName[1], doc.get("Name").toString());
        }

        sortTreeByName(depthFirstSearch(root, "Animalia"));

    }

    void topTaxonomy() {

        insertNodeBetween("Root", "Balistidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Root", "Monacanthidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Root", "Tetraodontidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Root", "Diodontidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Root", "Ostraciidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Root", "Balistidae", "Tetraodontiformes", "Order");
        insertNodeBetween("Root", "Ogcocephalidae", "Lophiiformes", "Order");
        insertNodeBetween("Root", "Antennariidae", "Lophiiformes", "Order");
        insertNodeBetween("Root", "Dactylopteridae", "Dactylopteriformes", "Order");
        insertNodeBetween("Root", "Pegasidae", "Dactylopteriformes", "Order");
        insertNodeBetween("Root", "Bothidae", "Pleuronectiformes", "Order");
        insertNodeBetween("Root", "Samaridae", "Pleuronectiformes", "Order");
        insertNodeBetween("Root", "Paralichthyidae", "Pleuronectiformes", "Order");
        insertNodeBetween("Root", "Soleidae", "Pleuronectiformes", "Order");
        insertNodeBetween("Root", "Cynoglossidae", "Pleuronectiformes", "Order");
        insertNodeBetween("Root", "Gobiesocidae", "Gobiesociformes", "Order");

        insertNodeBetween("Root", "Scorpaenidae", "Scorpaenoidei", "Suborder");
        insertNodeBetween("Root", "Platycephalidae", "Scorpaenoidei", "Suborder");
        insertNodeBetween("Root", "Synanceiidae", "Scorpaenoidei", "Suborder");
        insertNodeBetween("Root", "Tetrarogidae", "Scorpaenoidei", "Suborder");
        insertNodeBetween("Root", "Aploactinidae", "Scorpaenoidei", "Suborder");
        insertNodeBetween("Root", "Uranoscopidae", "Uranoscopoidei", "Suborder");
        insertNodeBetween("Root", "Pinguipedidae", "Uranoscopoidei", "Suborder");
        insertNodeBetween("Root", "Uranoscopoidei", "Perciformes", "Order");
        insertNodeBetween("Root", "Scorpaenoidei", "Perciformes", "Order");
        insertNodeBetween("Root", "Cirrhitidae", "Centrarchiformes", "Order");
        insertNodeBetween("Root", "Callionymidae", "Callionymiformes", "Order");
        insertNodeBetween("Root", "Microdesmidae", "Gobiiformes", "Order");
        insertNodeBetween("Root", "Trichonotidae", "Gobiiformes", "Order");
        insertNodeBetween("Root", "Synodontidae", "Aulopiformes", "Order");
        insertNodeBetween("Root", "Ephippidae", "Acanthuriformes", "Order");
        insertNodeBetween("Root", "Chelonioidea", "Testudines", "Order");
        insertNodeBetween("Root", "Elapidae", "Serpentes", "Suborder");
        insertNodeBetween("Root", "Stenopodidea", "Decapoda", "Order");
        insertNodeBetween("Root", "Caridea", "Decapoda", "Order");
        insertNodeBetween("Root", "Dendrobranchiata", "Decapoda", "Order");
        insertNodeBetween("Root", "Axiidea", "Decapoda", "Order");
        insertNodeBetween("Root", "Achelata", "Decapoda", "Order");
        insertNodeBetween("Root", "Astacidea", "Decapoda", "Order");
        insertNodeBetween("Root", "Anomura", "Decapoda", "Order");
        insertNodeBetween("Root", "Brachyura", "Decapoda", "Order");
        insertNodeBetween("Root", "Corophiida", "Amphipoda", "Order");

        insertNodeBetween("Root", "Galatheoidea", "Anomura", "Infraorder");

        insertNodeBetween("Root", "Spionida", "Canalipalpata", "Order");
        insertNodeBetween("Root", "Sabellida", "Canalipalpata", "Order");
        insertNodeBetween("Root", "Terebellida", "Canalipalpata", "Order");
        insertNodeBetween("Root", "Doridina", "Nudibranchia", "Order");
        insertNodeBetween("Root", "Cladobranchia", "Nudibranchia", "Order");
        insertNodeBetween("Root", "Arminoidea", "Cladobranchia", "Suborder");
        insertNodeBetween("Root", "Dendronotoidea", "Cladobranchia", "Suborder");
        insertNodeBetween("Root", "Tritonioidea", "Cladobranchia", "Suborder");
        insertNodeBetween("Root", "Amphilepidida", "Ophintegrida", "Superorder");
        insertNodeBetween("Root", "Ophiacanthida", "Ophintegrida", "Superorder");
        insertNodeBetween("Root", "Euryalida", "Euryophiurida", "Superorder");


        insertNodeBetween("Root", "Tetraodontiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Lophiiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Batrachoidiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Dactylopteriformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Perciformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Centrarchiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Pleuronectiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Gobiesociformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Callionymiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Gobiiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Aulopiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Mulliformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Blenniiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Holocentriformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Kurtiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Carangiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Scombriformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Acanthuriformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Elopiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Albuliformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Beloniformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Siluriformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Syngnathiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Anguilliformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Eupercaria", "Teleostei", "Class");
        insertNodeBetween("Root", "Acropomatiformes", "Teleostei", "Class");
        insertNodeBetween("Root", "Carangaria", "Teleostei", "Class");
        insertNodeBetween("Root", "Ovalentaria", "Teleostei", "Class");
        insertNodeBetween("Root", "Canalipalpata", "Polychaeta", "Class");
        insertNodeBetween("Root", "Eunicida", "Polychaeta", "Class");
        insertNodeBetween("Root", "Phyllodocida", "Polychaeta", "Class");
        insertNodeBetween("Root", "Nudibranchia", "Euthyneura", "Infraclass");
        insertNodeBetween("Root", "Sacoglossa", "Euthyneura", "Infraclass");
        insertNodeBetween("Root", "Cephalaspidea", "Euthyneura", "Infraclass");
        insertNodeBetween("Root", "Euthyneura", "Heterobranchia", "Subclass");
        insertNodeBetween("Root", "Architectonicoidea", "Heterobranchia", "Subclass");
        insertNodeBetween("Root", "Heterobranchia", "Gastropoda", "Class");
        insertNodeBetween("Root", "Trochida", "Vetigastropoda", "Class");
        insertNodeBetween("Root", "Vetigastropoda", "Gastropoda", "Class");
        insertNodeBetween("Root", "Acteonimorpha", "Euthyneura", "Infraclass");
        insertNodeBetween("Root", "Aplysiida", "Euthyneura", "Infraclass");
        insertNodeBetween("Root", "Pteropoda", "Euthyneura", "Infraclass");
        insertNodeBetween("Root", "Pleurobranchida", "Euthyneura", "Infraclass");
        insertNodeBetween("Root", "Myopsida", "Cephalopoda", "Class");
        insertNodeBetween("Root", "Idiosepida", "Cephalopoda", "Class");
        insertNodeBetween("Root", "Sepiida", "Cephalopoda", "Class");
        insertNodeBetween("Root", "Octopoda", "Cephalopoda", "Class");
        insertNodeBetween("Root", "Cardiida", "Bivalvia", "Class");
        insertNodeBetween("Root", "Ostreida", "Bivalvia", "Class");
        insertNodeBetween("Root", "Pectinida", "Bivalvia", "Class");
        insertNodeBetween("Root", "Limida", "Bivalvia", "Class");
        insertNodeBetween("Root", "Euryophiurida", "Ophiuroidea", "Class");
        insertNodeBetween("Root", "Ophintegrida", "Ophiuroidea", "Class");
        insertNodeBetween("Root", "Chitonida", "Polyplacophora", "Class");
        insertNodeBetween("Root", "Heteronemertea", "Pilidiophora", "Class");
        insertNodeBetween("Root", "Scleralcyonacea", "Octocorallia", "Class");
        insertNodeBetween("Root", "Malacalcyonacea", "Octocorallia", "Class");
        insertNodeBetween("Root", "Scleralcyonacea", "Octocorallia", "Class");
        insertNodeBetween("Root", "Dendrochirotida", "Holothuroidea", "Class");
        insertNodeBetween("Root", "Apodida", "Holothuroidea", "Class");
        insertNodeBetween("Root", "Holothuriida", "Holothuroidea", "Class");
        insertNodeBetween("Root", "Synallactida", "Holothuroidea", "Class");
        insertNodeBetween("Root", "Camarodonta", "Echinoidea", "Class");
        insertNodeBetween("Root", "Cidaroida", "Echinoidea", "Class");
        insertNodeBetween("Root", "Diadematoida", "Echinoidea", "Class");
        insertNodeBetween("Root", "Echinothurioida", "Echinoidea", "Class");
        insertNodeBetween("Root", "Spatangoida", "Echinoidea", "Class");
        insertNodeBetween("Root", "Valvatida", "Asteroidea", "Class");
        insertNodeBetween("Root", "Paxillosida", "Asteroidea", "Class");
        insertNodeBetween("Root", "Spinulosida", "Asteroidea", "Class");
        insertNodeBetween("Root", "Rhizostomeae", "Scyphozoa", "Class");
        insertNodeBetween("Root", "Semaeostomeae", "Scyphozoa", "Class");
        insertNodeBetween("Root", "Coronatae", "Scyphozoa", "Class");
        insertNodeBetween("Root", "Comatulida", "Crinoidea", "Class");
        insertNodeBetween("Root", "Balanomorpha", "Thecostraca", "Class");
        insertNodeBetween("Root", "Pantopoda", "Pycnogonida", "Class");
        insertNodeBetween("Root", "Xiphosurida", "Merostomata", "Class");
        insertNodeBetween("Root", "Lobata", "Tentaculata", "Class");
        insertNodeBetween("Root", "Cestida", "Tentaculata", "Class");
        insertNodeBetween("Root", "Platyctenida", "Tentaculata", "Order");



        insertNodeBetween("Root", "Myliobatiformes", "Batoidea", "Infraclass");
        insertNodeBetween("Root", "Carcharhiniformes", "Selachii", "Infraclass");
        insertNodeBetween("Root", "Orectolobiformes", "Selachii", "Infraclass");
        insertNodeBetween("Root", "Batoidea", "Elasmobranchii", "Class");
        insertNodeBetween("Root", "Selachii", "Elasmobranchii", "Class");
        insertNodeBetween("Root", "Orectolobiformes", "Elasmobranchii", "Class");
        insertNodeBetween("Root", "Torpediniformes", "Batoidea", "Infraclass");
        insertNodeBetween("Root", "Testudines", "Reptilia", "Class");
        insertNodeBetween("Root", "Serpentes", "Reptilia", "Class");
        insertNodeBetween("Root", "Cetacea", "Cetartiodactyla", "Order");
        insertNodeBetween("Root", "Cetartiodactyla", "Mammalia", "Class");
        insertNodeBetween("Root", "Sirenia", "Mammalia", "Class");
        insertNodeBetween("Root", "Decapoda", "Malacostraca", "Class");
        insertNodeBetween("Root", "Stomatopoda", "Malacostraca", "Class");
        insertNodeBetween("Root", "Amphipoda", "Malacostraca", "Class");
        insertNodeBetween("Root", "Malacostraca", "Crustacea", "Subphylum");
        insertNodeBetween("Root", "Mysida", "Crustacea", "Subphylum");
        insertNodeBetween("Root", "Cirripedia", "Crustacea", "Subphylum");
        insertNodeBetween("Root", "Thecostraca", "Crustacea", "Subphylum");
        insertNodeBetween("Root", "Isopoda", "Malacostraca", "Subphylum");
        insertNodeBetween("Root", "Mysida", "Malacostraca", "Subphylum");
        insertNodeBetween("Root", "Littorinimorpha", "Caenogastropoda", "Subclass");
        insertNodeBetween("Root", "Caenogastropoda", "Gastropoda", "Class");
        insertNodeBetween("Root", "Neogastropoda", "Caenogastropoda", "Class");
        insertNodeBetween("Root", "Leptothecata", "Hydrozoa", "Class");
        insertNodeBetween("Root", "Siphonophorae", "Hydrozoa", "Class");
        insertNodeBetween("Root", "Anthoathecata", "Hydrozoa", "Class");
        insertNodeBetween("Root", "Zoantharia", "Hexacorallia", "Class");
        insertNodeBetween("Root", "Actiniaria", "Hexacorallia", "Class");
        insertNodeBetween("Root", "Ceriantharia", "Hexacorallia", "Class");
        insertNodeBetween("Root", "Corallimorpharia", "Hexacorallia", "Class");
        insertNodeBetween("Root", "Scleractinia", "Hexacorallia", "Class");
        insertNodeBetween("Root", "Antipatharia", "Hexacorallia", "Class");
        insertNodeBetween("Root", "Tentaculata", "Ctenophora", "Phylum");
        insertNodeBetween("Root", "Tentaculata", "Ctenophora", "Phylum");
        insertNodeBetween("Root", "Asteroidea", "Asterozoa", "Subphylum");
        insertNodeBetween("Root", "Ophiuroidea", "Asterozoa", "Subphylum");
        insertNodeBetween("Root", "Asterozoa", "Echinodermata", "Phylum");
        insertNodeBetween("Root", "Crinoidea", "Crinozoa", "Subphylum");
        insertNodeBetween("Root", "Crinozoa", "Echinodermata", "Phylum");
        insertNodeBetween("Root", "Echinoidea", "Echinozoa", "Subphylum");
        insertNodeBetween("Root", "Holothuroidea", "Echinozoa", "Subphylum");
        insertNodeBetween("Root", "Echinozoa", "Echinodermata", "Phylum");
        insertNodeBetween("Root", "Ascidiacea", "Tunicata", "Subphylum");
        insertNodeBetween("Root", "Thaliacea", "Tunicata", "Subphylum");
        insertNodeBetween("Root", "Gymnolaemata", "Bryozoa", "Phylum");
        insertNodeBetween("Root", "Demospongiae", "Porifera", "Phylum");
        insertNodeBetween("Root", "Homoschleromorpha", "Porifera", "Phylum");
        insertNodeBetween("Root", "Calcarea", "Porifera", "Phylum");
        insertNodeBetween("Root", "Merostomata", "Chelicerata", "Subphylum");
        insertNodeBetween("Root", "Pycnogonida", "Chelicerata", "Subphylum");
        insertNodeBetween("Root", "Pilidiophora", "Nemertea", "Phylum");

        insertNodeBetween("Root", "Teleostei", "Actinopteri", "Superclass");
        insertNodeBetween("Root", "Actinopteri", "Actinopterygii", "Gigaclass");
        insertNodeBetween("Root", "Actinopterygii", "Osteichthyes", "Parvphylum");
        insertNodeBetween("Root", "Elasmobranchii", "Chondrichthyes", "Parvphylum");
        insertNodeBetween("Root", "Osteichthyes", "Chordata", "Phylum");
        insertNodeBetween("Root", "Chondrichthyes", "Chordata", "Phylum");
        insertNodeBetween("Root", "Reptilia", "Chordata", "Phylum");
        insertNodeBetween("Root", "Mammalia", "Chordata", "Phylum");
        insertNodeBetween("Root", "Tunicata", "Chordata", "Phylum");
        insertNodeBetween("Root", "Chordata", "Animalia", "Kingdom");
        insertNodeBetween("Root", "Crustacea", "Arthropoda", "Phylum");
        insertNodeBetween("Root", "Chelicerata", "Arthropoda", "Phylum");
        insertNodeBetween("Root", "Arthropoda", "Animalia", "Kingdom");
        insertNodeBetween("Root", "Polycladida", "Platyhelminthes", "Phylum");
        insertNodeBetween("Root", "Acoela", "Xenacoelomorpha", "Phylum");
        insertNodeBetween("Root", "Amphinomida", "Annelida", "Phylum");
        insertNodeBetween("Root", "Platyhelminthes", "Animalia", "Kingdom");
        insertNodeBetween("Root", "Xenacoelomorpha", "Animalia", "Kingdom");
        insertNodeBetween("Root", "Annelida", "Animalia", "Kingdom");
        insertNodeBetween("Root", "Polychaeta", "Annelida", "Kingdom");
        insertNodeBetween("Root", "Gastropoda", "Mollusca", "Phylum");
        insertNodeBetween("Root", "Polyplacophora", "Mollusca", "Phylum");
        insertNodeBetween("Root", "Mollusca", "Animalia", "Kingdom");
        insertNodeBetween("Root", "Cephalopoda", "Mollusca", "Phylum");
        insertNodeBetween("Root", "Bivalvia", "Mollusca", "Phylum");
        insertNodeBetween("Root", "Scyphozoa", "Medusozoa", "Subphylum");
        insertNodeBetween("Root", "Hydrozoa", "Medusozoa", "Subphylum");
        insertNodeBetween("Root", "Hexacorallia", "Anthozoa", "Subphylum");
        insertNodeBetween("Root", "Octocorallia", "Anthozoa", "Subphylum");
        insertNodeBetween("Root", "Medusozoa", "Cnidaria", "Phylum");
        insertNodeBetween("Root", "Anthozoa", "Cnidaria", "Phylum");
        insertNodeBetween("Root", "Cnidaria", "Animalia", "Phylum");
        insertNodeBetween("Root", "Ctenophora", "Animalia", "Class");
        insertNodeBetween("Root", "Echinodermata", "Animalia", "Kingdom");
        insertNodeBetween("Root", "Bryozoa", "Animalia", "Kingdom");
        insertNodeBetween("Root", "Porifera", "Animalia", "Kingdom");
        insertNodeBetween("Root", "Phoronida", "Animalia", "Kingdom");
        insertNodeBetween("Root", "Nemertea", "Animalia", "Kingdom");
        insertNodeBetween("Root", "Animalia", "Eukaryota", "Domain");

        insertNodeBetween("Root", "Ulvophyceae", "Chlorophyta", "Division");
        insertNodeBetween("Root", "Chlorophyta", "Viridiplantae", "Subkingdom");
        insertNodeBetween("Root", "Viridiplantae", "Plantae", "Kingdom");
        insertNodeBetween("Root", "Plantae", "Eukaryota", "Domain");
        insertNodeBetween("Root", "Florideophyceae", "Rhodophyta", "Division");
        insertNodeBetween("Root", "Rhodophyta", "Biliphyta", "Subkingdom");
        insertNodeBetween("Root", "Biliphyta", "Plantae", "Kingdom");
        insertNodeBetween("Root", "Phaeophyceae", "Ochrophyta", "Phylum");
        insertNodeBetween("Root", "Ochrophyta", "Chromista", "Kingdom");
        insertNodeBetween("Root", "Chromista", "Eukaryota", "Domain");

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
    }

    public TreeNode<Taxon> buildTaxonomy() {
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
                String  status = json.get("status").toString();
                System.out.print(status  + "\t");
                sb.append(status).append("\t");
                String taxon = getRestReply("https://www.marinespecies.org/rest/AphiaClassificationByAphiaID/" + id);
                System.out.println(taxon);
                sb.append(taxon).append("\n");

            } catch(Exception e) {
                System.out.println("Species: " + sp + " UNKNOWN");
            }
        });
        try (FileWriter writer = new FileWriter("/tmp/worms.txt")) {
            writer.write(sb.toString());
        }
    }


    public static void main(String[] args) throws Exception {

        SpeciesTree speciesTree = new SpeciesTree();
        speciesTree.buildTaxonomy();
        // speciesTree.printTree(speciesTree.root, "");

        System.out.println();
        System.out.println();
        speciesTree.sortTreeByName(speciesTree.root);
        String tree = speciesTree.displayTree(speciesTree.depthFirstSearch(speciesTree.root, "Root"));
        System.out.println(tree);
        try (FileWriter writer = new FileWriter("/tmp/taxonomy_tree.txt")) {
            writer.write(tree);
        }

        speciesTree.exportTreeToCSV(speciesTree.root, "/tmp/species.csv");

        List<String> leafNames = speciesTree.getAlldangelingLeaf(speciesTree.root);
        leafNames.forEach(System.out::println);
        System.out.println();
        System.out.println();

        System.exit(0);

        speciesTree.worms();


    }

}
