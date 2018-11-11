package clock;

import java.util.Comparator;


public class VectorClockComparator implements Comparator<VectorClock> {

    @Override
    public int compare(VectorClock lhs, VectorClock rhs) {
        boolean before = false;
        boolean after = false;
        for (String key : lhs.clock.keySet()) {
            int keyInt = Integer.parseInt(key);
            if (rhs.getTime(keyInt) != -1) {
                if (lhs.getTime(keyInt) < rhs.getTime(keyInt))
                    before = true;
                else if (lhs.getTime(keyInt) > rhs.getTime(keyInt))
                    after = true;
            }
        }

        if (before && after || (!before && !after))
            return 0;
        else if (before)
            return -1;
        else if (after)
            return 1;

        return 0;
    }
}
