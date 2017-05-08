package finalir;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Pair;
import finalir.DataStructure.*;
import static finalir.IR.Print;
import static finalir.IR.Print;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Google{

    Tokenizer toky;
    InvertedIndex index;
    Searcher searcher;
    
    public Google(){
        toky = new Tokenizer();
        index = new InvertedIndex();
        searcher = new Searcher(index);
    }
    
    private void indexing(String name,List<CoreLabel> words){
        Document d = index.AddDoc(name, words.size());
        int postion = 0;
        for (CoreLabel w : words)
            index.AddTerm(w.lemma(), d, postion++ , w.beginPosition(), TermType.Text);
    }
    
    private static int id = 0;
    public Google IndexText(String t){
        indexing("NotFile" + ++id, toky.getTokens(t));
        return this;
    }
    
    public Google IndexFile(File f) throws IOException{
        indexing(f.getName(), toky.getTokens(f));
        return this;
    }
    
    public Google IndexFiles(File[] files) throws IOException{
        int i = 0;
        for (File f : files){
            IndexFile(f);
            
            if(i++ % 100 == 0)
                Print((i * 100 / files.length) + "%");
        }
        
        Print((i * 100 / files.length) + " %");
        return this;
    }
    
    
    public Google ComputeTF_IDF() {
        index.ApplyTF_IDF();
        return this;
    }
    
    
    
        
    public List<DocumentResult> SearchQuery(String query){
        
        query = query.toLowerCase();
        
        List<DocumentTermEntry> r = new ArrayList<>();

        for (CoreLabel w : toky.getTokens(query))
            r = searcher.SearchOr(w.lemma(), r);
        
        return searcher.ranking(Document.convert(r), convertQueryToVector(toky.getTokens(query)));
    }
    
    public static void Pizza(){
        
        Google indx = new Google()
            .IndexText("the sun is yellow and the sky it is blue, the weather is wonderful sun green blue very white")
            .IndexText("the sun is yellow and very blue, not sky red eye")
            .IndexText("get your eyes over here wonderfully")
            .IndexText("this is the project to solve your information retrivel problems, enjoy it and have a good day")
            .IndexText("the sun is not yellow and the sky isn't realy red red red, the end eye")
            .ComputeTF_IDF();
        
        
        Print("AllDocumentsCount: \t"     +  indx.index.getCountOfDocuments());
        Print("AvaregeDocumentLength: \t" +  indx.index.getAvaregeDocumentLength());
        Print("SiqmaTF(\"sun\"): \t"      +  indx.index.getSiqmaTF("sun"));
        Print("DF(\"sun\"): \t\t"         +  indx.index.getDF("sun"));
        Print("SiqmaTF(\"red\"): \t"      +  indx.index.getSiqmaTF("red"));
        Print("TF(\"red\",GetDoc(1)): \t" +  indx.index.getTF("red",indx.index.GetDoc(1)));
        Print("DF(\"yellow\"): \t\t"      +  indx.index.getDF("yellow"));
        Print("MaxTF(GetDoc(0)): \t"      +  indx.index.GetMaxTF(indx.index.GetDoc(0)));

        Print("Stopwrods removed: \t"     + indx.toky.getRemovedWordsCount());
        
        
        Stopwatch st = new Stopwatch().Start();
        
        
        Print(" ---- 1 ---- ");
        Print(indx.searcher.search("orjkgogmkdsmg").size());
        Print(indx.searcher.search("SUN").size());
        Print(indx.searcher.search("the").size());
        Print(indx.searcher.search("problem").size());
                
        Print(" ---- 2 ---- ");
        Print(indx.searcher.SearchAnd("sun","blue").size());
        Print(indx.searcher.SearchAnd("sun","eye").size());
        Print(indx.searcher.SearchOr ("sun","eye").size()); 
        Print(indx.searcher.SearchNot("eye","sun").size());
        Print(indx.searcher.SearchNot("sun","eye").size());
        Print(indx.searcher.SearchNot("sun","red").size());
        Print(indx.searcher.SearchAnd("sky","blue").size());
        Print(indx.searcher.SearchNot(indx.searcher.SearchAnd("sky","blue"), "red").size());
        Print(" ---- 3 ---- ");
        Print(indx.searcher.SearchAnd("sky",indx.searcher.SearchAnd("red", "yellow")).size());
        Print(indx.searcher.SearchAnd("sky",indx.searcher.SearchAnd("red", indx.searcher.SearchAnd("eye", "yellow"))).size());
        Print(indx.searcher.SearchAnd("sky", "red", "yellow").size());
        Print(indx.searcher.SearchAnd("sky", "blue","sun", "yellow").size());
        Print(indx.searcher.SearchAnd("boy", "blue","sun", "yellow").size());
        Print(indx.searcher.SearchOr ("sky", "blue","sun", "yellow").size());
        Print(indx.searcher.SearchOr ("red", "problem").size());
        
        Print(" ---- 4 ---- ");
        Print(indx.searcher.SearchNear(2,"sky","blue").size());
        Print(indx.searcher.SearchNear(1,"sky","eye").size());
        
        Print(indx.searcher.SearchNear(1,"red","red","red").size());
        Print(indx.searcher.SearchNear(1,"red","red","red","end").size());
        Print(indx.searcher.SearchNear(1,"realy","blue","red","red","red","end").size());
        Print(indx.searcher.SearchNear(1,"sky","realy","blue","red","red","red","end").size());
        Print(indx.searcher.SearchNear(1,"sky","realy","red","red","red","end").size());
        
        
        IR.Print(""+ st.Stop().GetMilisec());
        for(;;)
            for (DocumentResult d : indx.SearchQuery(new Scanner(System.in).nextLine()))
                Print(d.getDocument().getName() + " --> " + d.getRank());
    }
     
    
    private List<QueryTerm> convertQueryToVector(List<CoreLabel> tokens) {   
       List<QueryTerm> query = new ArrayList<>();

       double max = (1.0 / tokens.size());
       
       for (CoreLabel c : tokens)
       {
           boolean found = false;
           for (QueryTerm p : query)
           {
               if(p.term.equals(c.lemma()))
               {
                   p.value += (1.0 / tokens.size());
                   if(p.value > max)
                       max = p.value;
                   found = true;
                   break;
               }
           }
           if(found == false)
             query.add(new QueryTerm(c.lemma(), (1.0 / tokens.size())));
       }
       
        for (int j = 0; j < query.size(); j++)
            query.get(j).value = ((0.5 + 0.5 * query.get(j).value / max) * 
                    (Math.log(index.getCountOfDocuments() / index.getDF(query.get(j).term))));
        
        return query;
    }
}

class QueryTerm {
    public String term;
    public double value;
    public QueryTerm(String t,double v) {
        term = t;
        value = v;
    }
}