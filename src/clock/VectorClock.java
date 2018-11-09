package clock;

import java.util.Hashtable;
import java.util.Map;

public class VectorClock implements Clock {


    protected Map<Integer,Integer> clock = new Hashtable<Integer,Integer>();


    @Override
    public void update(Clock other) {
        for (Integer key : ((VectorClock) other).clock.keySet()) {
            int thisTime = this.getTime(key);
            int otherTime = other.getTime(key);
            // Check this has that process on its clock
            if (thisTime != -1) {
                // Only replace the value if its higher
                if (otherTime < thisTime) {
                    this.clock.put(key, otherTime);
                }
            // Otherwise, just add the new process and time
            } else {
                clock.put(key, otherTime);
            }
        }
    }


    @Override
    public void setClock(Clock other) {
        this.clock = ((VectorClock) other).clock;
    }


    @Override
    public void tick(Integer pid) {
        if (pid == null) {
            // Iterate the (one) timestamp in the clock
            for (Integer key : this.clock.keySet()) {
                this.clock.put(key, this.clock.get(key) + 1);
            }
        }
        else {
            if (this.clock.containsKey(pid)) {
                this.clock.put(pid, this.clock.get(pid) + 1);
            }
        }
    }


    @Override
    public boolean happenedBefore(Clock other) {
        VectorClockComparator comp = new VectorClockComparator();
        if (comp.compare(this, (VectorClock) other) < 0)
            return true;
        return false;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Integer key : this.clock.keySet()) {
            sb.append(key);
            sb.append(":");
            sb.append(this.clock.get(key));
            sb.append(",");
        }
        
        return sb.toString();
    }


    @Override
    public void setClockFromString(String clock) {
        String[] timestamps = clock.trim().split(",");
        for (String timestamp : timestamps) {
            String[] keyVal = timestamp.trim().split(":");
            if (keyVal.length == 2) {
                this.clock.put(Integer.parseInt(keyVal[0]), Integer.parseInt(keyVal[1]));
            }
        }
    }


    @Override
    public int getTime(int p) {
        return clock.get(p);
    }


    @Override
    public void addProcess(int p, int c) {
        clock.put(p, c);
    }
}
