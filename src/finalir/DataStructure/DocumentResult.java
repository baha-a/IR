package finalir.DataStructure;

public class DocumentResult {
    private Document doc;
    private double rank;
    
    public DocumentResult(Document d,double r){
        doc = d;
        rank = r;
    }
    
    public Document getDocument(){
        return doc;
    }
    
    public double getRank(){
        return rank;
    }
}
