package clock;

import java.util.Comparator;


public class VectorClockComparator implements Comparator<VectorClock> {

    @Override
    public int compare(VectorClock lhs, VectorClock rhs) {

        boolean before = false;
        boolean after = false;

        for (String key : lhs.clock.keySet()) {
            int keyI = Integer.parseInt(key);
            if (rhs.getTime(keyI) != -1) {
                if (lhs.getTime(keyI) < rhs.getTime(keyI))
                    before = true;
                else if (lhs.getTime(keyI) > rhs.getTime(keyI))
                    after = true;
            }
        }

        for (String key : rhs.clock.keySet()) {
            if (lhs.getTime(Integer.parseInt(key)) == -1) {
                before = true;
                break;
            }
        }

        if (before == after)
            return 0;
        else if (before)
            return -1;
        else if (after)
            return 1;
        
        return 0;
    }
}
