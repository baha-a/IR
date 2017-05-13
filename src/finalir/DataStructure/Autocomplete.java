package finalir.DataStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Autocomplete {
    
    private int top = 3;
    private TreeSet<String> memory;
        
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
        
        if(t.length() <= 2)
            return d;
        
        int top2 = top;
        for (String s : memory.tailSet(t,false)) {
            d.add(s);
            if(top2-- == 0)
                break;
        }
        
        return d;
    }
}
