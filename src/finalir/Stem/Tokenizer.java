package finalir.Stem;

import finalir.CoreLabel;
import finalir.Engine;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Tokenizer {

    HashSet<String> stopwords = new HashSet<>();
    
    public Tokenizer() {        
        try 
        {
            stopwords.addAll(Files.readAllLines(new File("src\\finalir\\Stem\\stopwords.txt").toPath()));
        } catch (IOException ex) { Engine.PrintErr("Can't load stopwords file . . ." );  }

    }

    public List<CoreLabel> getTokens(File f) throws FileNotFoundException, IOException {
        return getTokens(new Scanner(f).useDelimiter("\\Z").next().toLowerCase());
    }
        
    public List<CoreLabel> getTokens(String txt) {
        return stemming(Split(txt.toLowerCase()));
    }
    
    private int removedWordsCount = 0;
    public int getRemovedWordsCount() {
        return removedWordsCount;
    }
 
    SnowballStemmer enStemmer = new englishStemmer();
    SnowballStemmer arStemmer = new arabicStemmer();
    
    private List<CoreLabel> stemming(List<String> tokens) {
        
        List<CoreLabel> words = new ArrayList<>();
        
        int pos = 0;
        for (String c : tokens)
            if(c.length() > 0){
                if(stopwords.contains(c))
                    removedWordsCount++;
                else
                {
                    try
                    {    
                        words.add(new CoreLabel(c,(c.charAt(0) <= 255) ? enStemmer.stem(c) : arStemmer.stem(c), pos));
                    }
                    catch(Exception ex)  {  words.add(new CoreLabel(c,c, pos)); }
                }
                pos += c.length() + 1;
            }
        return words;
    }
    
    
    
    //final String delimitersRegex = "[\\[\\]*/=+!@#$%^&!?~|}{)(.,\n\r\t\\ :;\"؟><’÷×؛ـ/-`]";
    //final String delimitersRegex = "[\\[\\^\\$\\.\\|?*+(){/=!@#%&!~}],\n\r\t:;\"؟><’÷×؛ـ/-` ]";
    final String delimitersRegex = "[\\[\\]\\\\ (){}_=.@/,+?|^$*!#%&~:;؟><’÷×؛ـ/\"\'`]";
    private List<String> Split(String str){
        return Arrays.asList(str.split(delimitersRegex));
    }
    
    
}