package org.intellij.plugins.test;

import junit.framework.TestCase;
import org.intellij.plugins.ListenerNotifier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: Jacques
 * Date: May 22, 2003
 * Time: 2:43:42 PM
 * To change this template use Options | File Templates.
 */
public abstract class ListenerNotifierTest extends TestCase implements PropertyChangeListener {
   protected ListenerNotifier configuration;
   protected boolean          notified;

   protected void setUp() throws Exception {
      configuration.addListener(this);
   }

   public void testAddListener() throws Exception {
      assertEquals("listeners count", 1, configuration.getListeners().length);
   }

   public void testImplementationChanged() throws Exception {
      assertNotificationAfterChange(true);
   }

   public void testRemoveListener() throws Exception {
      configuration.removeListener(this);
      assertNotificationAfterChange(false);
   }

   private void assertNotificationAfterChange(boolean expectedNotification) {
      configuration.notifyListenersOfChange();
      assertEquals("no notification", expectedNotification, notified);
   }

   public void propertyChange(PropertyChangeEvent evt) {
      notified = true;
   }

   public void setConfiguration(ListenerNotifier configuration) {this.configuration = configuration;}
}
