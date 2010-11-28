package net.sourceforge.transparent;

import net.sourceforge.clearcase.simple.ClearcaseException;
import net.sourceforge.clearcase.simple.ClearcaseFactory;

public class NewCommandLineClearCase extends AbstractClearCase {

   public NewCommandLineClearCase() throws ClearcaseException {
      super(ClearcaseFactory.CLI);
   }

}