package finalir;

import edu.stanford.nlp.ling.CoreLabel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import javax.swing.JFileChooser;

public class IR {

    public static void Print(String str) {
        System.out.println(str);
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
                
        File[] docs = getFiles(); 
        Print("You have selected " + docs.length + " document.");
        
        tokenizer t = new tokenizer();
        
        indexer i = new indexer();
        
        
        for (File doc : docs)
                i.indexingDoc(t.getTokens(doc), doc.getName());
        
        Print(t.getRemovedWordsCount() + " Stopwrods removed");

        i.printFrequencyMatrix();
        i.applyTf_Idf();
        i.printWightMatrix();

        String Queri = "love first movie"; // new Scanner(System.in).next();
        i.indexingQuery(t.getTokens(Queri));
        
        Print("cos :");
        i.match("cos");
        
        Print("inner :");
        i.match("inner");
    }
}
