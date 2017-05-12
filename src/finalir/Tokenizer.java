package finalir;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Tokenizer {

    Properties props = new Properties();
    StanfordCoreNLP pipeline;

    HashSet<String> stopwords = new HashSet<>();

    private boolean stemming;
    
    public Tokenizer(boolean stemmingOnly) {

        stemming = stemmingOnly;
        
        if(stemmingOnly == true)
            props.setProperty("annotators", "tokenize,ssplit");//,pos,lemma");
        else
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma");    
        pipeline = new StanfordCoreNLP(props);

        try {
            stopwords.addAll(Files.readAllLines(new File("src\\finalir\\stopwords.txt").toPath()));
        } catch (IOException ex) {
            IR.PrintErr("Can't load stopwords file . . .");
        }

    }

    public List<CoreLabel> getTokens(File f) throws FileNotFoundException, IOException {
        return getTokens(new Scanner(f).useDelimiter("\\Z").next());
    }
    
    
    public List<CoreLabel> getTokens(String queri) {
        Annotation ann = new Annotation(queri);
        pipeline.annotate(ann);
        List<CoreLabel> res = removeStopWords(ann.get(CoreAnnotations.TokensAnnotation.class));
        if(stemming)
            res = stemming(res);
        return res;
    }
    
    private int removedWordsCount = 0;

    public int getRemovedWordsCount() {
        return removedWordsCount;
    }
 
    
    private List<CoreLabel> stemming(List<CoreLabel> tokens) {
        Stemmer s = new Stemmer();
        for (CoreLabel c : tokens)
            c.setLemma(s.stem(c.word()));
        return tokens;
    }
    
    private List<CoreLabel> removeStopWords(List<CoreLabel> tokens) {
        int tmp =  tokens.size();
        tokens.removeIf( x -> stopwords.contains(x.word()) );
        removedWordsCount += tmp - tokens.size();
        return tokens;
    }
}