package net.sourceforge.transparent;

import net.sourceforge.clearcase.simple.ClearcaseException;
import net.sourceforge.clearcase.simple.ClearcaseFactory;

public class NewNativeClearCase extends AbstractClearCase {

   public NewNativeClearCase() throws ClearcaseException {
      super(ClearcaseFactory.JNI);
   }

}