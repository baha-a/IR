package finalir;

import finalir.DataStructure.DocumentResult;
import finalir.DataStructure.DocumentTermEntry;
import finalir.DataStructure.TermType;
import static finalir.Engine.Print;
import static finalir.Engine.PrintErr;
import static finalir.Engine.PrintR;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import javax.swing.JFileChooser;
import org.apache.tika.exception.TikaException;

public class tempTest {
    public static File[] getFiles() {    
        final JFileChooser chooser = new JFileChooser(new File("testCases"));
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
}
