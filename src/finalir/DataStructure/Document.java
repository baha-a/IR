package finalir.DataStructure;

import java.util.ArrayList;
import java.util.List;

public class Document {
    private List<DocumentTermEntry> docTermEntry;
    private int id;
    private String name;
    private int length;

    public int maxTF = 0;
    
    private static int counter = 0;
    
    public Document(String name, int length) {
        this.id = counter++; 
        this.name = name;
        this.length = length;
        docTermEntry = new ArrayList<>();
    }
    
    public Document addDocumentTermEntry(DocumentTermEntry d){
        docTermEntry.add(d);
        return this;
    }
    
    public List<DocumentTermEntry> getDocumentTermEntryList(){
        return docTermEntry;
    }
        
    public int getId(){
        return id;
    }
    
    public String getName(){
        return name;
    }
    
    public int getLength(){
        return length;
    }
    
    public Document setMaxTF(int newTf){
        maxTF = newTf;
        return this;
    }
    
    public int getMaxTF(){
        return maxTF;
    }
    
    
        
    public int compareWith(Document d){
        return Math.abs(id - d.id);
    }
}