// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCAttribute extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7EED-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCAttribute.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCAttribute() throws COMException {
        super();
    }
    public ICCAttribute(String progid) throws COMException {
        super(progid);
    }
    public ICCAttribute(IUnknown other) throws COMException {
        super(other);
    }
    public ICCAttribute(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public void Remove(java.lang.String Comment) throws COMException {
        invokeN("Remove", new Object[]{Comment}, 1);
    }
    public void SetValue(java.lang.Object Value, java.lang.String Comment) throws COMException {
        invokeN("SetValue", new Object[]{Value, Comment}, 2);
    }
    public ICCAttributeType getType() throws COMException {
        return new ICCAttributeType((DispatchPtr) get("Type"));
    }
    public java.lang.Object getValue() throws COMException {
        return (java.lang.Object) get("Value");
    }
    public ICCVOB getVOB() throws COMException {
        return new ICCVOB((DispatchPtr) get("VOB"));
    }
}
