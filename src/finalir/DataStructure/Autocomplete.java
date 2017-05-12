package finalir.DataStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Autocomplete {
    
    private int top = 10;
    private TreeSet<String> memory;   //  make this TreeMap<String, Integer>
        
    public Autocomplete(){
        memory = new TreeSet<>();
    }
    
    public Autocomplete(int top){
        memory = new TreeSet<>();
        this.top = top;
    }
    
    public String save(String t){
        memory.add(t);
        return t;
    }
    
    public List<String> suggest(String t){
        List<String> d = new ArrayList<>();
        
        int top2 = top;
        for (String s : memory.tailSet(t,true)) {
            if(s.equals(t))
                continue;
            d.add(s);
            if(top2-- == 0)
                break;
        }
        
        return d;
    }
}
