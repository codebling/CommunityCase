// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCTrigger extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7EF5-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCTrigger.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCTrigger() throws COMException {
        super();
    }
    public ICCTrigger(String progid) throws COMException {
        super(progid);
    }
    public ICCTrigger(IUnknown other) throws COMException {
        super(other);
    }
    public ICCTrigger(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public boolean getIsOnAttachedList() throws COMException {
        return ((java.lang.Boolean) get("IsOnAttachedList")).booleanValue();
    }
    public boolean getIsOnInheritanceList() throws COMException {
        return ((java.lang.Boolean) get("IsOnInheritanceList")).booleanValue();
    }
    public void Remove(java.lang.String Comment, boolean Recurse) throws COMException {
        invokeN("Remove", new Object[]{Comment, new java.lang.Boolean(Recurse)}, 2);
    }
    public void Remove(java.lang.String Comment, boolean Recurse, java.lang.Object DirectoryListsSubset) throws COMException {
        invokeN("Remove", new Object[]{Comment, new java.lang.Boolean(Recurse), DirectoryListsSubset}, 3);
    }
    public ICCTriggerType getType() throws COMException {
        return new ICCTriggerType((DispatchPtr) get("Type"));
    }
    public ICCVOB getVOB() throws COMException {
        return new ICCVOB((DispatchPtr) get("VOB"));
    }
}
