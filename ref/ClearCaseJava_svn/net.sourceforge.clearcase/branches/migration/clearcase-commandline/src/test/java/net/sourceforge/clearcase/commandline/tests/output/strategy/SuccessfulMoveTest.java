package net.sourceforge.clearcase.commandline.tests.output.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import net.sourceforge.clearcase.commandline.output.AbstractMatcherStrategy;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulMove;
import net.sourceforge.clearcase.enums.ElementStatus;
import net.sourceforge.clearcase.status.ClearCaseElementState;
import net.sourceforge.clearcase.status.ClearCaseStatus;

import org.junit.Test;

public class SuccessfulMoveTest {

	@Test
	public void testCheck() {
		final AbstractMatcherStrategy strategy = new SuccessfulMove();
		final String element1 = "c:\\test1";
		final String element2 = "c:\\test2";
		final StringBuffer output = new StringBuffer("Moved \"" + element1
				+ "\" to \"" + element2 + "\"");
		ClearCaseStatus status = new ClearCaseStatus();
		status = strategy.check(output, status);
		assertTrue(status.getStates().length == 1);
		assertTrue(status.getWarnings().length == 0);
		assertTrue(status.getErrors().length == 0);

		final ClearCaseElementState state = status.getStates()[0];
		assertEquals(element1, state.getElement());
		assertEquals(EnumSet.of(ElementStatus.MOVED), state.getState());
	}

}
