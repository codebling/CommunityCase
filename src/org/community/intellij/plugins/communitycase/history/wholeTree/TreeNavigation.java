package org.community.intellij.plugins.communitycase.history.wholeTree;

import com.intellij.openapi.vcs.Ring;
import com.intellij.util.containers.Convertor;
import com.intellij.util.containers.ReadonlyList;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * @author irengrig
 */
public interface TreeNavigation {
  /**
   *
   *
   * @param row - commit idx
   * @param commits
   * @return pair: idx of closest commit with ring recorded; ring - ring for that commit
   */
  @Nullable
  Ring<Integer> getUsedWires(int row, ReadonlyList<CommitI> commits, final Convertor<Integer, List<Integer>> future);
  Iterator<WireEvent> createWireEventsIterator(int rowInclusive);
}
