package net.sourceforge.clearcase.commandline.operations;

import net.sourceforge.clearcase.commandline.output.PatternMatcherStrategy;
import net.sourceforge.clearcase.commandline.output.strategy.DirectoryIsNotCheckedOut;
import net.sourceforge.clearcase.commandline.output.strategy.ElementAlreadyCheckedOut;
import net.sourceforge.clearcase.commandline.output.strategy.ElementAlreadyExists;
import net.sourceforge.clearcase.commandline.output.strategy.ElementCheckedOutReservedInOtherView;
import net.sourceforge.clearcase.commandline.output.strategy.ElementHasCheckOuts;
import net.sourceforge.clearcase.commandline.output.strategy.IdenticalPredecessor;
import net.sourceforge.clearcase.commandline.output.strategy.MatchStrategyComposite;
import net.sourceforge.clearcase.commandline.output.strategy.MostRecentNotPredecessor;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulAdd;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulCheckin;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulCheckout;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulDelete;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulMove;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulUncheckout;
import net.sourceforge.clearcase.commandline.output.strategy.UnableToCreateElement;

public final class CompositeStrategyUtil {
	private static PatternMatcherStrategy allStrategies;

	public static synchronized PatternMatcherStrategy getAllStrategies() {
		if (allStrategies == null) {
			final MatchStrategyComposite strategy = new MatchStrategyComposite();
			strategy.addStrategy(new DirectoryIsNotCheckedOut());
			strategy.addStrategy(new ElementAlreadyCheckedOut());
			strategy.addStrategy(new ElementAlreadyExists());
			strategy.addStrategy(new ElementCheckedOutReservedInOtherView());
			strategy.addStrategy(new ElementHasCheckOuts());
			strategy.addStrategy(new IdenticalPredecessor());
			strategy.addStrategy(new MostRecentNotPredecessor());
			strategy.addStrategy(new SuccessfulAdd());
			strategy.addStrategy(new SuccessfulCheckin());
			strategy.addStrategy(new SuccessfulCheckout());
			strategy.addStrategy(new SuccessfulDelete());
			strategy.addStrategy(new SuccessfulMove());
			strategy.addStrategy(new SuccessfulUncheckout());
			strategy.addStrategy(new UnableToCreateElement());
		}
		return allStrategies;
	}
}
