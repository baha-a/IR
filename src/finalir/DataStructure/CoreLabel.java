package finalir;

public class CoreLabel{

    String word;
    String lemma;
    int position;
    
    public CoreLabel setWord(String s){
        word = s;
        return this;
    }
    public String word(){
        return word;
    }
    
    public CoreLabel setBeginPosition(int t){
        position = t;
        return this;
    }
    public int beginPosition(){
        return position;
    }

    public CoreLabel setLemma(String s){
        lemma = s;
        return this;
    }
    public String lemma(){
        return lemma;
    }    

    public CoreLabel(){}
    public CoreLabel(String w,String l,int p){
        word = w;
        lemma = l;
        position = p;
    }
}