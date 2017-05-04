package finalir.DataStructure;

public class Stopwatch {
    
    private long time;
    private long duration;
    private boolean started = false;
    
    public Stopwatch Start(){
        started = true;
        time = System.currentTimeMillis();
        return this;
    }
    
    public Stopwatch Stop(){
        if(started == true)
            duration += System.currentTimeMillis() - time;
        started = false;
        return this;
    }
    
    public Stopwatch Reset(){
        duration = time = 0;
        return this;
    }
    
    public long getMilisec(){
        if(started)
            return (System.currentTimeMillis() - time) + duration;
        return duration;
    }
    
    public long getSec(){
        return Math.round(getMilisec() / 1000.0);
    }
}
