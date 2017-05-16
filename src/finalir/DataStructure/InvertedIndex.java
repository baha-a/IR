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
    
    public boolean Contains(String trm){
        return _index.containsKey(trm);
    }
    
    public int getTermCount() {
        return _index.size();
    }
    
    public InvertedIndex AddTerm(String term, Document doc, int position,int exactpostion, TermType type) {
        term = term.toLowerCase();
        if (_index.containsKey(term) == false) {
            Map<Integer, DocumentTermEntry> e = new HashMap<>();
            e.put(doc.getId(), new DocumentTermEntry(doc,term).Add(position,exactpostion, type));
            _index.put(term, e);
        }
        else {
            Map<Integer, DocumentTermEntry> e =_index.get(term);
            if(e.containsKey(doc.getId()) == false)
                e.put(doc.getId(), new DocumentTermEntry(doc,term).Add(position,exactpostion, type));
            else
                e.get(doc.getId()).Add(position,exactpostion, type);
        }
        
        return this;
    }
    
    public int getDF(String term){
        term = term.toLowerCase();
        if(_index.get(term) == null)
            return 0;
        return _index.get(term).size();
    }
    
    public double getTF(String term, Document doc) {
        term = term.toLowerCase();
        if(_index.containsKey(term) && _index.get(term).containsKey(doc.getId()))
            return _index.get(term).get(doc.getId()).getTf();
        return 0;
    }
    
    public double getSiqmaTF(String term) {
        term = term.toLowerCase();
        double sum = 0;
        for (DocumentTermEntry d : _index.get(term).values()) 
            sum += d.getTf();
        return sum;
    }
    
    public int getCountOfDocuments(){
        return _docs.size();
    }
    
    public double getAvaregeDocumentLength(){
        if(getCountOfDocuments() == 0)
            return 0;
        
        double sum = 0;
        for (Document d : _docs.values())
            sum += d.getLength();
        
        return sum / getCountOfDocuments();
    }
    
    public int GetMaxTF(Document doc){
        return _docs.get(doc.getId()).getMaxTF();
    }
    
    public List<DocumentTermEntry> search(String term){
        term = term.toLowerCase();
        if(_index.containsKey(term))
            return new ArrayList<>(_index.get(term).values());
        return new ArrayList<>();     // insted of 'null' to avoid Exeption
    }
    
    
    public InvertedIndex ApplyTF_IDF(){
        for (Map<Integer, DocumentTermEntry> m : _index.values())
            for (DocumentTermEntry d : m.values()){
                d.setTfIDF( (d.getTf() / d.getDocumentMaxTf()) * Math.log( getCountOfDocuments() * 1.0 / m.size() ) );
            }
        return this;
    }
}