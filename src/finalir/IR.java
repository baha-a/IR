package finalir;

import edu.stanford.nlp.ling.CoreLabel;
import finalir.DataStructure.*;
import static finalir.IR.Print;
import java.io.*;
import java.util.*;
import javax.swing.JFileChooser;

public class IR {

    public static void Print(String str) {
        System.out.println(str);
    }
    
    public static void Print(int t) {
        System.out.println(t);
    }

    public static void PrintErr(String str) {
        System.err.println(str);
    }
    
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
        
    public static void main(String[] args) throws FileNotFoundException, IOException {
        
          Google.Pizza();
          //new Google().IndexFiles(getFiles());
        
        
        
        
//        Tokenizer tokenizer = new Tokenizer();        
//        Indexer indexer = new Indexer();
//        
//        for (File doc : getFiles())
//            indexer.indexingDoc(tokenizer.getTokens(doc), doc.getName());
//        
//        Print(tokenizer.getRemovedWordsCount() + " Stopwrods removed");
//
//        indexer.printFrequencyMatrix();
//        indexer.applyTf_Idf();
//        indexer.printWightMatrix();
//
//        String Queri = "love first movie"; // new Scanner(System.in).next();
//        indexer.indexingQuery(tokenizer.getTokens(Queri));
//        
//        Print("cos :");
//        indexer.match("cos");
//        
//        Print("inner :");
//        indexer.match("inner");
    }
}
