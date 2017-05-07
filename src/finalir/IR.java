package finalir;

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
        
        //Google.Pizza();
        Google g = new Google().IndexFiles(getFiles()).ComputeTF_IDF();
        
        String query;
        do
        {
            Print("google: ");
            Print(g.SearchQuery(query = new Scanner(System.in).next()).size());
        }while(!query.toLowerCase().equals("x"));
        
//        indexer.indexingQuery(tokenizer.getTokens(Queri));
//        indexer.match("cos");
//        indexer.match("inner");
    }
}
