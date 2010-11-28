package net.sourceforge.transparent.actions.checkin.test;

import javax.swing.*;

import junit.framework.TestCase;
import net.sourceforge.transparent.actions.checkin.BaseCheckInDialog;
import org.intellij.openapi.testing.MockProject;

/**
 * Created by IntelliJ IDEA.
 * User: tkmower
 * Date: Mar 14, 2003
 * Time: 3:00:10 PM
 * Copyright (c) 2003 Sabre, Inc. All rights reserved. 
 */
public class BaseCheckInDialogTest extends TestCase
{
   public void testSetShowScrField_Visible() throws Exception
   {
      BaseCheckInDialog dialog = new BaseCheckInDialog(new MockProject()) { };
      dialog.createCenterPanel();
      dialog.setShowScrField(true);
      assertTrue("field should be visible", dialog.getScrFieldPanel().isVisible());
   }

   public void testSetShowScrField_NotVisible() throws Exception
   {
      BaseCheckInDialog dialog = new BaseCheckInDialog(new MockProject()) { };
      dialog.createCenterPanel();
      dialog.setShowScrField(false);
      assertFalse("field should not be visible", dialog.getScrFieldPanel().isVisible());
   }
}
