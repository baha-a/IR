package finalir;

import edu.stanford.nlp.ling.CoreLabel;
import finalir.DataStructure.*;
import static finalir.IR.Print;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
        indexing("NotFile" + id++, toky.getTokens(t));
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
    
    
    
        
    public List<DocumentTermEntry> SearchQuery(String query){
        
        // to-do later: complete this method
        // first, parse the query and execute
        //
        
        for (CoreLabel w : toky.getTokens(query))
            return searcher.search(w.lemma());
        return null;
    }
    
    
    public static void Pizza(){
        
        Google indx = new Google()
            .IndexText("the sun is yellow and the sky it is blue, the weather is wonderful sun blue blue very blue")
            .IndexText("the sun is yellow and very blue")
            .IndexText("get your eyes over here wonderfully")
            .IndexText("this is the project to solve your information retrivel problems, enjoy it and have a good day")
            .IndexText("the sun is not yellow and the sky isn't realy blue red red red, the end")
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
        Print(indx.searcher.SearchAnd("sky",indx.searcher.SearchAnd("red", "yellow")).size());
        Print(indx.searcher.SearchAnd("sky",indx.searcher.SearchAnd("red", indx.searcher.SearchAnd("eye", "yellow"))).size());
        
        Print(indx.searcher.SearchNear(2,"sky","blue").size());
        Print(indx.searcher.SearchNear(1,"sky","eye").size());
        
        Print(indx.searcher.SearchNear(1,"red","red","red").size());
        Print(indx.searcher.SearchNear(1,"red","red","red","end").size());
        Print(indx.searcher.SearchNear(1,"realy","blue","red","red","red","end").size());
        Print(indx.searcher.SearchNear(1,"sky","realy","blue","red","red","red","end").size());
        Print(indx.searcher.SearchNear(1,"sky","realy","red","red","red","end").size());
        
        for (int i = 0; i < 10*1000*1000; i++) 
            indx.searcher.search("sun");
        
        IR.Print("" + st.Stop().GetMilisec());
    }
     
//    void indexingQuery(List<CoreLabel> tokens) {
//        for (int j = 0; j < query.length; j++)
//            query[j] = (float) ((0.5 + 0.5 * (query[j] / maxFrequencyInQuery)) * (Math.log(Documents.size() / dfi[j])));
//    }
//
//    List<String> match(String similarityFunction) {
//        result.clear();
//        for (int i = 0; i < Documents.size(); i++) 
//        {
//            float sim = 0;
//            if (similarityFunction == "cos")
//                sim = cosine(query, vectors[i]);
//            else if (similarityFunction == "inner")
//                sim = dot(query, vectors[i]);
//
//            result.put(sim, Documents.get(i));
//        }
//
//        return result;
//    }
}