package clock;

import java.util.Hashtable;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONObject;

public class VectorClock implements Clock {


    protected Map<String,Integer> clock = new Hashtable<String,Integer>();


    @Override
    public void update(Clock other) {
        for (String key : ((VectorClock) other).clock.keySet()) {
            int keyInt = Integer.parseInt(key);
            int thisTime = this.getTime(keyInt);
            int otherTime = other.getTime(keyInt);
            // Check this has that process on its clock
            if (thisTime != -1) {
                // Only replace the value if its higher
                if (otherTime > thisTime) {
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
            // This is Lamport, iterate the one timestamp in the clock
            for (String key : this.clock.keySet()) {
                this.clock.put(key, this.clock.get(key) + 1);
            }
        }
        else {
            if (this.clock.containsKey(""+pid)) {
                this.clock.put(""+pid, this.clock.get(""+pid) + 1);
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
        List<String> sortedKeys = new ArrayList<>(this.clock.keySet());
        Collections.sort(sortedKeys);

        JSONObject obj = new JSONObject();
        
        for (String key : sortedKeys) {
            obj.put(key, this.getTime(Integer.parseInt(key)));
        }
        
        return obj.toString();
    }


    @Override
    public void setClockFromString(String clock) {
        Map<String, Integer> temp = new Hashtable<>();
        JSONObject obj = new JSONObject(clock);

        for (String key : obj.keySet()) {
            try {
                temp.put(key, obj.getInt(key));
            } catch (org.json.JSONException e) {
                return;
            }
        }
        this.clock = temp;
    }


    @Override
    public int getTime(int p) {
        if (!this.clock.containsKey(""+p))
            return -1;
        return clock.get(""+p);
    }


    @Override
    public void addProcess(int p, int c) {
        clock.put(""+p, c);
    }
}
