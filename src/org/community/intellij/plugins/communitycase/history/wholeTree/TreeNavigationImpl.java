package org.community.intellij.plugins.communitycase.history.wholeTree;

import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.Ring;
import com.intellij.util.Consumer;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.containers.Convertor;
import com.intellij.util.containers.ReadonlyList;

import java.util.*;

/**
 * @author irengrig
 */
public class TreeNavigationImpl implements TreeNavigation, WireEventsListener {
  public static final int[] MARKER = new int[]{-1};
  private final TreeMap<Integer, WireEvent> myWireEvents;
  // created for some of wire events; idx of _commit_
  private final TreeMap<Integer, RingIndex> myRingIndex;
  // maximum number of commits, after which index entry should be written
  private final int myCommitIndexInterval;
  // maximum number of wire events, after which index entry should be written
  private final int myNumWiresInGroup;

  private int myMaximumWires;

  public TreeNavigationImpl(final int commitIndexInterval, final int numWiresInGroup) {
    myCommitIndexInterval = commitIndexInterval;
    myNumWiresInGroup = numWiresInGroup;
    myWireEvents = new TreeMap<Integer, WireEvent>();
    myRingIndex = new TreeMap<Integer, RingIndex>();
    myMaximumWires = 0;
  }

  public void recalcIndex(final ReadonlyList<CommitI> commits, final Convertor<Integer, List<Integer>> future) {
    if (myWireEvents.isEmpty()) return;
    Integer lastIndexKey = myRingIndex.isEmpty() ? null : myRingIndex.lastKey();
    //System.out.println("=== recalc index from: " + lastIndexKey + " ====");
    final SortedMap<Integer, WireEvent> tail;
    final Ring<Integer> ring;
    if (lastIndexKey != null) {
      //tail = myWireEvents.tailMap(lastIndexKey, false); // was like that
      tail = myWireEvents.tailMap(lastIndexKey, true);
      final int size = tail.size();
      if (size == 1) return;
      final Integer lastKey = myWireEvents.lastKey();
      if (lastKey - lastIndexKey < myCommitIndexInterval || size < myNumWiresInGroup) {
        return;
      }
      ring = myRingIndex.lastEntry().getValue().getUsedInRing();
    } else {
      lastIndexKey = myWireEvents.firstKey();
      ring = new Ring.IntegerRing();
      final List<Integer> used = ring.getUsed();
      myRingIndex.put(lastIndexKey, new RingIndex(used.toArray(new Integer[used.size()])));
      WireEvent firstEvent = myWireEvents.firstEntry().getValue();
      performOnRing(ring, firstEvent, commits, future.convert(firstEvent.getCommitIdx()));
      tail = myWireEvents.tailMap(lastIndexKey, false);
    }

    int cnt = 0;
    int recordCommitIdx = myCommitIndexInterval + myWireEvents.floorKey(lastIndexKey);
    for (Integer integer : tail.keySet()) {
      ++ cnt;
      if ((cnt >= myNumWiresInGroup) || (integer >= recordCommitIdx)) {
        final List<Integer> used = ring.getUsed();
        myRingIndex.put(integer, new RingIndex(used.toArray(new Integer[used.size()])));
        cnt = 0;
        recordCommitIdx += myCommitIndexInterval;
      }
      WireEvent event = tail.get(integer);
      performOnRing(ring, event, commits, future.convert(event.getCommitIdx()));
    }
  }

