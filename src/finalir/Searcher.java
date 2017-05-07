package finalir;

import finalir.DataStructure.*;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.List;

public class Searcher {
    
    private InvertedIndex index;
    public Searcher(InvertedIndex indx){
        index = indx;
    }
    
    public double cosine(double[] v1, double[] v2) {
        double t = normF(v1) * normF(v2);
        if (t <= 0)
            return 0;
        return dot(v1, v2) / t;
    }

    public double dot(double[] v1, double[] v2) {
        double score = 0;
        for (int i = 0; i < v1.length; i++)
            score += v1[i] * v2[i];
        
        return score;
    }

    public double normF(double[] v) {
        double res = 0;
        for (double d : v)
            res += d * d;
        
        return sqrt(res);
    }
    
    public List<DocumentTermEntry> SearchOr(String term1,String term2){                   // make this private
        return Or(search(term1), search(term2));
    }
    
    public List<DocumentTermEntry> SearchOr(String term1,List<DocumentTermEntry> d){      // make this private
        return Or(search(term1), d);
    }
    
    public List<DocumentTermEntry> SearchAnd(String term1,String term2){                   // make this private
        return And(search(term1),search(term2));
    }
    
    public List<DocumentTermEntry> SearchAnd(String term1,List<DocumentTermEntry> d){       // make this private
        return And(search(term1),d);
    }
    
    public List<DocumentTermEntry> SearchNot(String term1,String term2){                    // make this private
        return Not(search(term1),search(term2));
    }
    
    public List<DocumentTermEntry> SearchNot(String term1,List<DocumentTermEntry> d){       // make this private
        return Not(search(term1),d);
    }
    
    public List<DocumentTermEntry> SearchNot(List<DocumentTermEntry> d,String term2){       // make this private
        return Not(d, search(term2));
    }
    
    
    public List<DocumentTermEntry> SearchNear(int target, String... terms){                 // make this private
        if(terms.length < 2)
            return new ArrayList<>();
        
        List<DocumentTermEntry> res = Near(search(terms[0]), search(terms[1]), target);
        for (int i = 2; i < terms.length; i++)
            res = Near(res, search(terms[i]), target);
        
        return res;
    }
    
    private List<DocumentTermEntry> Or(List<DocumentTermEntry> c1,List<DocumentTermEntry> c2){
        List<DocumentTermEntry> unine = new ArrayList<>();
        
        for (DocumentTermEntry d : c1)
            unine.add(d);
        for (DocumentTermEntry d : c2)
            unine.add(d);
        
        return unine;
    }
    
    private List<DocumentTermEntry> And(List<DocumentTermEntry> c1,List<DocumentTermEntry> c2){
        
        List<DocumentTermEntry> intersection = new ArrayList<>();
        
        int p1 = 0 , p2 = 0, compare = 0;
        while(p1 < c1.size() && p2 < c2.size()){
            compare = c1.get(p1).getDocument().compareWith(c2.get(p2).getDocument());
            
            if(compare > 0)
                p2++;
            else if(compare < 0)
                p1++;
            else
            {
                intersection.add(c1.get(p1));
                p1++;
                p2++;
            }
        }
        
        return intersection;
    }
    
    private List<DocumentTermEntry> Near(List<DocumentTermEntry> c1, List<DocumentTermEntry> c2,int target){
        
        List<DocumentTermEntry> near = new ArrayList<>();
        
        int p1 = 0 , p2 = 0, compare = 0;
        while(p1 < c1.size() && p2 < c2.size()){
            compare = c1.get(p1).getDocument().compareWith(c2.get(p2).getDocument());
            
            if(compare > 0)
                p2++;
            else if(compare < 0)
                p1++;
            else
            {
                if(c1.get(p1).getDistance(c2.get(p2),target) <= target)
                    near.add(c2.get(p2));
                p1++;
                p2++;
            }
        }
        
        return near;
    }
    
    private List<DocumentTermEntry> Not(List<DocumentTermEntry> c1, List<DocumentTermEntry> c2){
        
        List<DocumentTermEntry> not = new ArrayList<>();
        
        for (DocumentTermEntry d : c1)
            if(c2.contains(d) == false)
                not.add(d);
        
        return not;
    }
    
    public List<DocumentTermEntry> search(String term){
        return index.search(term);
    }
    
    public List<Document> convertDocument(List<DocumentTermEntry> t){
        List<Document> docs = new ArrayList<>();
        t.forEach(d -> docs.add(d.getDocument()));
        return docs;
    }
}
