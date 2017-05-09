package finalir.DataStructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cache <T>{
    private Map<String,List<T>> memory;
    public Cache(){
        memory = new HashMap<>();
    }
    
    public List<T> save(String t, List<T> d){
        memory.put(t,d);
        return d;
    }
    
    public boolean check(String t){
        return memory.containsKey(t);
    }
    
    public List<T> get(String t){
        return memory.get(t);
    }
    
    public Cache clear(){
        memory.clear();
        return this;
    }
}