  // todo write start immediately in event in form of hash
  // todo to be able to get it here, when building index
  // todo: problem: when doing index, we don't have "future commits" wires!
  private void performOnRing(final Ring<Integer> ring, final WireEvent event, final ReadonlyList<CommitI> convertor,
                             List<Integer> futureWireStarts) {
    final int[] wireEnds = event.getWireEnds();
    if (wireEnds != null) {
      for (int wireEnd : wireEnds) {
        int wireNumber = convertor.get(wireEnd).getWireNumber();
        if (ring.haveInFree(wireNumber)) {
          System.out.println("assertion will rise here, commits size: " + convertor.getSize() + " event idx: " + event.getCommitIdx());
        }
        //System.out.println("back(1): " + wireNumber + " from: " + event.getCommitIdx());
        ring.back(wireNumber);
      }
    }
    if (event.isStart()) {
      final int commitWire = convertor.get(event.getCommitIdx()).getWireNumber();
      //System.out.println("use(start): " + commitWire);
      ring.minus(commitWire);
    }
    if (event.isEnd()) {
      final int commitWire = convertor.get(event.getCommitIdx()).getWireNumber();
      //System.out.println("back(2): " + commitWire + " from: " + event.getCommitIdx());
      ring.back(commitWire);
    } else {
      final int[] commitsStarts = event.getCommitsStarts();
      for (int commitStart : commitsStarts) {
        final int commitWire = convertor.get(commitStart).getWireNumber();
        //System.out.println("use(merge commit): " + commitWire);
        ring.minus(commitWire);
      }
    }
    for (Integer wireStart : futureWireStarts) {
      ring.minus(wireStart);
    }
    myMaximumWires = Math.max(myMaximumWires, ring.getMaxNumber());
  }

  public Collection<WireEvent> getTail(int rowInclusive) {
    return myWireEvents.tailMap(rowInclusive).values();
  }

  public WireEvent getEventForRow(int row) {
    return myWireEvents.get(row);
  }

  @Override
  public Iterator<WireEvent> createWireEventsIterator(int rowInclusive) {
    return myWireEvents.tailMap(rowInclusive).values().iterator();
  }

  private Iterator<WireEvent> createWireEventsBackIterator(int rowExclusive) {
    return myWireEvents.headMap(rowExclusive, false).descendingMap().values().iterator();
  }

  @Override
  public Ring<Integer> getUsedWires(int row, ReadonlyList<CommitI> commits, final Convertor<Integer, List<Integer>> future) {
    final Map.Entry<Integer, RingIndex> entry = myRingIndex.floorEntry(row);
      if (entry == null) return new Ring.IntegerRing();
    final Ring<Integer> ring = entry.getValue().getUsedInRing();
      if (entry.getKey() == row) {
        return ring;
      }
    System.out.println("-----------------> row = " + row);
      final Iterator<WireEvent> iterator = createWireEventsIterator(entry.getKey());
      while (iterator.hasNext()) {
        final WireEvent event = iterator.next();
        if (event.getCommitIdx() >= row) {
          return ring;
        }
        System.out.println("event: " + event.toString());
        System.out.println("ring before: " + ring.toString());
        performOnRing(ring, event, commits, future.convert(event.getCommitIdx()));
      }
    return ring;
  }

  @Override
  public void addStartToEvent(int row, final int parentRow) {
    modify(row, new Consumer<WireEvent>() {
      @Override
      public void consume(WireEvent wireEvent) {
        wireEvent.addStart(parentRow);
      }
    });
  }

  @Override
  public void wireStarts(int row) {
    modify(row, new Consumer<WireEvent>() {
      @Override
      public void consume(WireEvent wireEvent) {
        /*if (wireEvent.getCommitsEnds() == null) {
          wireEvent.setCommitEnds(MARKER);
        }*/
      }
    });
    //myWireEvents.put(row, new WireEvent(row, MARKER));
  }

  @Override
  public void wireEnds(int row) {
    modify(row, new Consumer<WireEvent>() {
      @Override
      public void consume(WireEvent wireEvent) {
        wireEvent.addStart(-1);
      }
    });
  }

  @Override
  public void setEnds(int row, final int[] commitEnds) {
    modify(row, new Consumer<WireEvent>() {
      @Override
      public void consume(WireEvent wireEvent) {
        wireEvent.setCommitEnds(commitEnds);
      }
    });
  }

  /*@Override
  public void addWireEvent(int row, int[] branched) {
    final WireEvent wireEvent = new WireEvent(row, branched);
    myWireEvents.put(row, wireEvent);
  }*/

