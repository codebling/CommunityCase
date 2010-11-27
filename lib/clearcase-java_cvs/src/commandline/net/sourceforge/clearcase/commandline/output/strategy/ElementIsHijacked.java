package net.sourceforge.clearcase.commandline.output.strategy;

import java.util.regex.Matcher;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseException;

/**
 * This
 */
public class ElementIsHijacked extends AbstractMatcherStrategy {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPattern() {
		return "^\\s*version\\s+(.*)@@(\\S*)\\s+\\[hijacked[^\\]]*\\].*$";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ClearCaseElementState getResult(String output, Matcher matcher)
			throws ClearCaseException {
		String element = matcher.group(1);
		String version = matcher.group(2);
		return new ClearCaseElementState(element, ClearCase.HIJACKED
				| ClearCase.IS_ELEMENT, version);
	}

}
