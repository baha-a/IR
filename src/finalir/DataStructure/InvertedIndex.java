package finalir.DataStructure;

import java.util.*;

public class InvertedIndex {

    private Map<String, Map<Integer, DocumentEntry>> _index;
    private List<Document> _docs;
    
    public InvertedIndex() {
        _index = new HashMap<>();
        _docs = new ArrayList<>();
    }
    
    public Document AddDoc(String name,int length){
        Document d = new Document(name,length);
        _docs.add(d);
        return d;
    }
    
    public InvertedIndex Add(String term, Document doc, int position, TermType type) {
        if (_index.containsKey(term) == false) {
            _index.put(term, new HashMap<>())
                    .put(doc.getId(), new DocumentEntry(doc))
                    .Add(position, type);
        }
        else 
            _index.get(term).get(doc.getId()).Add(position, type);
        
        return this;
    }
    
    public int getDF(String term){
        return _index.get(term).size();
    }
    
    public int getTF(String term, Document doc) {
        return _index.get(term).get(doc.getId()).getOccurrencesCount();
    }
    
    public int getSiqmaTF(String term) {
        int sum = 0;
        for (DocumentEntry d : _index.get(term).values()) 
            sum += d.getOccurrencesCount();
        return sum;
    }
    
    public int getAllDocumentsCount(){
        return _docs.size();
    }
    
    public double getAvaregeDocumentLength(){
        int sum = 0;
        for (Document d : _docs)
            sum += d.getLength();
        
        return sum * 1.0 / getAllDocumentsCount();
    }
}


class DocumentEntry {
    private Document document;
    private ArrayList<Occurrence> occurrence;
    private double frequencyMatrix;
    
    public DocumentEntry(Document doc) {
        occurrence = new ArrayList<>();
        document = doc;
    }

    public DocumentEntry Add(int position, TermType t) {        
        occurrence.add(getIndexToAddSorted(position), new Occurrence(position, t));
        upadteDocumentMaxTF();
        return this;
    }
    
    private int getIndexToAddSorted(int value)
    {
        int i = 0;
        while(i < occurrence.size() && occurrence.get(i).getPosition() < value)
            i++;
        return i;
    }
    
    private void upadteDocumentMaxTF(){
        if(occurrence.size() > document.getMaxTF())
            document.setMaxTF(occurrence.size());
    }
    
    public int getOccurrencesCount(){
        return occurrence.size();
    }
    
    public double getFrequencyValue(){
        // ????? from Basel's Code
        return getOccurrencesCount() * (1.0 / document.getLength());
    }
}

class Occurrence {
    private int position;
    private TermType type;

    public Occurrence(int pos, TermType t) {
        position = pos;
        type = t;
    }

    public int getPosition() {
        return position;
    }

    public TermType getType() {
        return type;
    }
        
    public int getTypeWeight() {
        switch(type)
        {
            case Titel:       return 10;
            case Author:      return 7;
            case Description: return 4;
        }
        return 1;
    }
}

enum TermType {
    Titel,
    Author,
    Description,
    Text,
}

class Document {
    private int id;
    private String name;
    private int length;

    public int maxTF = 0;
    
    private static int counter = 0;
    public Document() {
        this.id = counter++; 
    }
    
    public Document(String name, int length) {
        this.id = counter++; 
        this.name = name;
        this.length = length;
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
}