package finalir;

import Form.EngineClient;
import finalir.DataStructure.*;
import finalir.Stem.Tokenizer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

public class Engine{

    InvertedIndex index;
    Tokenizer toky;
    Searcher searcher;
    Cache<DocumentResult> cache;
    Autocomplete completer;    
    LeskWSD lesker;
    
    
    public Engine(boolean stemmingOnly){
        toky = new Tokenizer();
        index = new InvertedIndex();
        searcher = new Searcher(index);
        cache = new Cache<>();
        completer = new Autocomplete();
        lesker = new LeskWSD();
    }
    
    
    public void indexingXml(String name,String path,String title,String author,String txt){
        
            Document d = index.AddDoc(name,path, title.length() + author.length() + txt.length());
            int position = 0;
            for (CoreLabel w : toky.getTokens(title))
                index.AddTerm(w.lemma(), d, position++ , w.beginPosition(), TermType.Titel);
            
            for (CoreLabel w : toky.getTokens(author))
                index.AddTerm(w.lemma(), d, position++ , w.beginPosition(), TermType.Author);
            
            for (CoreLabel w : toky.getTokens(txt))
                index.AddTerm(w.lemma(), d, position++ , w.beginPosition(), TermType.Text);
            
            cache.clear();
    }
    private void indexingXml(String name,String path,File f){
        try
        {
            XPath xPath = XPathFactory.newInstance().newXPath();
            org.w3c.dom.Document xml = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(f);

            //String id = xPath.compile("/article/@id").evaluate(xml).trim();
            
            String t = xPath.compile("/article/title").evaluate(xml).trim();
            String a = xPath.compile("/article/author").evaluate(xml).trim();
            String c = xPath.compile("/article/text").evaluate(xml).trim();

            indexingXml(name, path, t, a, c);
        }catch(Exception e){System.err.println("error reading xml file:" + name);}
    }
    
    private void indexingPlanText(String name,String path,List<CoreLabel> words){
        Document d = index.AddDoc(name,path, words.size());
        int postion = 0;
        for (CoreLabel w : words)
            index.AddTerm(w.lemma(), d, postion++ , w.beginPosition(), TermType.Text);
        
        cache.clear();
    }
    
    private static int id = 0;
    public Engine IndexText(String t){
        indexingPlanText("NotFile" + ++id,"", toky.getTokens(t));
        return this;
    }
    
    public Engine IndexText(String f,String t){
        indexingPlanText(f,"", toky.getTokens(t));
        return this;
    }
    
    public Engine IndexFile(File f) throws IOException, TikaException{
        if(f.getName().endsWith(".xml"))
            indexingXml(f.getName(),f.getPath(), f);
        else
            indexingPlanText(f.getName(),f.getPath(), toky.getTokens(new Tika().parseToString(f)));
        return this;
    }
    
    public Engine IndexFiles(File[] files) throws IOException, TikaException {
        return IndexFiles(files, t -> Print(t + " %"));
    }
    
    public Engine IndexFiles(File[] files, Callback<Integer> processBar) throws IOException, TikaException{
        int i = 0;
        for (File f : files){
            IndexFile(f);
            
            if(i++ % 10 == 0)
                processBar.call((i * 100 / files.length));
        }
        
        processBar.call(100);
        return this;
    }
    
    
    public Engine ComputeTF_IDF() {
        index.ApplyTF_IDF();
        return this;
    }
    
    
    
    public List<DocumentResult> SearchQuery(String q,boolean advanceSearch,TermType searchIn) {
        Stopwatch st = new Stopwatch().Start();
        
        List<DocumentResult> res = searchQuery(q, advanceSearch,searchIn);
        
        st.Stop();
        Print(" TIME : " + st.GetMilisec() + " msec     RAM  : " + (st.GetMemoryUsage()/1024) + " Kbyte");
        lastTime = st.GetMilisec();
        return res;
    }
    private long lastTime;
    public long getLastTime(){ return lastTime;}
    
    public boolean useSynonyms = true;
    public boolean useHypernyms = true;
    public boolean useHyponyms = true;
    
    private List<DocumentResult> searchQuery(String q,boolean advanceSearch, TermType searchIn) {
        
        String query = q = q.toLowerCase();
        
        if(cache.check(q + advanceSearch + useSynonyms + useHypernyms + useHyponyms + searchIn.toString()))
            return cache.get(q + advanceSearch + useSynonyms + useHypernyms + useHyponyms + searchIn.toString());
                
        List<DocumentTermEntry> r = new ArrayList<>();

        List<CoreLabel> queryTokens = toky.getTokens(query);
        
        if(advanceSearch)
            r = ParseQuery(query);
        else
            for (CoreLabel w : queryTokens)
                r = searcher.SearchOr(w.lemma(), r);
        
        if(useSynonyms)//&& r.size() < 10)
            for (CoreLabel w : queryTokens)
                for(String s : lesker.getSynonyms(query,w.word(),useHypernyms,useHyponyms)){
                    r = searcher.SearchOr(s, r);
                    query += " or " + s;
                }
        
        if(searchIn != TermType.Any)
            r.removeIf(x->x.checkTermType(searchIn) == false);
        
        
        List<DocumentResult> res = searcher.ranking(Document.convert(r), convertQueryToVector(toky.getTokens(query)));
        
        cache.save(q + advanceSearch + useSynonyms + useHypernyms + useHyponyms + searchIn.toString(), res);
        completer.save(q);
        
        
        String dubg = "your query: ";
        for (CoreLabel c: queryTokens) dubg += c.lemma() + ", ";
        dubg += "\r\nused query: ";
        for (CoreLabel c: toky.getTokens(query)) dubg += c.lemma() + ", ";
        dubg+="\r\n";
        
        PrintErr(dubg);
        
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
    
    public int getDocumentsCount(){
        return index.getCountOfDocuments();
    }
     
    
    public static void PrintR(String str){ System.out.print(str); }
    public static void Print(String str) { System.out.println(str); }
    public static void Print(int t) { System.out.println(t); }
    public static void PrintErr(String str) { System.err.println(str); }
     
    
    public static void main(String[] args) throws FileNotFoundException, IOException, TikaException, Exception {
        try  { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) { }
        
//        tempTest.Pizza();
//        tempTest.MAIN();
        
        java.awt.EventQueue.invokeLater( () -> { new EngineClient().setVisible(true); });
    }
}