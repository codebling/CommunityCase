package net.sourceforce.clearcase.tests;

import static org.junit.Assert.*;
import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseFactory;
import net.sourceforge.clearcase.impl.ClearCaseDefaultImpl;
import net.sourceforge.clearcase.impl.ClearCaseMgr;

import org.junit.After;
import org.junit.Test;

public class ClearCaseMgrTest {

	private static void register() {
		ClearCaseFactory fakeFactory = new ClearCaseFactory() {

			public String getId() {
				return "test";
			}

			public ClearCase create() {
				return new ClearCaseDefaultImpl() {
				
					@Override
					public String getName() {
						return "test";
					}
				};
			}
		};
		ClearCaseMgr.register(fakeFactory);
	}

	@After
	public void tearDown() throws Exception {
		// clear the ClearCaseMgr after each test
		ClearCaseMgr.clearImplementations();
	}

	@Test
	public void testRegister() {
		register();
		try {
			register();
			fail("A runtime Exception must be thrown because of duplicate factory");
		} catch (RuntimeException e) {
		}
	}

	@Test
	public void testGetAvailableImplementations() {
		String[] impls;
		// No initial implementation
		impls = ClearCaseMgr.getAvailableImplementations();
		assertArrayEquals(new String[] {}, impls);
		// Add an implementation and check its id is available
		register();
		impls = ClearCaseMgr.getAvailableImplementations();
		assertArrayEquals(new String[] { "test" }, impls);
	}

	@Test
	public void testGetImplementation() {
		register();
		ClearCase impl = ClearCaseMgr.getImplementation("test");
		assertTrue(impl != null);
	}

}
