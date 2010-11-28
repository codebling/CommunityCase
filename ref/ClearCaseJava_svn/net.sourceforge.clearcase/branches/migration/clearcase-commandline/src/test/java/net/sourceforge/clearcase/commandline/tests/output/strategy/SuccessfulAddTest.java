package net.sourceforge.clearcase.commandline.tests.output.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import net.sourceforge.clearcase.commandline.output.AbstractMatcherStrategy;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulAdd;
import net.sourceforge.clearcase.enums.ElementStatus;
import net.sourceforge.clearcase.status.ClearCaseElementState;
import net.sourceforge.clearcase.status.ClearCaseStatus;

import org.junit.Test;

public class SuccessfulAddTest {

	@Test
	public void testCheck() {
		AbstractMatcherStrategy strategy = new SuccessfulAdd();
		String element = "c:\\test";
		StringBuffer output = new StringBuffer("Created element \"" + element
				+ "\"");
		ClearCaseStatus status = new ClearCaseStatus();
		status = strategy.check(output, status);
		assertTrue(status.getStates().length == 1);
		assertTrue(status.getWarnings().length == 0);
		assertTrue(status.getErrors().length == 0);

		ClearCaseElementState state = status.getStates()[0];
		assertEquals(element, state.getElement());
		assertEquals(EnumSet.of(ElementStatus.IS_ELEMENT), state.getState());
	}

}
