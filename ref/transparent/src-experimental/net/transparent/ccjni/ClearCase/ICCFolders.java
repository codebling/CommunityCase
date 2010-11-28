// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCFolders extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7F42-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCFolders.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCFolders() throws COMException {
        super();
    }
    public ICCFolders(String progid) throws COMException {
        super(progid);
    }
    public ICCFolders(IUnknown other) throws COMException {
        super(other);
    }
    public ICCFolders(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public ICCFolder getItem(int index) throws COMException {
        return new ICCFolder((DispatchPtr) getN("Item", new Object[]{new java.lang.Integer(index)}));
    }
    public void Add(ICCFolder pFolder) throws COMException {
        invokeN("Add", new Object[]{pFolder}, 1);
    }
    public int getCount() throws COMException {
        return ((java.lang.Integer) get("Count")).intValue();
    }
    public void Remove(int index) throws COMException {
        invokeN("Remove", new Object[]{new java.lang.Integer(index)}, 1);
    }
    public java.lang.String getInitErrors() throws COMException {
        return (java.lang.String) get("InitErrors");
    }
    public DispatchPtr get_NewEnum() throws COMException {
        return (DispatchPtr) get("_NewEnum");
    }
}
