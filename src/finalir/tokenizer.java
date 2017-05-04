package finalir;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class tokenizer {
    Properties props = new Properties();
    StanfordCoreNLP pipeline;

    HashSet<String> stopwordcorpus = new HashSet<>();
    
    public tokenizer() throws FileNotFoundException {

        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        pipeline = new StanfordCoreNLP(props);
        
        try
        {
            stopwordcorpus.addAll(Files.readAllLines(new File("src\\finalir\\stopwords.txt").toPath()));
        }
        catch(IOException ex)
        {
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
        Integer tmp =  tokens.size();
        
        //tokens.removeAll(stopwordcorpus);
        
        for (int i = 0; i < tokens.size(); i++)
            if (stopwordcorpus.contains(tokens.get(i).word()))
                tokens.remove(i);
        
        removedWordsCount += tmp - tokens.size();
        return tokens;
    }
}
