package finalir.DataStructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Cache {
    private Map<String,List<DocumentResult>> memory;
    public Cache(){
        memory = new HashMap<>();
    }
    
    public Cache save(String t, List<DocumentResult> d){
        memory.put(t,d);
        return this;
    }
    
    public boolean check(String t){
        return memory.containsKey(t);
    }
    
    public List<DocumentResult> get(String t){
        return memory.get(t);
    }
    
    public Cache clear(){
        memory.clear();
        return this;
    }
}
