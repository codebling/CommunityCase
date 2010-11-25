package net.sourceforge.transparent;

public class CheckedOutStatus {
	public static final CheckedOutStatus RESERVED = new CheckedOutStatus("reserved");
	public static final CheckedOutStatus UNRESERVED = new CheckedOutStatus("unreserved");
	public static final CheckedOutStatus NOT_CHECKED_OUT = new CheckedOutStatus("not checked out");
	private String _checkedOutState;

   private CheckedOutStatus(String state) {
		_checkedOutState = state;
	}

	public String toString() {
		return _checkedOutState;
	}
}
