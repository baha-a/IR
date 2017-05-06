package finalir.DataStructure;

public class Observation {
    private int position;
    private int exactPosition;
    private TermType type;

    public Observation(int pos,int exactPos, TermType t) {
        position = pos;
        exactPosition = exactPos;
        type = t;
    }

    public int getPosition() {
        return position;
    }
    
    public int getExactPosition() {
        return position;
    }

    public TermType getType() {
        return type;
    }
        
    public int getTypeWeight() {
        switch(type)
        {
            case Titel:       return 10;
            case Author:      return 7;
            case Description: return 4;
        }
        return 1;
    }
}
