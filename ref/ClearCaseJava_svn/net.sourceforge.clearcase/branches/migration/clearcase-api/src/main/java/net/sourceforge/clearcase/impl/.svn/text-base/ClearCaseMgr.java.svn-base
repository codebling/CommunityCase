package net.sourceforge.clearcase.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseFactory;

/**
 * This class manages the different implementations of ClearCase interface
 */
public final class ClearCaseMgr {
	private static Map<String, ClearCaseFactory> impls = new HashMap<String, ClearCaseFactory>();
	
	private ClearCaseMgr() {
		
	}
	
	public static void register(ClearCaseFactory impl) {
		if (impl == null) {
			throw new IllegalArgumentException("impl cannot be null");
		}
		if (impls.containsKey(impl.getId())){
			throw new RuntimeException("The implementation " + impl.getId() + " is already registered.");
		}
		impls.put(impl.getId(), impl);
	}
	
	public static String[] getAvailableImplementations() {
		Set<String> keys = impls.keySet();
		return keys.toArray(new String[keys.size()]);
	}
	
	public static ClearCase getImplementation(String id) {
		if (impls.containsKey(id)) {
			ClearCaseFactory factory = (ClearCaseFactory)impls.get(id);
			return factory.create();
		} else {
			throw new RuntimeException("The implementation " + id + " hasn't been registered.");
		}
	}
	
	public static void clearImplementations() {
		impls.clear();
	}
}
