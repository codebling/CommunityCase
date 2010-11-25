package net.sourceforge.transparent;

import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.plugins.util.CommandUtil;
import org.intellij.plugins.util.FileUtil;

import java.util.Arrays;
import java.io.IOException;

/**
 * User: sg426575
 * Date: Aug 29, 2003
 * Time: 9:49:36 AM
 */
public class CheckOutHelper {
   private TransparentVcs vcs;
   private AbstractVcsHelper vcsHelper;
   private TransparentConfiguration config;
   private FileUtil fileUtil;

   public CheckOutHelper(TransparentVcs transparentVcs) {
      this(transparentVcs,
           AbstractVcsHelper.getInstance(transparentVcs.getProject()),
           transparentVcs.getTransparentConfig(),
           new FileUtil());
   }

   public CheckOutHelper(TransparentVcs transparentVcs,
                         AbstractVcsHelper vcsHelper,
                         TransparentConfiguration transparentConfig,
                         FileUtil fileUtil) {
      this.vcs = transparentVcs;
      this.vcsHelper = vcsHelper;
      this.config = transparentConfig;
      this.fileUtil = fileUtil;
   }

   public void checkOutOrHijackFile(VirtualFile file) {
      try {
         makeFileWritable(file);
      } catch (Throwable e) {
         vcsHelper.showErrors(
            Arrays.asList(new VcsException[]{new VcsException(e)}),
            "Exception while " + (shouldHijackFile(file) ? "hijacking " : "checking out ") + file.getPresentableUrl());
      }
   }

   public boolean shouldHijackFile(VirtualFile file) {
      return config.offline || !isElement(file);
   }

   private boolean isElement(VirtualFile file) {
      return new ClearCaseFile(file, vcs.getClearCase()).isElement();
   }

   private void makeFileWritable(final VirtualFile file) throws VcsException {
      if (shouldHijackFile(file)) {
         hijackFile(file);
      } else {
         vcs.checkoutFile(file.getPresentableUrl(), false);
      }
   }

   public void hijackFile(final VirtualFile file) throws VcsException {
      try {
         fileUtil.setFileWritability(file, true);

      } catch (Exception e) {
         throw new VcsException(e);
      }
   }

}
