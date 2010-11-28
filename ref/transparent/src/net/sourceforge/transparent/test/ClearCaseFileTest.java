/*
 * Copyright (c) 2003 Sabre Inc. All rights reserved.
 * This software is the confidential and proprietary product of Sabre Inc.
 * Any unauthorized use, reproduction, or transfer of this software, in any
 * medium, or incorporation of this software into any system or publication,
 * is strictly prohibited. Sabre, the Sabre logo design, and AirServ are
 * trademarks and/or service marks of an affiliate of Sabre Inc. All other
 * trademarks, service marks and trade names are owned by their respective
 * companies.
 *
 */
package net.sourceforge.transparent.test;

import junit.framework.TestCase;

import java.io.*;

import net.sourceforge.transparent.ClearCaseFile;
import net.sourceforge.transparent.MockClearCase;

/**
 * Created by IntelliJ IDEA.
 * User: csheppe
 * Date: Apr 9, 2003
 * Time: 3:42:45 PM
 * To change this template use Options | File Templates.
 */
public class ClearCaseFileTest extends TestCase
{
   public void testDelete_Tree() throws Exception
   {
      MockClearCase cc = new MockClearCase();

      File file = new File("dir1/test.txt");
      File dir = file.getParentFile();

      cc.getElements().add(dir);
      cc.getElements().add(file);

      cc.assertSteps(new String[]{});
      ClearCaseFile clearCaseFile = new ClearCaseFile(file, cc);
      clearCaseFile.delete("");
      cc.verifySteps();
   }
}
