package finalir.DataStructure;

public class Stopwatch {
    
    private long time;
    private long duration;
    private long totalSum;
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
    
    public Stopwatch StoreTime(){
        totalSum += duration;
        return this;
    }
    
    public Stopwatch ResetTotal(){
        totalSum = 0;
        return this;
    }

    
    public long CurrentTimeMillis(){
        return System.currentTimeMillis();
    }
    
    public long GetTotal(){
        return totalSum;
    }
    
     
    public long GetMilisec(){
        if(started)
            return (System.currentTimeMillis() - time) + duration;
        return duration;
    }
    
    public long GetSec(){
        return Math.round(GetMilisec() / 1000.0);
    }
}
