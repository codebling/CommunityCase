// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCComponents extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7F43-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCComponents.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCComponents() throws COMException {
        super();
    }
    public ICCComponents(String progid) throws COMException {
        super(progid);
    }
    public ICCComponents(IUnknown other) throws COMException {
        super(other);
    }
    public ICCComponents(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public ICCComponent getItem(int index) throws COMException {
        return new ICCComponent((DispatchPtr) getN("Item", new Object[]{new java.lang.Integer(index)}));
    }
    public void Add(ICCComponent pComponent) throws COMException {
        invokeN("Add", new Object[]{pComponent}, 1);
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
