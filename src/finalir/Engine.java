package finalir;

import Form.EngineClient;
import finalir.DataStructure.*;
import finalir.Stem.Tokenizer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JFileChooser;
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

            Document d = index.AddDoc(name,path, t.length() + a.length() + c.length());
            int position = 0;
            for (CoreLabel w : toky.getTokens(t))
                index.AddTerm(w.lemma(), d, position++ , w.beginPosition(), TermType.Titel);
            
            for (CoreLabel w : toky.getTokens(a))
                index.AddTerm(w.lemma(), d, position++ , w.beginPosition(), TermType.Author);
            
            for (CoreLabel w : toky.getTokens(c))
                index.AddTerm(w.lemma(), d, position++ , w.beginPosition(), TermType.Text);
            
            cache.clear();
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
        
        List<DocumentResult> res = searchQuery2(q, advanceSearch,searchIn);
        
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
    
    private List<DocumentResult> searchQuery2(String q,boolean advanceSearch, TermType searchIn) {
        
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
    
    
    public static void Pizza(){
    
        Engine indx = new Engine(true)
            .IndexText("the sun is yellow and the  sky it is blue, the weather is wonderful sun green blue very white")
            .IndexText("the sun is yellow and very blue, not sky red eye")
            .IndexText("get your eyes over here  wonderfully")
            .IndexText("this is the project to solve your information retrivel problems, enjoy it and have a good day")
            .IndexText("the sun is not yellow and the sky isn't realy red red red, the end eye السيارة")
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
        
        
        Print(" ---- 5 ---- ");
        List<DocumentTermEntry> d1 = indx.searcher.search("sky");
        List<DocumentTermEntry> d2 = indx.searcher.search("red");
        List<DocumentTermEntry> d3 = indx.searcher.And(d1, d2);
        List<DocumentTermEntry> d4 = indx.searcher.And(d2, d1);
        
        for (DocumentTermEntry d : d3)
            Print(d.getDocument().getName());   
        Print("-");
        for (DocumentTermEntry d : d4)
            Print(d.getDocument().getName());   
        
        
        Print(" ---- 6 ---- ");
        for (String w : indx.lesker.getSynonyms("the dog is black", "dog"))
            PrintR(w +", ");
        Print("");
        
        Print("Enter your querys (or x to exit):");
        for(;;)
            for (DocumentResult d : indx.SearchQuery(new Scanner(System.in,"UTF-8").nextLine(),true,TermType.Any))
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
    
    public int getDocumentsCount(){
        return index.getCountOfDocuments();
    }
    
    
    
    
    
    
    
    
    
    public static void PrintR(String str){ System.out.print(str); }
    public static void Print(String str) { System.out.println(str); }
    public static void Print(int t) { System.out.println(t); }
    public static void PrintErr(String str) { System.err.println(str); }
    
    public static File[] getFiles() {    
        final JFileChooser chooser = new JFileChooser(new File("src\\finalir\\testCases"));
        chooser.setMultiSelectionEnabled(true);
        chooser.setAcceptAllFileFilterUsed(false);

        File[] files = null;
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            files = chooser.getSelectedFiles();
        }
        return files;
    }    
            
    public static void MAIN() throws FileNotFoundException, IOException, TikaException, Exception {
        Engine g = new Engine(true).IndexFiles(getFiles()).ComputeTF_IDF();
        JazzySpellChecker speller = new JazzySpellChecker();
        
        String query = "";
        List<DocumentResult> res;
     
        for(int top; !query.equals("x");){
            
            top = 10;
            
            PrintR("Query: ");
            res = g.SearchQuery(query = new Scanner(System.in).nextLine().toLowerCase(),true,TermType.Any);
            
            if(speller.HasError(query))
                PrintR("Did you mean: " + speller.getCorrectedLine(query));
            
            if(res.isEmpty())
                Print("nothing found");
            
            PrintErr(" AutoCompleting : ");
            for (String str : g.getSuggestions(query))
                Print(str);
            
            PrintErr(" RESULT : ");
            for (DocumentResult d : res) {
                if(top-- == 0)
                    break;
                Print(d.getDocument().getName() + " -> " + d.getRank());
            }
        }

//        new PrintWriter("C:\\json.txt").write(
//                new ObjectMapper()
//                .writer()
//                .writeValueAsString(g));
        
        
        //System.out.println(new Gson().toJson(g));
    }
     
    
    public static void main(String[] args) throws FileNotFoundException, IOException, TikaException, Exception {
        try  { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) { }
        
        //Engine.Pizza();
        //MAIN();    
        
        java.awt.EventQueue.invokeLater( () -> { new EngineClient().setVisible(true); });
    }
}