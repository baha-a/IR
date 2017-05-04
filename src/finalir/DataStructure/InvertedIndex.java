package finalir.DataStructure;

import java.util.*;

public class InvertedIndex {

    Map<String, Map<Integer, DocumentEntry>> _index;

    public InvertedIndex() {
        _index = new HashMap<>();
    }

    public InvertedIndex AddToTerm(String term, Document doc, int position, TermType type) {
        if (_index.containsKey(term) == false) {
            
            _index.put(term, new HashMap<>())
                    .put(doc.Id, new DocumentEntry(doc))
                    .Add(position, type);
        }
        else 
            _index.get(term).get(doc.Id).Add(position, type);
        
        return this;
    }
    
}

class DocumentEntry {
    Document document;
    ArrayList<Occurrence> occurrence;

    public DocumentEntry(Document doc) {
        occurrence = new ArrayList<>();
        document = doc;
    }

    public DocumentEntry Add(int position, TermType t) {
        occurrence.add(new Occurrence(position, t));   //   sort this arraylist late
        return this;
    }
}

class Occurrence {
    int position;
    TermType type;

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
}

enum TermType {
    Titel,
    Author,
    Text,
}

class Document {
    static int AvaregeDocumentLength;
    static int AllDocumentsCount;
    int maxTF;
    int length;

    int Id;
    String name;
    String filePath;
    String fileExtension;

    static int counter = 0;

    public Document() { Id = counter++; }
}