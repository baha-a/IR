package finalir.DataStructure;

import java.util.ArrayList;
import java.util.List;

public class DocumentResult {
    private Document doc;
    private double rank;
    
    private List<Integer> positions;
    
    public DocumentResult(Document d,double r){
        doc = d;
        rank = r;
        positions = new ArrayList<>();
    }
    
    public Document getDocument(){
        return doc;
    }
    
    public double getRank(){
        return rank;
    }
    
    public List<Integer> getPositions(){
        return positions;
    }
    
    public DocumentResult addPosition(String t){
        if(doc != null)
            positions.add(doc.getFirstPosition(t));
        
        return this;
    }
}
