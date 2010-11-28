package net.sourceforge.transparent;

public class Status {
	public static final Status CHECKED_OUT = new Status("checked out");
	public static final Status CHECKED_IN= new Status("checked in");
	public static final Status NOT_AN_ELEMENT = new Status("not an element");
	public static final Status HIJACKED = new Status("hijacked");
	private String _state;

   private Status(String state) {
		_state = state;
	}

	public String toString() {
		return _state;
	}
}
