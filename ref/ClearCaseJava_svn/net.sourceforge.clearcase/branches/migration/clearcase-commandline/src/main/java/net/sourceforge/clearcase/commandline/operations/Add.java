package net.sourceforge.clearcase.commandline.operations;

import net.sourceforge.clearcase.commandline.output.PatternMatcherStrategy;
import net.sourceforge.clearcase.status.ClearCaseStatus;

public class Add extends AbstractOperation {

	@Override
	protected String[] executeCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ClearCaseStatus parseOutput(final String[] output) {
		final PatternMatcherStrategy strategy = CompositeStrategyUtil
				.getAllStrategies();
		ClearCaseStatus status = null;
		for (final String s : output) {
			status = strategy.check(new StringBuffer(s), status);
		}
		return status;
	}

	@Override
	protected void validateInput() {
		for (final String element : getElements()) {
			if (element == null) {
				throw new IllegalArgumentException(
						"One of the provided element is null");
			}
		}
	}

}
