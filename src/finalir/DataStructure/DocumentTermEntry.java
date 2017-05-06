package finalir.DataStructure;

import java.util.ArrayList;

public class DocumentTermEntry {
    private String term;
    private Document document;
    private ArrayList<Observation> observation;
    
    public DocumentTermEntry(Document doc,String trm) {
        observation = new ArrayList<>();
        document = doc;
        term = trm;
    }

    public String getTerm(){
        return term;
    }
    
    public DocumentTermEntry Add(int position,int exactPosition, TermType t) {        
        //observation.add(getIndexToAddSorted(position), new Observation(position,exactPosition, t)); // add sorted
        observation.add(new Observation(position,exactPosition, t));  // ignore the sorting for now
        document.addDocumentTermEntry(this);
        upadteDocumentMaxTF();
        return this;
    }
    
    private int getIndexToAddSorted(int value){
        int i = 0; 
        while(i < observation.size() && observation.get(i).getPosition() < value)
            i++;
        return i;
    }
    
    private void upadteDocumentMaxTF(){
        if(observation.size() > document.getMaxTF())
            document.setMaxTF(observation.size());
    }
    
    public int getLength(){
        return observation.size();
    }
    
    public double getFrequency(){
        // ?????  this from Basel's Code
        return observation.size() * (1.0 / document.getLength());
    }
    
    public Document getDocument(){
        return document;
    }
    
    public int getDistance(DocumentTermEntry d, int target){

        for (Observation p1 : observation) 
            for (Observation p2 : d.observation) 
                if( Math.abs(p1.getPosition() - p2.getPosition()) <= target)
                    return target;
        return Integer.MAX_VALUE;
    }
}
