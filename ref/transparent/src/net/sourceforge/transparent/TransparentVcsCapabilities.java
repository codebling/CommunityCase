package net.sourceforge.transparent;

import com.intellij.openapi.vcs.AbstractVcsCapabilities;

public class TransparentVcsCapabilities extends AbstractVcsCapabilities {
    public boolean isFileMovingSupported() {
        return true;
    }

    public boolean isFileRenamingSupported() {
        return true;
    }

    public boolean isDirectoryMovingSupported() {
        return true;
    }

    public boolean isDirectoryRenamingSupported() {
        return true;
    }

    public boolean isTransactionSupported() {
        return false;
    }

}