  @Override
  public void parentWireEnds(int row, final int parentRow) {
    modify(row, new Consumer<WireEvent>() {
      @Override
      public void consume(WireEvent wireEvent) {
        wireEvent.addWireEnd(parentRow);
      }
    });
  }

  private void modify(final int row, final Consumer<WireEvent> consumer) {
    WireEvent event = myWireEvents.get(row);
    if (event == null) {
      event = new WireEvent(row, null);
      myWireEvents.put(row, event);
    }
    consumer.consume(event);
  }

  public void printSelf() {
    System.out.println("============== EVENTS =================");
    for (WireEvent event : myWireEvents.values()) {
      System.out.println(event.toString());
    }
    System.out.println("==============********=================");
  }

  private static class RingIndex {
    private final Integer[] myWireNumbers;

    private RingIndex(Integer[] wireNumbers) {
      myWireNumbers = wireNumbers;
    }

    public Ring<Integer> getUsedInRing() {
      return new Ring.IntegerRing(Arrays.<Integer>asList(myWireNumbers));
    }
  }

  /*public void recountWires(final int fromIdx, final ReadonlyList<CommitI> commits) {
    final Map<Integer, Integer> recalculateMap = new HashMap<Integer, Integer>();

    //final Iterator<WireEvent> backIterator = createWireEventsBackIterator(fromIdx);
    //int runningCommitNumber = backIterator.hasNext() ? backIterator.next().getCommitIdx() : 0;  // next after previous event
    // t_odo: group two iterators to optimize!

    Ring<Integer> usedWires = getUsedWires(fromIdx, commits);
    final Ring.IntegerRing ring = new Ring.IntegerRing(usedWires.getUsed());

    int runningCommitNumber = 0;
    final Iterator<WireEvent> iterator = createWireEventsIterator(fromIdx);
    for (; iterator.hasNext(); ) {
      final WireEvent we = iterator.next();
      recountFragmentZwichem(commits, recalculateMap, runningCommitNumber, we.getCommitIdx());
      runningCommitNumber = we.getCommitIdx() + 1;

      final int[] wireEnds = we.getWireEnds();
      if (wireEnds != null) {
        for (int wireEnd : wireEnds) {
          ring.back(wireEnd);
        }
      }
      if (we.isStart()) {
        final CommitI thisCommit = commits.get(we.getCommitIdx());
        final int thisWireNum = thisCommit.getWireNumber();
        final Integer newNum = ring.getFree();
        if (newNum != thisWireNum) {
          recalculateMap.put(thisWireNum, newNum);
          // if self is start, recalculate self here
          thisCommit.setWireNumber(newNum);
        }
      }
      if (we.isEnd()) {
        ring.back(commits.get(we.getCommitIdx()).getWireNumber());
      }
      final int[] commitsStarts = we.getCommitsStarts();
      if (commitsStarts.length > 0 && (! we.isEnd())) {
        for (int commitStart : commitsStarts) {
          // wire number
          Integer corrected = recalculateMap.get(commitStart);
          int wasWireNumber = commits.get(commitStart).getWireNumber();
          corrected = (corrected == null) ? wasWireNumber : corrected;
          if (! ring.isNumUsed(corrected)) {
            final Integer newNum = ring.getFree();
            recalculateMap.put(wasWireNumber, newNum);
          }
        }
      }
    }
    recountFragmentZwichem(commits, recalculateMap, runningCommitNumber, commits.getSize() - 1);
    // todo is not called any more
    myMaximumWires = Math.max(myMaximumWires, ring.getMaxNumber());
  }*/

  public int getMaximumWires() {
    return myMaximumWires;
  }

  private void recountFragmentZwichem(ReadonlyList<CommitI> commits,
                                      Map<Integer, Integer> recalculateMap,
                                      int runningCommitNumber,
                                      int inclusive) {
    if (! recalculateMap.isEmpty()) {
      for (int i = runningCommitNumber; i <= inclusive; i++) {
        final CommitI commit = commits.get(i);
        final Integer newWire = recalculateMap.get(commit.getWireNumber());
        if (newWire != null) {
          commit.setWireNumber(newWire);
        }
      }
    }
  }
}
