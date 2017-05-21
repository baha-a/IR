package finalir;

import finalir.Stem.Tokenizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.dictionary.Dictionary;
import java.util.Scanner;


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
    
    public List<String> getSynonyms(String sentense, List<CoreLabel> words,boolean hypernyms,boolean hyponyms){
        int number;
        Set<String> sysns = new HashSet<>();
        for (CoreLabel c : words)
        {
            sysns.addAll(getSynonyms(sentense, c.word(), hypernyms, hyponyms));
            if(c.word().charAt(0) >= '0' && c.word().charAt(0) <= '9')
            {
                number = Integer.parseInt(c.word());
                
                for (CoreLabel s : X.getTokens(Numbers.NumberToWords(number)))
                    sysns.add(s.lemma());
                                
                for (CoreLabel s : X.getTokens(Numbers.NumberToWordsAR(number)))
                    sysns.add(s.lemma());
            }
        }
        
        List<String> res = new ArrayList<>();
        boolean a;
        for (String s : sysns) {
            a = true;
            for (CoreLabel c : words) 
                if(s.contains(c.word()) || s.contains(c.lemma())){
                    a = false;
                    break;
                }
            if(a)
                res.add(s);
        }
        return res;
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
        
        StringBuilder st = new StringBuilder();

        // get correct synonyms
        for (Word w : word.getSenses().get(index).getWords())
                st.append(w.getLemma()).append(" ");
    
        // get all hypernyms
        if(hypernyms)
        for (PointerTargetNode p : PointerUtils.getDirectHypernyms(word.getSenses().get(index)))
            for (Word w : p.getSynset().getWords())
                st.append(w.getLemma()).append(" ");

        // get some hyponyms
        int some = 3;
        if(hyponyms)
            for (PointerTargetNode p : PointerUtils.getDirectHyponyms(word.getSenses().get(index))) {
                for (Word w : p.getSynset().getWords())
                    st.append(w.getLemma()).append(" ");
                if(--some == 0)
                    break;
            }
        
        
        List<String> res = new ArrayList<>();
        for (CoreLabel s : X.getTokens(st.toString()))
            if(res.contains(s.lemma()) == false)
                res.add(s.lemma());
        
        return res;
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

class Numbers {

        static String unitsMap[] = { "zero", "one", "two", "three", "four", "five","six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen" };
        static String tensMap[] = { "zero", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety" };

        public static String NumberToWords(int number){
            
            if (number == 0) return "zero";

           if (number < 0)
               return "minus " + NumberToWords(number * -1);

           String words = "";

           if ((number / 1000000000) > 0)
           {
               words += NumberToWords(number / 1000000000) + " billion ";
               number %= 1000000000;
           }

           if ((number / 1000000) > 0)
           {
               words += NumberToWords(number / 1000000) + " million ";
               number %= 1000000;
           }

           if ((number / 1000) > 0)
           {
               words += NumberToWords(number / 1000) + " thousand ";
               number %= 1000;
           }

           if ((number / 100) > 0)
           {
               words += NumberToWords(number / 100) + " hundred ";
               number %= 100;
           }

           if (number > 0)
           {
               if (number < 20)
                   words += unitsMap[number];
               else
               {
                   words += tensMap[number / 10];
                   if ((number % 10) > 0)
                       words += " " + unitsMap[number % 10];
               }
           }

           return words;
        }
        
        
        static String unitsMapAr[] = { "صفر", "واحد", "اثنان", "ثلاثة", "اربعة", "خمسة","ستة", "سبعة", "ثمانية", "تسعة", "عشرة", "احد عشر", "اثنا عشر", "ثلاثة عشر", "اربعة عشر", "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر" };
        static String tensMapAr[] = { "صفر", "عشرة", "عشرون", "ثلاثون", "اربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون" };
        
        public static String NumberToWordsAR(int number){
            
            if (number == 0) return unitsMapAr[0];

           if (number < 0)
               return "سالب " + NumberToWordsAR(number * -1);

           String words = "";

           if ((number / 1000000000) > 0)
           {
               words += NumberToWordsAR(number / 1000000000) + " مليار ";
               number %= 1000000000;
           }

           if ((number / 1000000) > 0)
           {
               words += NumberToWordsAR(number / 1000000) + " مليون ";
               number %= 1000000;
           }

           if ((number / 1000) > 0)
           {
               words += NumberToWordsAR(number / 1000) + " ألف ";
               number %= 1000;
           }

           if ((number / 100) > 0)
           {
               words += NumberToWordsAR(number / 100) + " مئة ";
               number %= 100;
           }

           if (number > 0)
           {
               if (number < 20)
                   words += unitsMapAr[number];
               else
               {
                   words += tensMapAr[number / 10];
                   if ((number % 10) > 0)
                       words += " " + unitsMapAr[number % 10];
               }
           }

           return words;
        }
    }