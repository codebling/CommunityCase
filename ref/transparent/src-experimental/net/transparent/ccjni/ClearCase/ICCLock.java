// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCLock extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7EF7-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCLock.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCLock() throws COMException {
        super();
    }
    public ICCLock(String progid) throws COMException {
        super(progid);
    }
    public ICCLock(IUnknown other) throws COMException {
        super(other);
    }
    public ICCLock(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public ICCHistoryRecord getCreationRecord() throws COMException {
        return new ICCHistoryRecord((DispatchPtr) get("CreationRecord"));
    }
    public java.lang.Object getExemptUsersStringArray() throws COMException {
        return (java.lang.Object) get("ExemptUsersStringArray");
    }
    public boolean getIsObsolete() throws COMException {
        return ((java.lang.Boolean) get("IsObsolete")).booleanValue();
    }
    public ICCVOBObject getLockedObject() throws COMException {
        return new ICCVOBObject((DispatchPtr) get("LockedObject"));
    }
    public int getNumberOfExemptUsers() throws COMException {
        return ((java.lang.Integer) get("NumberOfExemptUsers")).intValue();
    }
    public void Remove(java.lang.String Comment) throws COMException {
        invokeN("Remove", new Object[]{Comment}, 1);
    }
    public void SetExemptUsersStringArray(java.lang.Object ExemptUsersStringArray, java.lang.String Comment) throws COMException {
        invokeN("SetExemptUsersStringArray", new Object[]{ExemptUsersStringArray, Comment}, 2);
    }
    public void SetObsolete(boolean IsObsolete, java.lang.String Comment) throws COMException {
        invokeN("SetObsolete", new Object[]{new java.lang.Boolean(IsObsolete), Comment}, 2);
    }
    public ICCVOB getVOB() throws COMException {
        return new ICCVOB((DispatchPtr) get("VOB"));
    }
}
