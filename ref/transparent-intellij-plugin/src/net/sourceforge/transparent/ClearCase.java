package net.sourceforge.transparent;

import java.io.File;

public interface ClearCase {

   String getName();

   void checkIn(File file, String comment);
   void checkOut(File file, boolean isReserved);
   void undoCheckOut(File file);

   void add(File file, String comment);
   void delete(File file, String comment);
   void move(File file, File target, String comment);

   Status getStatus(File file);
   boolean isElement(File file);
   boolean isCheckedOut(File file);

   void cleartool(String cmd);

    CheckedOutStatus getCheckedOutStatus(File file);

}
