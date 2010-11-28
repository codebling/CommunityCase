package net.sourceforge.clearcase.commandline.tests.output.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.sourceforge.clearcase.commandline.output.AbstractMatcherStrategy;
import net.sourceforge.clearcase.commandline.output.strategy.UnableToCreateElement;
import net.sourceforge.clearcase.enums.Error;
import net.sourceforge.clearcase.status.ClearCaseError;
import net.sourceforge.clearcase.status.ClearCaseStatus;

import org.junit.Test;

public class UnableToCreateElementTest {

	@Test
	public void testCheck() {
		final AbstractMatcherStrategy strategy = new UnableToCreateElement();
		final String element = "c:\\test";
		final StringBuffer output = new StringBuffer(
				"cleartool: Error: Unable to create element \"" + element
						+ "\"");
		ClearCaseStatus status = new ClearCaseStatus();
		status = strategy.check(output, status);
		assertTrue(status.getStates().length == 0);
		assertTrue(status.getWarnings().length == 0);
		assertTrue(status.getErrors().length == 1);

		final ClearCaseError err = status.getErrors()[0];
		assertEquals("", element, err.getElement());
		assertEquals(err.getType(), Error.CAN_NOT_CREATE_ELEMENT);
	}

}
