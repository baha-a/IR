package finalir.DataStructure;

import java.util.*;

public class InvertedIndex {

    private Map<String, Map<Integer, DocumentTermEntry>> _index = new HashMap<>();
    private Map<Integer, Document> _docs = new HashMap<>();
    
    public Document AddDoc(String name,int length){
        Document d = new Document(name,length);
        _docs.put(d.getId(),d);
        return d;
    }
    
    public Document GetDoc(int id){
        return _docs.get(id);
    }
    
    public InvertedIndex AddTerm(String term, Document doc, int position, TermType type) {
        
        if (_index.containsKey(term) == false) {
            Map<Integer, DocumentTermEntry> e = new HashMap<>();
            e.put(doc.getId(), new DocumentTermEntry(doc,term).Add(position, type));
            _index.put(term, e);
        }
        else {
            Map<Integer, DocumentTermEntry> e =_index.get(term);
            if(e.containsKey(doc.getId()) == false)
                e.put(doc.getId(), new DocumentTermEntry(doc,term).Add(position, type));
            else
                e.get(doc.getId()).Add(position, type);
        }
        
        return this;
    }
    
    public int getDF(String term){
        return _index.get(term).size();
    }
    
    public int getTF(String term, Document doc) {
        return _index.get(term).get(doc.getId()).getLength();  // .getFrequency() ??
    }
    
    public int getSiqmaTF(String term) {
        int sum = 0;
        for (DocumentTermEntry d : _index.get(term).values()) 
            sum += d.getLength();                              // .getFrequency() ??
        return sum;
    }
    
    public int getAllDocumentsCount(){
        return _docs.size();
    }
    
    public double getAvaregeDocumentLength(){
        if(getAllDocumentsCount() == 0)
            return 0;
        
        double sum = 0;
        for (Document d : _docs.values())
            sum += d.getLength();
        
        return sum / getAllDocumentsCount();
    }
    
    public int GetMaxTF(Document doc){
        return _docs.get(doc.getId()).getMaxTF();
    }
    
    public Collection<DocumentTermEntry> searchFor(String term){
        return _index.get(term).values();
    }
}