package finalir;

import edu.stanford.nlp.ling.CoreLabel;
import finalir.DataStructure.*;
import static finalir.IR.Print;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

public class Engine{

    Tokenizer toky;
    InvertedIndex index;
    Searcher searcher;
    Cache<DocumentResult> cache;
    Autocomplete completer;
    
    public Engine(){
        toky = new Tokenizer(true);
        index = new InvertedIndex();
        searcher = new Searcher(index);
        cache = new Cache<>();
        completer = new Autocomplete();
    }
    
    private void indexing(String name,List<CoreLabel> words){
        Document d = index.AddDoc(name, words.size());
        int postion = 0;
        for (CoreLabel w : words)
            index.AddTerm(w.lemma(), d, postion++ , w.beginPosition(), TermType.Text);
        
        cache.clear();
    }
    
    private static int id = 0;
    public Engine IndexText(String t){
        indexing("NotFile" + ++id, toky.getTokens(t));
        return this;
    }
    
    public Engine IndexFile(File f) throws IOException, TikaException{
        indexing(f.getName(), toky.getTokens(new Tika().parseToString(f)));
        return this;
    }
    
    public Engine IndexFiles(File[] files) throws IOException, TikaException{
        int i = 0;
        for (File f : files){
            IndexFile(f);
            
            if(i++ % 100 == 0)
                Print((i * 100 / files.length) + "%");
        }
        
        Print((i * 100 / files.length) + " %");
        return this;
    }
    
    
    public Engine ComputeTF_IDF() {
        index.ApplyTF_IDF();
        return this;
    }
    
    
    
    public List<DocumentResult> SearchQuery(String q,boolean advanceSearch) {
        Stopwatch st = new Stopwatch().Start();
        
        List<DocumentResult> res = searchQuery2(q, advanceSearch);
        
        st.Stop();
        Print(" TIME : " + st.GetMilisec() + " msec     RAM  : " + (st.GetMemoryUsage()/1024) + " Kbyte");
        lastTime = st.GetMilisec();
        return res;
    }
    private long lastTime;
    public long getLastTime(){ return lastTime;}
    
    private List<DocumentResult> searchQuery2(String q,boolean advanceSearch) {
        
        String query = q = q.toLowerCase();
        
        if(cache.check(q + advanceSearch))
            return cache.get(q + advanceSearch);
        
        for (CoreLabel w : toky.getTokens(query))
            for(String s : WordNet.getSynonyms(w.lemma()))
            {
                query += " " + s;
                break;
            }
        
        List<DocumentTermEntry> r = new ArrayList<>();

        if(advanceSearch)
            r = ParseQuery(q);
        else
            for (CoreLabel w : toky.getTokens(query))
                r = searcher.SearchOr(w.lemma(), r);
        
        
        List<DocumentResult> res = searcher.ranking(Document.convert(r), convertQueryToVector(toky.getTokens(query)));
        res.sort((DocumentResult d1, DocumentResult d2) -> 
        {
            if(d1.getRank() < d2.getRank()) return 1;
            else if(d1.getRank() > d2.getRank()) return -1;
            return 0;
        });
        
        cache.save(q + advanceSearch, res);
        completer.save(q);
        return res;
    }
    
    public String[] tokinzing(String t){
        List<String> res = new ArrayList<>();
        toky.getTokens(t).forEach(x -> res.add(x.lemma()));
        return res.toArray(new String[res.size()]);
    }
    
    private List<DocumentTermEntry> ParseQuery(String q){
        
        if(q.length() <= 1)
            return new ArrayList<>();
        q = q.trim();
        
        if(q.startsWith("\"") && q.endsWith("\""))
            return searcher.SearchNear(1, tokinzing(q));
        
        int arc = 0;
        boolean arcAppered = false;
        String operator = "";

        String leftWord = "";
        String rightWord = "";

        int i = 0;
        for (; i < q.length(); i++) 
        {
            if(q.charAt(i) == '('){
                if(arc != 0) leftWord += q.charAt(i);
                arc++;
                arcAppered = true;
            }
            else if(q.charAt(i) == ')'){
                arc--;
                if(arc != 0) leftWord += q.charAt(i);
            }
            else if(q.charAt(i) == ' ' || i == q.length()-1)
            {
                if(operator.equals("and") || operator.equals("or") || operator.equals("not"))
                    break;
                else
                {    
                    if(q.charAt(i) != ' ' && i == q.length()-1)
                        leftWord += operator + q.charAt(i) + " ";
                    else
                        leftWord += operator + " ";
                    operator = "";
                }
            }
            else if(arc == 0)
               operator += q.charAt(i);
            else
                leftWord += q.charAt(i);
        }


        if(arc == 0 && operator.isEmpty() && arcAppered)
            return ParseQuery(q.substring(1, q.length()-1));
        else if(arc == 0 && operator.isEmpty() && !arcAppered)
            return searcher.SearchOr(tokinzing(leftWord));
        else
        {
            if(i < q.length())
                rightWord = q.substring(i+1);
            
            if(operator.equals("and"))
                return searcher.And(ParseQuery(leftWord), ParseQuery(rightWord));
            else if(operator.equals("not"))
                return searcher.Not(ParseQuery(leftWord), ParseQuery(rightWord));
            return searcher.Or(ParseQuery(leftWord), ParseQuery(rightWord));
        }
    }
    
    
    public List<String> getSuggestions(String s){
        return completer.suggest(s.toLowerCase());
    }
    
    
    public static void Pizza(){
    
        Engine indx = new Engine()
            .IndexText("the sun is yellow and the  sky it is blue, the weather is wonderful sun green blue very white")
            .IndexText("the sun is yellow and very blue, not sky red eye")
            .IndexText("get your eyes over here  wonderfully")
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
        
        
        Print("Enter query:");
        for(int i = 0; i < 10; i++)
            for (DocumentResult d : indx.SearchQuery(new Scanner(System.in).nextLine(),true))
                Print(d.getDocument().getName() + " --> " + d.getRank());
    }
     
    
    private List<QueryTerm> convertQueryToVector(List<CoreLabel> tokens) {   
       List<QueryTerm> query = new ArrayList<>();

       double max = 1;
       
       for (CoreLabel c : tokens)
       {
           if(index.Contains(c.lemma()) == false)
               continue;
           
           boolean found = false;
           for (QueryTerm p : query)
           {
               if(p.term.equals(c.lemma()))
               {
                   p.value += 1;
                   if(p.value > max)
                       max = p.value;
                   found = true;
                   break;
               }
           }
           if(found == false)
             query.add(new QueryTerm(c.lemma(), 1));
       }
       
        for (int j = 0; j < query.size(); j++){
         query.get(j).value = ((query.get(j).value / max) * 
                    (Math.log(index.getCountOfDocuments() * 1.0 / index.getDF(query.get(j).term))));
        }
        return query;
    }
}