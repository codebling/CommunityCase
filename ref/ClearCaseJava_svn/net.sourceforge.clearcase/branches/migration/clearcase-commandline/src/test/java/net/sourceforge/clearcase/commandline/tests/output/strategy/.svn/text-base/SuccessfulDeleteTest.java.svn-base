package net.sourceforge.clearcase.commandline.tests.output.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import net.sourceforge.clearcase.commandline.output.AbstractMatcherStrategy;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulDelete;
import net.sourceforge.clearcase.enums.ElementStatus;
import net.sourceforge.clearcase.status.ClearCaseElementState;
import net.sourceforge.clearcase.status.ClearCaseStatus;

import org.junit.Test;

public class SuccessfulDeleteTest {

	@Test
	public void testCheck() {
		final AbstractMatcherStrategy strategy = new SuccessfulDelete();
		final String element = "c:\\test";
		final StringBuffer output = new StringBuffer("Removed \"" + element
				+ "\"");
		ClearCaseStatus status = new ClearCaseStatus();
		status = strategy.check(output, status);
		assertTrue(status.getStates().length == 1);
		assertTrue(status.getWarnings().length == 0);
		assertTrue(status.getErrors().length == 0);

		final ClearCaseElementState state = status.getStates()[0];
		assertEquals(element, state.getElement());
		assertEquals(EnumSet.of(ElementStatus.REMOVED), state.getState());
	}

}
