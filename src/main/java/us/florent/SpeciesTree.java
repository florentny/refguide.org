package us.florent;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
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

import static java.util.Arrays.asList;

public class SpeciesTree {


    List<String> ranks = Arrays.asList("Domain", "Kingdom", "Subkingdom", "Division", "Phylum", "Subphylum", "Parvphylum", "Gigaclass", "Superclass", "Class", "Subclass",
            "Infraclass", "Subterclass", "Superorder", "Order", "Suborder", "Infraorder", "Superfamily", "Family", "Subfamily", "Genus", "Species");

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
            if(sp.getName().equals(name))
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

        List<Document> pipeline = asList(
                new Document("$addFields", new Document("rankOrder", new Document("$indexOfArray", asList(ranks, "$rank")))),
                new Document("$sort", new Document("rankOrder", 1))
        );
        // Run the aggregation
        AggregateIterable<Document> sortedDocs = collection.aggregate(pipeline);

        for(Document doc : sortedDocs) {
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

        // categories and taxon validation
        collection = db.getCollection("categories");
        for(Document doc : collection.find()) {
            String alttype = doc.get("alttype").toString();
            String name = doc.get("altclassification").toString();
            String family = doc.get("family").toString();
            String cat = doc.get("category").toString();
            String subfamily = doc.get("subfamily").toString();
            TreeNode<Taxon> familyNode = depthFirstSearch(root, family);
            if(!subfamily.isEmpty()) {
                TreeNode<Taxon> subfamilyNode = depthFirstSearch(root, subfamily);
                if(!subfamilyNode.getValue().getCategory().equals(cat)) {
                    throw new Exception("Subfamily category mismatch: " + subfamilyNode.getValue().getCategory() + " != " + cat);
                }

            } else {
                if(!familyNode.getValue().getCategory().equals(cat)) {
                    throw new Exception("Family category mismatch: " + familyNode.getValue().getCategory() + " != " + cat);
                }

            }
        }

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


    public void printNodeJson(TreeNode<Taxon> node, String parentName, StringBuilder jsonOutput) {
        JSONObject obj = new JSONObject();
        obj.put("name", node.getValue().getName());
        obj.put("rank", node.getValue().getRank());
        obj.put("category", node.getValue().getCategory());
        obj.put("parent", parentName);
        jsonOutput.append(obj.toString()).append("\n");

        for(TreeNode<Taxon> child : node.getChildren()) {
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
            out.append(species.genus.charAt(0)).append(". ").append(species.epithet).append(" -  ").append(species.getName()).append("\n");
        } else {
            String cat = (node.getValue().getCategory() == null) ? "" : " [" + node.getValue().getCategory() + "]";
            out.append(node.getValue().getName()).append(" (").append(node.getValue().getRank()).append(")").append(cat).append("\n");
        }

        for(int i = 0; i < node.children.size(); i++) {
            displayTree(node.children.get(i), indent, i == node.children.size() - 1, out);
        }
    }

    public void exportTreeToCSV(TreeNode<Taxon> root, String filename) throws IOException {
        List<String> cols = new ArrayList<>(ranks);
        cols.removeFirst();
        cols.add("Common Name");
        cols.add("Category");
        FileWriter writer = new FileWriter(filename);
        writer.write(String.join(",", cols) + "\n");

        List<String> path = new ArrayList<>(Collections.nCopies(cols.size(), ""));
        exportTreeToCSVHelper(root, path, cols, writer);
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

    public boolean isPathInRankOrder(List<Taxon> path) {
        int lastIndex = -1;
        for(Taxon taxon : path) {
            int idx = ranks.indexOf(taxon.getRank());
            if(idx == -1) return false; // Unknown rank
            if(idx <= lastIndex) return false; // Not in order
            lastIndex = idx;
        }
        return true;
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

    public List<Taxon> getPathToSpecies(String species) {
        Species sp = findSpecies(root, species);
        return getPathToSpecies(root, sp);
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

    void addAphiaIDB() throws Exception {
        try(BufferedReader br = new BufferedReader(new FileReader("worms.txt"))) {
            String line;
            while((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                //System.out.println("Adding AphiaID " + fields[1] + " to " + fields[0]);
                Species sp = findSpecies(root, fields[0]);
                sp.setAphiaID(Integer.parseInt(fields[1]));
                List<Taxon> list = getPathToSpecies(root, sp);
                if(!isPathInRankOrder(list)) {
                    System.out.println("Path for " + sp.getName() + " is not in rank order: " + list);
                    throw new Exception("Path for " + sp.getName() + " is not in rank order");
                }
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

        for(int i = 2; i < list1.size(); i++) {
            String name = list1.get(i).getSciName();
            String rank = list1.get(i).getRank();

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

        System.exit(0);

        StringBuilder out = new StringBuilder();
        speciesTree.printNodeJson(speciesTree.root, null, out);
        try(FileWriter writer = new FileWriter("/tmp/taxonomy_nodes.json")) {
            writer.write(out.toString());
        }

        speciesTree.worms();


    }

}
