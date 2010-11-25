package net.sourceforge.transparent;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jdom.Element;


public class TransparentPlugin implements ApplicationComponent, JDOMExternalizable {
	private ClearCaseFileListener _listener = new ClearCaseFileListener();

	public String getComponentName() {
		return "Transparent ClearCase Integration"; 
	}

	public void initComponent() {
		LocalFileSystem.getInstance().addVirtualFileListener(_listener);
		System.out.println(getComponentName() + " loaded");
	}

	public void disposeComponent() {
		LocalFileSystem.getInstance().removeVirtualFileListener(_listener);
	}

	public void readExternal(Element element) throws InvalidDataException {
	}

	public void writeExternal(Element element) throws WriteExternalException {
	}
}
