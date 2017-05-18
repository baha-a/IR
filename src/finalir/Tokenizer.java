package finalir;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Tokenizer {

    HashSet<String> stopwords = new HashSet<>();
    
    public Tokenizer() {        
        try 
        {
            stopwords.addAll(Files.readAllLines(new File("src\\finalir\\stopwords.txt").toPath()));
        } catch (IOException ex) { Engine.PrintErr("Can't load stopwords file . . ."); }

    }

    public List<CoreLabel> getTokens(File f) throws FileNotFoundException, IOException {
        return getTokens(new Scanner(f).useDelimiter("\\Z").next().toLowerCase());
    }
        
    public List<CoreLabel> getTokens(String txt) {
        return stemming(removeStopWords(Split(txt)));
    }
    
    private int removedWordsCount = 0;
    public int getRemovedWordsCount() {
        return removedWordsCount;
    }
 
    Stemmer enStemmer = new Stemmer();
    ArabicStemmer arStemmer = new ArabicStemmer();
    
    private List<CoreLabel> stemming(List<String> tokens) {
        
        List<CoreLabel> words = new ArrayList<>();
        
        int pos = 0;
        for (String c : tokens)
            if(c.length() > 0){
                words.add(new CoreLabel(c,(c.charAt(0) <= 255) ? enStemmer.stem(c) : arStemmer.stem(c), pos));
                pos += c.length() + 1;
            }
        return words;
    }
    
    private List<String> removeStopWords(List<String> tokens) {
        List<String> t = new ArrayList<>();
        for (String s : tokens)
            if(stopwords.contains(s) == false)
                t.add(s);
        removedWordsCount +=  t.size();
        return t;
    }
    
    
    final String delimitersRegex = "[\\[\\]*/=+!@#$%^&!?~|}{)(.,\n\r\t\\ ]";
    private List<String> Split(String str){
        return Arrays.asList(str.split(delimitersRegex));
    }
}