package net.sourceforge.eclipseccase.jni;

public class Clearcase
{
	public static class Status
	{
		public boolean status;
		public String message;

		public Status(boolean status, String message)
		{
			this.status = status;
			this.message = message;
		}
	}

	/**
	 Does a clearcase checkout of the given file.  Comment can be
	 empty string.  If reserved is true, does a reserved checkout.
	 ptime preserves file timestamp
	 */
	public static   native Status checkout(
		String file,
		String comment,
		boolean reserved,
		boolean ptime);

	/**
	 Does a clearcase checkin of the given file.  Comment can be
	 empty string.  ptime preserves file timestamp
	 */
	public static native Status checkin(String file, String comment, boolean ptime);

	/**
	 Does a clearcase uncheckout of the given file.  If keep is true,
	 the file is copied to a ".keep" file
	 */
	public static native Status uncheckout(String file, boolean keep);

	/**
	 Adds the given file to clearcase source control.  This requires
	 the parent directory to be under version control and checked
	 out.  The isdirectory flag causes creation of a directory element
	 when true.  Comment can be empty string.
	 */
	public static native Status add(String file, String comment, boolean isdirectory);

	/**
	 Removes the given file from clearcase source control (rmname NOT
	 rmelem).  This requires the parent directory to be under version
	 control and checked out.  Comment can be empty string.
	 */
	public static native Status delete(String file, String comment);

	/** Moves file to newfile.  The parent directories of both file and newfile must be checked out.  Comment can be empty string. */
	public static native Status move(String file, String newfile, String comment);

	/**
	 Executes the command "cmd" just like a command line "cleartool cmd".
	 */
	public static native Status cleartool(String cmd);

	/**
	 Returns true if the file is under version control and checked
	 out
	 */
	public static native boolean isCheckedOut(String file);

	/**
	 Returns true if the file is under clearcase version control
	 */
	public static native boolean isElement(String file);

	/**
	 Returns true if the file is checked out and different from its
	 predecessor
	 */
	public static native boolean isDifferent(String file);

	/**
	 Returns true if the file is under version control and part of a snapshot view
	 */
	public static native boolean isSnapShot(String file);

	/**
	 Returns true if the file is under version control and hijacked from a snapshot view
	 */
	public static native boolean isHijacked(String file);

	/** For testing puposes only */
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("Usage: Clearcase existing_ccase_elt nonexisting_ccase_elt");
			System.exit(1);
		}
		String file = args[0];
		System.out.println("isElement: " + isElement(file));
		System.out.println("isCheckedOut: " + isCheckedOut(file));
		System.out.println("checkout: " + checkout(file, "", false, true).message);
		System.out.println("isCheckedOut: " + isCheckedOut(file));
		System.out.println("uncheckout: " + uncheckout(file, false).message);
		System.out.println("isCheckedOut: " + isCheckedOut(file));

		if (args.length > 1)
		{
			String newfile = args[1];
			System.out.println("isElement: " + isElement(newfile));
			System.out.println("add: " + add(newfile, "", false).message);
			System.out.println("isElement: " + isElement(newfile));
			System.out.println("checkin: " + checkin(newfile, "", true).message);
			System.out.println("delete: " + delete(newfile, "").message);
			System.out.println("isElement: " + isElement(newfile));
		}
	}

	/** Completely static class so constructor is hidden */
	private Clearcase()
	{
	}

	/** Initializes the clearcase library **/
	private static native void initialize();

	static {
		System.loadLibrary("ccjni");
		initialize();
	}

}