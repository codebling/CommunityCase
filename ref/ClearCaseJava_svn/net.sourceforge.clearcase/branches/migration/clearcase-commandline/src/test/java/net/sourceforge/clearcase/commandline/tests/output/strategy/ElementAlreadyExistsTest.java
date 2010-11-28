package net.sourceforge.clearcase.commandline.tests.output.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.sourceforge.clearcase.commandline.output.AbstractMatcherStrategy;
import net.sourceforge.clearcase.commandline.output.strategy.ElementAlreadyExists;
import net.sourceforge.clearcase.enums.Error;
import net.sourceforge.clearcase.status.ClearCaseError;
import net.sourceforge.clearcase.status.ClearCaseStatus;

import org.junit.Test;

public class ElementAlreadyExistsTest {

	@Test
	public void testCheck() {
		AbstractMatcherStrategy strategy = new ElementAlreadyExists();
		String element = "c:\\test";
		StringBuffer output = new StringBuffer(
				"cleartool: Error: Entry named \"" + element
						+ "\" already exists.");
		ClearCaseStatus status = new ClearCaseStatus();
		status = strategy.check(output, status);
		assertTrue(status.getStates().length == 0);
		assertTrue(status.getWarnings().length == 0);
		assertTrue(status.getErrors().length == 1);

		ClearCaseError err = status.getErrors()[0];
		assertEquals("", element, err.getElement());
		assertEquals(err.getType(), Error.ALREADY_ADDED);
	}

}
