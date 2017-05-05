package finalir;

import edu.stanford.nlp.ling.CoreLabel;
import finalir.DataStructure.*;
import static finalir.IR.Print;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.sqrt;
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

        Print("AllDocumentsCount: \t" +  indx.index.getAllDocumentsCount());
        Print("AvaregeDocumentLength: \t" +  indx.index.getAvaregeDocumentLength());
        Print("SiqmaTF(\"sun\"): \t" +  indx.index.getSiqmaTF("sun"));
        Print("DF(\"sun\"): \t\t" +  indx.index.getDF("sun"));
        Print("SiqmaTF(\"red\"): \t" +  indx.index.getSiqmaTF("red"));
        Print("TF(\"red\",GetDoc(1)): \t" +  indx.index.getTF("red",indx.index.GetDoc(1)));
        Print("DF(\"yellow\"): \t\t" +  indx.index.getDF("yellow"));
        Print("MaxTF(GetDoc(0)): \t" +  indx.index.GetMaxTF(indx.index.GetDoc(0)));

        Print("Stopwrods removed: \t" + indx.toky.getRemovedWordsCount());
        
        Print("");
        Print("" + indx.index.search("orjkgojrgojsrkmlkgmkdsmg").size());
        Print("" + indx.index.search("sun").size());
        Print("" + indx.index.search("SUN").size());
        Print("" + indx.index.search("the").size());
        Print("" + indx.index.search("problem").size());
                
    }
    
    public List<DocumentTermEntry> SearchQuery(String query){
    
    }
    private List<DocumentTermEntry> Search(String term){
        
        // lemma
        // search
        
    }
}
