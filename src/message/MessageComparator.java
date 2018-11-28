package message;

import java.util.Comparator;

import clock.VectorClock;
import clock.VectorClockComparator;

/**
 * Message comparator class. Use with PriorityQueue.
 */
public class MessageComparator implements Comparator<Message> {

    @Override
    public int compare(Message lhs, Message rhs) {
        VectorClockComparator comp = new VectorClockComparator();
        return comp.compare(lhs.ts, rhs.ts);
    }

}
