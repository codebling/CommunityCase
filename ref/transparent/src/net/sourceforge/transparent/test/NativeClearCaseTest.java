package net.sourceforge.transparent.test;

import junit.framework.TestCase;
import net.sourceforge.transparent.*;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: csheppe
 * Date: Feb 19, 2003
 * Time: 10:28:35 AM
 * To change this template use Options | File Templates.
 */
public class NativeClearCaseTest extends ClearCaseTestFixture {

    public NativeClearCaseTest() {
        super(new NativeClearCase());
    }
}
