// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCLabel extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7EE5-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCLabel.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCLabel() throws COMException {
        super();
    }
    public ICCLabel(String progid) throws COMException {
        super(progid);
    }
    public ICCLabel(IUnknown other) throws COMException {
        super(other);
    }
    public ICCLabel(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public void Remove(java.lang.String Comment) throws COMException {
        invokeN("Remove", new Object[]{Comment}, 1);
    }
    public ICCLabelType getType() throws COMException {
        return new ICCLabelType((DispatchPtr) get("Type"));
    }
    public ICCVOB getVOB() throws COMException {
        return new ICCVOB((DispatchPtr) get("VOB"));
    }
}
