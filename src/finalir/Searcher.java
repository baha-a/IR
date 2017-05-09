package finalir;

import finalir.DataStructure.*;
import static java.lang.Math.sqrt;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class Searcher {
    
    private InvertedIndex index;
    
    public Searcher(InvertedIndex indx){
        index = indx;
    }
    
    
    
    private List<DocumentTermEntry> Or(List<DocumentTermEntry> c1,List<DocumentTermEntry> c2){
        return Or(c1,c2,0);
    }
    
    private List<DocumentTermEntry> Or(List<DocumentTermEntry> c1,List<DocumentTermEntry> c2,int target){
        List<DocumentTermEntry> union = new ArrayList<>(c1);
        
        for (DocumentTermEntry d : c2)
        {
            boolean fond = false;
            for (DocumentTermEntry t : c1)
            {
                if(d.getDocument().getId() == t.getDocument().getId())
                    fond = true;
            }
            if(fond == false)
                union.add(d);
        }   
        return union;
    }
    
    
    
    private List<DocumentTermEntry> And(List<DocumentTermEntry> c1,List<DocumentTermEntry> c2){
        return And(c1,c2,0);
    }

    private List<DocumentTermEntry> And(List<DocumentTermEntry> c1,List<DocumentTermEntry> c2,int target){
        
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

    
    
    
    public List<DocumentTermEntry> multiwordsSearch(String[] terms,Callable<List<DocumentTermEntry>> method){ 
        return multiwordsSearch(terms,method,0);
    }
    
    public List<DocumentTermEntry> multiwordsSearch(String[] terms,Callable<List<DocumentTermEntry>> method, int target){
        if(terms == null || terms.length < 1)
            return new ArrayList<>();
        else if(terms.length == 1)
            return new ArrayList<>(search(terms[0]));
        
        List<DocumentTermEntry> res = method.call(search(terms[0]),search(terms[1]),target);
        for (int i = 2; i < terms.length; i++)
            res = method.call(res,search(terms[i]),target);
        return res;
    }

    
    
    
    public List<DocumentTermEntry> SearchOr(String... terms){                   // make this private
        return multiwordsSearch(terms, (x,y,z) -> { return Or(x, y); });
    }
    
    public List<DocumentTermEntry> SearchOr(String term1,List<DocumentTermEntry> d){      // make this private
        return Or(search(term1), d);
    }
    
    public List<DocumentTermEntry> SearchAnd(String... terms){                   // make this private
        return multiwordsSearch(terms, (x,y,z) -> { return And(x, y); });
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
        return multiwordsSearch(terms, (x, y, z)-> { return Near(x, y, z); }, target);
    }
    
    public List<DocumentTermEntry> SearchNear(int target, String term2, List<DocumentTermEntry> d){   // make this private
        return Near(d, search(term2), target);
    }

	
    public static List<Document> convertDocument(List<DocumentTermEntry> t){
        List<Document> docs = new ArrayList<>();
        t.forEach(d -> docs.add(d.getDocument()));
        return docs;
    }
    
 
        
    public List<DocumentResult> ranking(List<Document> list, List<QueryTerm> query) {
        List<DocumentResult> result = new ArrayList<>();
        double c ;
        for (Document d : list)
            result.add(new DocumentResult(d, cosine(d,query)));
        return result;
    }
    
    
    private double cosine(Document d, List<QueryTerm> v2) {
        double[] q1 = new double[v2.size()];
        double[] q2 = new double[v2.size()];
        
        
        double[] q3 = d.getTfIdfVector();

        for (int i = 0; i < q1.length; i++) {
            q2[i] = v2.get(i).value;
            
            if(d.Contains(v2.get(i).term))
                q1[i] = d.getDocTermEntry(v2.get(i).term).getTfIDF();
        }
        
        return cosine(q1,q2,q3);
    }
    
    private double cosine(double[] v1, double[] v2,double[] v11) {
        double t = normF(v11) * normF(v2);
        if (t <= 0)
            return 0;
        return dot(v1, v2) / t;
    }
    
    private double normF(double[] v) {
        double res = 0;
        for (double d : v)
            res += d * d;
        return sqrt(res);
    }
    
    private double dot(double[] v1, double[] v2) {
        double score = 0;
        for (int i = 0; i < v1.length; i++)
            score += v1[i] * v2[i];
        
        return score;
    }
}