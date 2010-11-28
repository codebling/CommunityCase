// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCCheckedOutFileQuery extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7EDC-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCCheckedOutFileQuery.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCCheckedOutFileQuery() throws COMException {
        super();
    }
    public ICCCheckedOutFileQuery(String progid) throws COMException {
        super(progid);
    }
    public ICCCheckedOutFileQuery(IUnknown other) throws COMException {
        super(other);
    }
    public ICCCheckedOutFileQuery(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public ICCCheckedOutFiles Apply() throws COMException {
        return new ICCCheckedOutFiles((DispatchPtr) invoke("Apply"));
    }
    public java.lang.String getBranchType() throws COMException {
        return (java.lang.String) get("BranchType");
    }
    public void setBranchType(java.lang.String newBranchType) throws COMException {
        put("BranchType", newBranchType);
    }
    public boolean getExamineAllReplicas() throws COMException {
        return ((java.lang.Boolean) get("ExamineAllReplicas")).booleanValue();
    }
    public void setExamineAllReplicas(boolean newExamineAllReplicas) throws COMException {
        put("ExamineAllReplicas", new java.lang.Boolean(newExamineAllReplicas));
    }
    public java.lang.Object getPathArray() throws COMException {
        return (java.lang.Object) get("PathArray");
    }
    public void setPathArray(java.lang.Object newPathArray) throws COMException {
        put("PathArray", newPathArray);
    }
    public int getPathSelects() throws COMException {
        return ((java.lang.Integer) get("PathSelects")).intValue();
    }
    public void setPathSelects(int newPathSelects) throws COMException {
        put("PathSelects", new java.lang.Integer(newPathSelects));
    }
    public boolean getUseCurrentView() throws COMException {
        return ((java.lang.Boolean) get("UseCurrentView")).booleanValue();
    }
    public void setUseCurrentView(boolean newUseCurrentView) throws COMException {
        put("UseCurrentView", new java.lang.Boolean(newUseCurrentView));
    }
    public java.lang.String getUser() throws COMException {
        return (java.lang.String) get("User");
    }
    public void setUser(java.lang.String newUser) throws COMException {
        put("User", newUser);
    }
}
