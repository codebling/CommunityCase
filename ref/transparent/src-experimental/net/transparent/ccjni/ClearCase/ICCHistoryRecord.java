// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCHistoryRecord extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7ECF-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCHistoryRecord.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCHistoryRecord() throws COMException {
        super();
    }
    public ICCHistoryRecord(String progid) throws COMException {
        super(progid);
    }
    public ICCHistoryRecord(IUnknown other) throws COMException {
        super(other);
    }
    public ICCHistoryRecord(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public java.lang.String getUserLoginName() throws COMException {
        return (java.lang.String) get("UserLoginName");
    }
    public java.lang.String getUserFullName() throws COMException {
        return (java.lang.String) get("UserFullName");
    }
    public java.lang.String getGroup() throws COMException {
        return (java.lang.String) get("Group");
    }
    public java.lang.String getComment() throws COMException {
        return (java.lang.String) get("Comment");
    }
    public void setComment(java.lang.String newComment) throws COMException {
        put("Comment", newComment);
    }
    public java.lang.String getHost() throws COMException {
        return (java.lang.String) get("Host");
    }
    public java.lang.String getEventKind() throws COMException {
        return (java.lang.String) get("EventKind");
    }
    public java.util.Date getDate() throws COMException {
        return (java.util.Date) get("Date");
    }
    public ICCVOB getVOB() throws COMException {
        return new ICCVOB((DispatchPtr) get("VOB"));
    }
}
