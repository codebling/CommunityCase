// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCAttributes extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7EEE-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCAttributes.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCAttributes() throws COMException {
        super();
    }
    public ICCAttributes(String progid) throws COMException {
        super(progid);
    }
    public ICCAttributes(IUnknown other) throws COMException {
        super(other);
    }
    public ICCAttributes(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public ICCAttribute getItem(int index) throws COMException {
        return new ICCAttribute((DispatchPtr) getN("Item", new Object[]{new java.lang.Integer(index)}));
    }
    public void Add(ICCAttribute pAttribute) throws COMException {
        invokeN("Add", new Object[]{pAttribute}, 1);
    }
    public int getCount() throws COMException {
        return ((java.lang.Integer) get("Count")).intValue();
    }
    public void Remove(int index) throws COMException {
        invokeN("Remove", new Object[]{new java.lang.Integer(index)}, 1);
    }
    public DispatchPtr get_NewEnum() throws COMException {
        return (DispatchPtr) get("_NewEnum");
    }
}
