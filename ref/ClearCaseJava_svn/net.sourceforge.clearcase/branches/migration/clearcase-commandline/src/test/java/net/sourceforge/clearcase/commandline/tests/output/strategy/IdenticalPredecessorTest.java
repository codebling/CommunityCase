package net.sourceforge.clearcase.commandline.tests.output.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.sourceforge.clearcase.commandline.output.AbstractMatcherStrategy;
import net.sourceforge.clearcase.commandline.output.strategy.IdenticalPredecessor;
import net.sourceforge.clearcase.enums.Error;
import net.sourceforge.clearcase.status.ClearCaseError;
import net.sourceforge.clearcase.status.ClearCaseStatus;

import org.junit.Test;

public class IdenticalPredecessorTest {

	@Test
	public void testCheck() {
		AbstractMatcherStrategy strategy = new IdenticalPredecessor();
		String element = "c:\\test";
		StringBuffer output = new StringBuffer(
				"cleartool: Error: By default, won't create version with data identical to predecessor.")
				.append("\n").append(
						"cleartool: Error: Unable to check in \"" + element
								+ "\"");
		ClearCaseStatus status = new ClearCaseStatus();
		status = strategy.check(output, status);
		assertTrue(status.getStates().length == 0);
		assertTrue(status.getWarnings().length == 0);
		assertTrue(status.getErrors().length == 1);

		ClearCaseError err = status.getErrors()[0];
		assertEquals(element, err.getElement());
		assertEquals(err.getType(), Error.PREDECESSOR_IS_IDENTICAL);
	}

}
