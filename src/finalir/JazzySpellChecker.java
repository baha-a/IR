package finalir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;
import com.swabunga.spell.event.TeXWordFinder;

public class JazzySpellChecker implements SpellCheckListener {

    private SpellChecker spellChecker;
    private List<String> misspelledWords;

    private static SpellDictionaryHashMap dictionaryHashMap;
    
    static 
    {
        File dict = new File("dictionary/dictionary.txt");
        try { dictionaryHashMap = new SpellDictionaryHashMap(dict); }
        catch (Exception e) { System.err.println("  Cann't load dictionary for spellchacker ");}
    }

    public JazzySpellChecker() {
        misspelledWords = new ArrayList<>();
        spellChecker = new SpellChecker(dictionaryHashMap);
        spellChecker.addSpellCheckListener(this);
    }

    private List<String> getMisspelledWords(String text) {
        misspelledWords.clear();
        spellChecker.checkSpelling(new StringWordTokenizer(text, new TeXWordFinder()));
        return misspelledWords;
    }

    public boolean HasError(String q) {
        List<String> m = getMisspelledWords(q);
        return (m != null && m.size() > 0);
    }
    
    public String getCorrectedLine(String line) {
        for (String misSpelledWord : getMisspelledWords(line)){
            List<String> suggestions = getSuggestions(misSpelledWord);
            if (suggestions.isEmpty()) 
                continue;
            line = line.replace(misSpelledWord,  suggestions.get(0));
        }
        return line;
    }

    private List<String> getSuggestions(String misspelledWord) {
        List<String> suggestions = new ArrayList<>();
        for (Word suggestion : (List<Word>)spellChecker.getSuggestions(misspelledWord, 0)) 
            suggestions.add(suggestion.getWord());
        return suggestions;
    }

    @Override
    public void spellingError(SpellCheckEvent event) {
        //event.ignoreWord(true);
        misspelledWords.add(event.getInvalidWord());
    }
}
