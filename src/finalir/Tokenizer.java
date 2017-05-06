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
    
    public Tokenizer() {

        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        pipeline = new StanfordCoreNLP(props);
        
        try
        {
            stopwords.addAll(Files.readAllLines(new File("src\\finalir\\stopwords.txt").toPath()));
        }
        catch(IOException ex){
            IR.PrintErr("Can't load stopwords file . . .");
        }
        
    }
    
    public List<CoreLabel> getTokens(File f) throws FileNotFoundException, IOException {
        return getTokens(new Scanner(f).useDelimiter("\\Z").next());
    }

    public List<CoreLabel> getTokens(String queri){
        Annotation ann = new Annotation(queri);
        pipeline.annotate(ann);
        return removeStopWords(ann.get(CoreAnnotations.TokensAnnotation.class));
    }

    private int removedWordsCount = 0;
    public int getRemovedWordsCount(){
        return removedWordsCount;
    }
    
    private List<CoreLabel> removeStopWords(List<CoreLabel> tokens) {
        int tmp =  tokens.size();
       
        tokens.removeIf( x -> stopwords.contains(x.word()) );
        
        removedWordsCount += tmp - tokens.size();
        return tokens;
    }
}
