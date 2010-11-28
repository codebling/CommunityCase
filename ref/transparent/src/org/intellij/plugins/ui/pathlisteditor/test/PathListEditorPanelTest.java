package org.intellij.plugins.ui.pathlisteditor.test;

import junit.framework.TestCase;
import org.intellij.plugins.ExcludedPathsFromVcsConfiguration;
import org.intellij.plugins.ui.pathlisteditor.PathListElement;
import org.intellij.plugins.ui.pathlisteditor.PathListEditorPanel;
import org.intellij.plugins.util.testing.MockFileUtil;

/**
 * Created by IntelliJ IDEA.
 * User: Jacques
 * Date: May 22, 2003
 * Time: 5:56:24 PM
 * To change this template use Options | File Templates.
 */
public class PathListEditorPanelTest extends TestCase {
   private ExcludedPathsFromVcsConfiguration configuration = new ExcludedPathsFromVcsConfiguration(null);
   private PathListEditorPanel pathListEditorPanel;
   private PathListElement PATH1;
   private PathListElement PATH2;

   public void setUp() {
      pathListEditorPanel = new PathListEditorPanel("",configuration, new MockFileUtil());
      PATH1 = new PathListElement("test1", false, true);
      PATH2 = new PathListElement("test2", false, true);
   }

   public void testApply() throws Exception {
      pathListEditorPanel.getPathListElements().add(PATH1);
      pathListEditorPanel.getPathListElements().add(PATH2);
      pathListEditorPanel.apply();
      assertEquals("excludedPaths length", 2, configuration.getExcludedPaths().size());
      assertEquals("excludedPath 1", PATH1 , configuration.getExcludedPaths().get(0));
      assertEquals("excludedPath 2", PATH2 , configuration.getExcludedPaths().get(1));
   }

   public void testIsModified_NoChange() throws Exception {
      assertFalse("should not be modified", pathListEditorPanel.isModified());
   }

   public void testIsModified_Changed() throws Exception {
      pathListEditorPanel.getPathListElements().add(PATH2);
      assertTrue("should be modified", pathListEditorPanel.isModified());
   }


}
