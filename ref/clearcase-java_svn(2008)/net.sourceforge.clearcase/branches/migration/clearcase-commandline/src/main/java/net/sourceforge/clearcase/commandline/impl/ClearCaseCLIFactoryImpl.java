package net.sourceforge.clearcase.commandline.impl;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseFactory;

public class ClearCaseCLIFactoryImpl implements ClearCaseFactory {
	
	public static final String ID = ClearCaseCLIImpl.class.getName();

	public ClearCase create() {
		return new ClearCaseCLIImpl();
	}

	public String getId() {
		return ID;
	}

}
