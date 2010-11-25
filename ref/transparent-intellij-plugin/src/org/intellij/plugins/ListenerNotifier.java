package org.intellij.plugins;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Created by IntelliJ IDEA.
 * User: Jacques
 * Date: May 22, 2003
 * Time: 2:37:38 PM
 * To change this template use Options | File Templates.
 */
public interface ListenerNotifier {
   public PropertyChangeListener[] getListeners();

   public void addListener(PropertyChangeListener listener);

   public void notifyListenersOfChange();

   public void removeListener(PropertyChangeListener listener);
}
