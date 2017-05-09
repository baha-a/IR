package finalir;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import finalir.DataStructure.Stopwatch;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WordNet {
    
    public static List<String> getSynonyms(String wordForm) {
        System.setProperty("wordnet.database.dir", new File("WordNet\\3.1\\dict").toString());
        
        HashSet hs = new HashSet();
        for (Synset sy : WordNetDatabase.getFileInstance().getSynsets(wordForm))
            for (String s : sy.getWordForms())
                hs.add(s);
        return new ArrayList<>(hs);
    }
}
