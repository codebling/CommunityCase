/*
 * Created by IntelliJ IDEA.
 * User: sg426575
 * Date: Oct 14, 2002
 * Time: 5:52:08 PM
 * To change this template use Options | File Templates.
 */
package net.transparent.ccjni.ClearCase.test;

import junit.framework.*;
import net.transparent.ccjni.ClearCase.*;
import com.develop.jawin.win32.Ole32;

public class IClearCaseTest extends TestCase {

   public void testSimple() throws Exception
   {
      Ole32.CoInitialize();
      try {
         IClearCase cc = new IClearCase(Application.clsID);
         ICCVersion version = cc.getVersion("C:/cc_views/ss/sabresoft/WebGUI/bin");
         version.CheckOut(CCReservedState.ccReserved,
                          "test",
                          true,
                          CCVersionToCheckOut.ccVersion_Default,
                          true,
                          true);
         assertTrue("version checked out", version.getIsCheckedOut());
      } finally {
         Ole32.CoUninitialize();
      }
   }
}

