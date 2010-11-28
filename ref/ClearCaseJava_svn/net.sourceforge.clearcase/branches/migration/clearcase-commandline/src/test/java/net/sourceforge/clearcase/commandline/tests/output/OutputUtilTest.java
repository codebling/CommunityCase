package net.sourceforge.clearcase.commandline.tests.output;

import static org.junit.Assert.*;

import net.sourceforge.clearcase.commandline.output.OutputUtil;

import org.junit.Test;

public class OutputUtilTest {

	private static final String TEST = "test";
	private static final String QUOTE = "\"";
	private static final String A = "a";
	private static final String IS = "is";
	private static final String THIS = "This";
	private static final String THIS_IS_A_TEST_ALL_QUOTED = "\"This\" \"is\" \"a\" \"test\"";
	private static final String THIS_IS_A_TEST = "This is a test";

	@Test
	public void testGetElementNameBetweenQuotation() {
		String[] r;
		r = OutputUtil.getElementNameBetweenQuotation(QUOTE + THIS_IS_A_TEST
				+ QUOTE);
		assertArrayEquals(new String[] { THIS_IS_A_TEST }, r);
		r = OutputUtil
				.getElementNameBetweenQuotation(THIS_IS_A_TEST_ALL_QUOTED);
		assertArrayEquals(new String[] { THIS, IS, A, TEST }, r);
	}

	@Test
	public void testGetFirstElementNameBetweenQuotation() {
		String r;
		r = OutputUtil.getFirstElementNameBetweenQuotation(QUOTE
				+ THIS_IS_A_TEST + QUOTE);
		assertEquals(THIS_IS_A_TEST, r);
		r = OutputUtil
				.getFirstElementNameBetweenQuotation(THIS_IS_A_TEST_ALL_QUOTED);
		assertEquals(THIS, r);
	}

}
