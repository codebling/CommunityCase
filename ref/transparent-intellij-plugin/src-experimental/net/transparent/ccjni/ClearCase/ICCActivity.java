// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCActivity extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7EF9-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCActivity.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCActivity() throws COMException {
        super();
    }
    public ICCActivity(String progid) throws COMException {
        super(progid);
    }
    public ICCActivity(IUnknown other) throws COMException {
        super(other);
    }
    public ICCActivity(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public ICCAttribute getAttribute(java.lang.String AttributeType) throws COMException {
        return new ICCAttribute((DispatchPtr) getN("Attribute", new Object[]{AttributeType}));
    }
    public ICCAttributes getAttributes() throws COMException {
        return new ICCAttributes((DispatchPtr) get("Attributes"));
    }
    public java.lang.String getComment() throws COMException {
        return (java.lang.String) get("Comment");
    }
    public void setComment(java.lang.String newComment) throws COMException {
        put("Comment", newComment);
    }
    public ICCHistoryRecord getCreationRecord() throws COMException {
        return new ICCHistoryRecord((DispatchPtr) get("CreationRecord"));
    }
    public ICCHistoryRecords getHistoryRecords(ICCBranchType pICCBranchType, java.util.Date Since, java.lang.String User, boolean Minor, boolean ExcludeCheckOutEvents, boolean Recurse, boolean DirectoryOnly) throws COMException {
        return new ICCHistoryRecords((DispatchPtr) getN("HistoryRecords", new Object[]{pICCBranchType, Since, User, new java.lang.Boolean(Minor), new java.lang.Boolean(ExcludeCheckOutEvents), new java.lang.Boolean(Recurse), new java.lang.Boolean(DirectoryOnly)}));
    }
    public ICCHyperlinks getHyperlinks(java.lang.String HyperlinkType) throws COMException {
        return new ICCHyperlinks((DispatchPtr) getN("Hyperlinks", new Object[]{HyperlinkType}));
    }
    public java.lang.String getOID() throws COMException {
        return (java.lang.String) get("OID");
    }
    public java.lang.String getVOBFamilyUUID() throws COMException {
        return (java.lang.String) get("VOBFamilyUUID");
    }
    public java.lang.String getName() throws COMException {
        return (java.lang.String) get("Name");
    }
    public ICCVersions getChangeSet(ICCView pNameResolverView, boolean FailIfErrors) throws COMException {
        return new ICCVersions((DispatchPtr) getN("ChangeSet", new Object[]{pNameResolverView, new java.lang.Boolean(FailIfErrors)}));
    }
    public void CreateLock(java.lang.String Comment, boolean Obsolete) throws COMException {
        invokeN("CreateLock", new Object[]{Comment, new java.lang.Boolean(Obsolete)}, 2);
    }
    public void CreateLock(java.lang.String Comment, boolean Obsolete, java.lang.Object ExemptUsersStringArray) throws COMException {
        invokeN("CreateLock", new Object[]{Comment, new java.lang.Boolean(Obsolete), ExemptUsersStringArray}, 3);
    }
    public java.lang.String getHeadline() throws COMException {
        return (java.lang.String) get("Headline");
    }
    public ICCLock getLock() throws COMException {
        return new ICCLock((DispatchPtr) get("Lock"));
    }
    public java.lang.String getMaster() throws COMException {
        return (java.lang.String) get("Master");
    }
    public ICCView getNameResolverView() throws COMException {
        return new ICCView((DispatchPtr) get("NameResolverView"));
    }
    public void SetMaster(java.lang.String Replica, java.lang.String Comment) throws COMException {
        invokeN("SetMaster", new Object[]{Replica, Comment}, 2);
    }
    public ICCVOB getVOB() throws COMException {
        return new ICCVOB((DispatchPtr) get("VOB"));
    }
    public java.lang.String getClearQuestRecordID() throws COMException {
        return (java.lang.String) get("ClearQuestRecordID");
    }
    public java.lang.String getGroup() throws COMException {
        return (java.lang.String) get("Group");
    }
    public boolean getHasChangeSet() throws COMException {
        return ((java.lang.Boolean) get("HasChangeSet")).booleanValue();
    }
    public java.lang.String getOwner() throws COMException {
        return (java.lang.String) get("Owner");
    }
    public void SetGroup(java.lang.String NewGroup, java.lang.String Comment) throws COMException {
        invokeN("SetGroup", new Object[]{NewGroup, Comment}, 2);
    }
    public void SetHeadline(java.lang.String NewHeadline, java.lang.String Comment) throws COMException {
        invokeN("SetHeadline", new Object[]{NewHeadline, Comment}, 2);
    }
    public void SetName(java.lang.String NewName, java.lang.String Comment) throws COMException {
        invokeN("SetName", new Object[]{NewName, Comment}, 2);
    }
    public void SetOwner(java.lang.String NewOwner, java.lang.String Comment) throws COMException {
        invokeN("SetOwner", new Object[]{NewOwner, Comment}, 2);
    }
    public ICCStream getStream() throws COMException {
        return new ICCStream((DispatchPtr) get("Stream"));
    }
}
