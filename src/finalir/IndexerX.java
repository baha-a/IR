package finalir;

import edu.stanford.nlp.ling.CoreLabel;
import finalir.DataStructure.*;
import static finalir.IR.Print;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IndexerX{
    Tokenizer toky = new Tokenizer();
    InvertedIndex index = new InvertedIndex();
    
    static int id = 0;
    public IndexerX IndexText(String t){
        List<CoreLabel> words = toky.getTokens(t);
        Document d = index.AddDoc("NotFile" + id++, words.size());
        int postion = 0;
        for (CoreLabel w : words)
            index.AddTerm(w.lemma(), d, postion++ , w.beginPosition(), TermType.Text);
        
        return this;
    }
    
    public IndexerX IndexFile(File f) throws IOException{
        List<CoreLabel> words = toky.getTokens(f);
        Document d = index.AddDoc(f.getName(), words.size());
        int postion = 0;
        for (CoreLabel w : words)
            index.AddTerm(w.lemma(), d, postion++, w.beginPosition(), TermType.Text);
        
        return this;
    }
    
    public IndexerX IndexFiles(File[] files) throws IOException{
        int i = 0;
        for (File f : files){
            IndexFile(f);
            
            if(i++ % 100 == 0)
                Print((i * 100 / files.length) + "%");
        }
        
        Print((i * 100 / files.length) + " %");
        return this;
    }
    
    
    public void applyTF_IDF() {
        
//        for (int i = 0; i < frequencyMatrix.length; i++)
//            for (int j = 0; j < corpus.size(); j++)
//                vectors[i][j] = ((frequencyMatrix[i][j] / document.maxTF) * (Math.log(Documents.size() / dfi[j])));
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
    
    
    
    
    public static void Pizza(){
        IndexerX indx = new IndexerX()
            .IndexText("the sun is yellow and the sky it is blue, the weather is wonderful sun blue blue very blue")
            .IndexText("the sun is yellow and very blue")
            .IndexText("get your eyes over here wonderfully ")
            .IndexText("this is the project to solve your information retrivel problems, enjoy it and have a good day")
            .IndexText("the sun is not yellow and the sky isn't realy blue, red red red, the end");

        Print("AllDocumentsCount: \t" +  indx.index.getCountOfDocuments());
        Print("AvaregeDocumentLength: \t" +  indx.index.getAvaregeDocumentLength());
        Print("SiqmaTF(\"sun\"): \t" +  indx.index.getSiqmaTF("sun"));
        Print("DF(\"sun\"): \t\t" +  indx.index.getDF("sun"));
        Print("SiqmaTF(\"red\"): \t" +  indx.index.getSiqmaTF("red"));
        Print("TF(\"red\",GetDoc(1)): \t" +  indx.index.getTF("red",indx.index.GetDoc(1)));
        Print("DF(\"yellow\"): \t\t" +  indx.index.getDF("yellow"));
        Print("MaxTF(GetDoc(0)): \t" +  indx.index.GetMaxTF(indx.index.GetDoc(0)));

        Print("Stopwrods removed: \t" + indx.toky.getRemovedWordsCount());
        
        Print(" ---- 1 ---- ");
        Print("" + indx.index.search("orjkgojrgojsrkmlkgmkdsmg").size());
        Print("" + indx.index.search("SUN").size());
        Print("" + indx.index.search("the").size());
        Print("" + indx.index.search("problem").size());
                
        Print(" ---- 2 ---- ");
        Print("" + indx.SearchAnd("sun","blue").size());
        Print("" + indx.SearchAnd("sun","eye").size());
        Print("" + indx.SearchOr("sun","eye").size()); 
        Print("" + indx.SearchNot("eye","sun").size());
        Print("" + indx.SearchNot("sun","eye").size());
        Print("" + indx.SearchNot("sun","red").size());
        Print("" + indx.SearchAnd("sky","blue").size());
        Print("" + indx.SearchNot(indx.SearchAnd("sky","blue"), "red").size());
        Print("" +indx.SearchAnd("sky",indx.SearchAnd("red", "yellow")).size());        
        Print("" +indx.SearchAnd("sky",indx.SearchAnd("red", indx.SearchAnd("eye", "yellow"))).size());
    }
    
    public Collection<DocumentTermEntry> SearchQuery(String query){
        
        // to-do later: complete this method
        // first parse the query and execute
        //
        
        for (CoreLabel w : toky.getTokens(query))
            search(w.lemma());
        
        return null;
    }

    public List<Document> SearchOr(String term1,String term2){                  // make this private
        return Or(search(term1), search(term2));
    }
    
    public List<Document> SearchOr(String term1,List<Document> d){              // make this private
        return Or(search(term1), d);
    }
    
    public List<Document> SearchAnd(String term1,String term2){                 // make this private
        return And(search(term1),search(term2));
    }
    
    public List<Document> SearchAnd(String term1,List<Document> d){             // make this private
        return And(search(term1),d);
    }
    
    public List<Document> SearchNot(String term1,String term2){                 // make this private
        return Not(search(term1),search(term2));
    }
    
    public List<Document> SearchNot(String term1,List<Document> d){             // make this private
        return Not(search(term1),d);
    }
    
    public List<Document> SearchNot(List<Document> d,String term2){             // make this private
        return Not(d,search(term2));
    }
    
    private List<Document> Or(List<Document> c1,List<Document> c2){
        List<Document> unine = new ArrayList<>();
        
        for (Document d : c1)
            unine.add(d);
        for (Document d : c2)
            unine.add(d);
        
        return unine;
    }
    
    private List<Document> And(List<Document> c1,List<Document> c2){
        
        List<Document> intersection = new ArrayList<>();
        
        int p1 = 0 , p2 = 0, compare = 0;
        while(p1 < c1.size() && p2 < c2.size()){
            compare = c1.get(p1).compareWith(c2.get(p2));
            
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
    
    private List<Document> Not(List<Document> c1,List<Document> c2){
        
        List<Document> not = new ArrayList<>();
        
        for (Document d : c1)
            if(c2.contains(d) == false)
                not.add(d);
        
        return not;
    }
    
    private List<Document> search(String term){
        List<Document> docs = new ArrayList<>();
        index.search(term).forEach(d -> docs.add(d.getDocument()));
        return docs;
    }
}
