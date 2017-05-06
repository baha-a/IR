package finalir;

import finalir.DataStructure.Stopwatch;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class wordNetSearcher {
    public static void main(String[] args) {
        Stopwatch st = new Stopwatch().Start();

        
        IR.Print("Hello wordnet");
        wordNetSearcher w = new wordNetSearcher();
        w.getSynonyms("capacity");

        System.out.println(st.Stop().GetMilisec());
        System.out.println(st.GetSec());
    }

    void getSynonyms(String wordForm) {
   // System.setProperty("wordnet.database.dir", path);
        // WordNetDatabase database = WordNetDatabase.getFileInstance();

        // String wordForm = "capacity";
        //  Get the synsets containing the word form=capicity
        File f = new File("WordNet\\3.1\\dict");
        System.setProperty("wordnet.database.dir", f.toString());
        //setting path for the WordNet Directory

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(wordForm);
        //  Display the word forms and definitions for synsets retrieved

        if (synsets.length > 0) {
            ArrayList<String> al = new ArrayList<String>();
            // add elements to al, including duplicates
            HashSet hs = new HashSet();
            String[] wordForms;
            for (int i = 0; i < synsets.length; i++) {
                wordForms = synsets[i].getWordForms();
                for (int j = 0; j < wordForms.length; j++) {
                    al.add(wordForms[j]);
                }

                //removing duplicates
                hs.addAll(al);
                al.clear();
                al.addAll(hs);
            }

            for (String al1 : al)
                IR.Print(al1);
        }
        else 
            IR.PrintErr("No synsets exist that contain the word form '" + wordForm + "'");
    }
}
