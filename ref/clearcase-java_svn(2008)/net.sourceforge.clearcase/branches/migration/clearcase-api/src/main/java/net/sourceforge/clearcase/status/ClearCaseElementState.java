package net.sourceforge.clearcase.status;

import java.util.EnumSet;

import net.sourceforge.clearcase.enums.ElementStatus;

/**
 * This class holds data about element states.
 */
public final class ClearCaseElementState {

	/** the element. */
	private final String element;

	/** the target of a link (maybe <code>null</code>). */
	private final String linkTarget;

	/** the element state. */
	private final EnumSet<ElementStatus> state;

	/** the element version (maybe <code>null</code>). */
	private final String version;

	/**
	 * Creates a new instance.
	 * <p>
	 * <b>NOTE: </b> Although this method is exposed as API it is not expected
	 * to be called from outside the ClearCase Java API.
	 * </p>
	 * 
	 * @param element
	 * @param state
	 */
	public ClearCaseElementState(final String element, final EnumSet<ElementStatus> state) {
		this.element = element;
		this.state = state;
		this.version = null;
		this.linkTarget = null;
	}
	
	/**
	 * Creates a new instance.
	 * <p>
	 * <b>NOTE: </b> Although this method is exposed as API it is not expected
	 * to be called from outside the ClearCase Java API.
	 * </p>
	 * 
	 * @param element
	 * @param state
	 */
	public ClearCaseElementState(final String element, final ElementStatus state) {
		this.element = element;
		this.state = EnumSet.of(state);
		this.version = null;
		this.linkTarget = null;
	}

	/**
	 * Creates a new instance.
	 * <p>
	 * <b>NOTE: </b> Although this method is exposed as API it is not expected
	 * to be called from outside the ClearCase Java API.
	 * </p>
	 * 
	 * @param element
	 * @param state
	 * @param version
	 */
	public ClearCaseElementState(final String element, final EnumSet<ElementStatus> state,
			final String version) {
		this.element = element;
		this.state = state;
		this.version = version;
		this.linkTarget = null;
	}
	
	/**
	 * Creates a new instance.
	 * <p>
	 * <b>NOTE: </b> Although this method is exposed as API it is not expected
	 * to be called from outside the ClearCase Java API.
	 * </p>
	 * 
	 * @param element
	 * @param state
	 * @param version
	 */
	public ClearCaseElementState(final String element, final ElementStatus state,
			final String version) {
		this.element = element;
		this.state = EnumSet.of(state);
		this.version = version;
		this.linkTarget = null;
	}

	/**
	 * Creates a new instance.
	 * <p>
	 * <b>NOTE: </b> Although this method is exposed as API it is not expected
	 * to be called from outside the ClearCase Java API.
	 * </p>
	 * 
	 * @param element
	 * @param state
	 * @param version
	 */
	public ClearCaseElementState(final String element, final EnumSet<ElementStatus> state,
			final String version, final String linkTarget) {
		this.element = element;
		this.state = state;
		this.version = version;
		this.linkTarget = linkTarget;
	}
	
	/**
	 * Creates a new instance.
	 * <p>
	 * <b>NOTE: </b> Although this method is exposed as API it is not expected
	 * to be called from outside the ClearCase Java API.
	 * </p>
	 * 
	 * @param element
	 * @param state
	 * @param version
	 */
	public ClearCaseElementState(final String element, final ElementStatus state,
			final String version, final String linkTarget) {
		this.element = element;
		this.state = EnumSet.of(state);
		this.version = version;
		this.linkTarget = linkTarget;
	}

	public String getElement() {
		return element;
	}

	public String getLinkTarget() {
		return linkTarget;
	}

	public EnumSet<ElementStatus> getState() {
		return state;
	}

	public String getVersion() {
		return version;
	}
}
