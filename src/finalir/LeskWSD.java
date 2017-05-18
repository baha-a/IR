package finalir;

import java.util.ArrayList;
import java.util.List;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.dictionary.Dictionary;

public class LeskWSD{

    private Dictionary dictionary;

    private Tokenizer X;
    
    public LeskWSD() { 
        try
        {
            X = new Tokenizer();
            dictionary = Dictionary.getDefaultResourceInstance(); 
        }
        catch(Exception j) {  }
    }
    
    public List<String> getSynonyms(String sentense, String word){
        try
        {
            return disambiguate(sentense, dictionary.lookupIndexWord(POS.NOUN, word),true,true);
        }
        catch(Exception j) { }
       
        return new ArrayList<>();
    }
    
    public List<String> getSynonyms(String sentense, String word,boolean hypernyms,boolean hyponyms){
        try
        {
            return disambiguate(sentense, dictionary.lookupIndexWord(POS.NOUN, word),hypernyms,hyponyms);
        }
        catch(Exception j) { }
       
        return new ArrayList<>();
    }
    
    private List<String> disambiguate(String sentense, IndexWord word,boolean hypernyms,boolean hyponyms) throws JWNLException {
        int index = getMaxIdx(lesk(word, sentense));
        
        // get correct synonyms
        List<String> sysns = new ArrayList<>();
        for (Word w : word.getSenses().get(index).getWords())
            if(word.getLemma().equals(w.getLemma()) == false)
                sysns.add(w.getLemma());
    
        // get all hypernyms
        if(hypernyms)
        for (PointerTargetNode p : PointerUtils.getDirectHypernyms(word.getSenses().get(index)))
            for (Word w : p.getSynset().getWords())
                sysns.add(w.getLemma());

        // get some hyponyms
        int some = 3;
        if(hyponyms)
            for (PointerTargetNode p : PointerUtils.getDirectHyponyms(word.getSenses().get(index))) {
                for (Word w : p.getSynset().getWords())
                    sysns.add(w.getLemma());
                if(--some == 0)
                    break;
            }
        
        return sysns;
    }
    
    private int[] lesk(IndexWord word, String sentense) {
        String[] glosses = getGlosses(word);
        int intersection[] = new int[glosses.length];

        for (int i = 0; i < glosses.length; i++)
            for (CoreLabel w : X.getTokens(sentense))
                for (CoreLabel t : X.getTokens(glosses[i]))
                    if (w.lemma().equals(t.lemma()))
                        intersection[i]++;
        
        return intersection;
    }
    
    private String[] getGlosses(IndexWord word) {
        List<Synset> synset = word.getSenses();
        String glosses[] = new String[synset.size()];

        for (int i = 0; i < synset.size(); i++)
            glosses[i] = synset.get(i).getGloss();

        return glosses;
    }
    
    private int getMaxIdx(int[] intersections) {
        int idx = 0;
        int max = intersections[0];

        for (int i = 0; i < intersections.length; i++)
            if (max < intersections[i]) {
                max = intersections[i];
                idx = i;
            }
        return idx;
    }
}