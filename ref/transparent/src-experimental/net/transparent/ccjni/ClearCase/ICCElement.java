// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCElement extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7ED2-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCElement.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCElement() throws COMException {
        super();
    }
    public ICCElement(String progid) throws COMException {
        super(progid);
    }
    public ICCElement(IUnknown other) throws COMException {
        super(other);
    }
    public ICCElement(GUID ClsID) throws COMException {
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
    public java.lang.String getPath() throws COMException {
        return (java.lang.String) get("Path");
    }
    public java.lang.String getExtendedPath() throws COMException {
        return (java.lang.String) get("ExtendedPath");
    }
    public java.lang.String getExtendedPathInView(ICCView pView) throws COMException {
        return (java.lang.String) getN("ExtendedPathInView", new Object[]{pView});
    }
    public boolean getIsDirectory() throws COMException {
        return ((java.lang.Boolean) get("IsDirectory")).booleanValue();
    }
    public java.lang.String getPathInView(ICCView pView) throws COMException {
        return (java.lang.String) getN("PathInView", new Object[]{pView});
    }
    public ICCView getView() throws COMException {
        return new ICCView((DispatchPtr) get("View"));
    }
    public ICCVOB getVOB() throws COMException {
        return new ICCVOB((DispatchPtr) get("VOB"));
    }
    public ICCCheckedOutFile getCheckedOutFile() throws COMException {
        return new ICCCheckedOutFile((DispatchPtr) get("CheckedOutFile"));
    }
    public void CreateLock(java.lang.String Comment, boolean Obsolete) throws COMException {
        invokeN("CreateLock", new Object[]{Comment, new java.lang.Boolean(Obsolete)}, 2);
    }
    public void CreateLock(java.lang.String Comment, boolean Obsolete, java.lang.Object ExemptUsersStringArray) throws COMException {
        invokeN("CreateLock", new Object[]{Comment, new java.lang.Boolean(Obsolete), ExemptUsersStringArray}, 3);
    }
    public java.lang.String getElementType() throws COMException {
        return (java.lang.String) get("ElementType");
    }
    public java.lang.String getGroup() throws COMException {
        return (java.lang.String) get("Group");
    }
    public ICCLock getLock() throws COMException {
        return new ICCLock((DispatchPtr) get("Lock"));
    }
    public java.lang.String getMaster() throws COMException {
        return (java.lang.String) get("Master");
    }
    public void Move(ICCElement pNewParent, java.lang.String Comment) throws COMException {
        invokeN("Move", new Object[]{pNewParent, Comment}, 2);
    }
    public java.lang.String getOwner() throws COMException {
        return (java.lang.String) get("Owner");
    }
    public ICCElement getParent() throws COMException {
        return new ICCElement((DispatchPtr) get("Parent"));
    }
    public int getPermissions() throws COMException {
        return ((java.lang.Integer) get("Permissions")).intValue();
    }
    public void RemoveElement(java.lang.String Comment) throws COMException {
        invokeN("RemoveElement", new Object[]{Comment}, 1);
    }
    public void RemoveName(java.lang.String Comment, boolean DirectoryMustBeCheckedOut) throws COMException {
        invokeN("RemoveName", new Object[]{Comment, new java.lang.Boolean(DirectoryMustBeCheckedOut)}, 2);
    }
    public void Rename(java.lang.String NewName, java.lang.String Comment) throws COMException {
        invokeN("Rename", new Object[]{NewName, Comment}, 2);
    }
    public void SetGroup(java.lang.String NewGroup, java.lang.String Comment) throws COMException {
        invokeN("SetGroup", new Object[]{NewGroup, Comment}, 2);
    }
    public void SetMaster(java.lang.String Replica, java.lang.String Comment) throws COMException {
        invokeN("SetMaster", new Object[]{Replica, Comment}, 2);
    }
    public void SetOwner(java.lang.String NewOwner, java.lang.String Comment) throws COMException {
        invokeN("SetOwner", new Object[]{NewOwner, Comment}, 2);
    }
    public void SetPermissions(int NewPermissions, java.lang.String Comment) throws COMException {
        invokeN("SetPermissions", new Object[]{new java.lang.Integer(NewPermissions), Comment}, 2);
    }
    public ICCTrigger getTrigger(java.lang.String TriggerType) throws COMException {
        return new ICCTrigger((DispatchPtr) getN("Trigger", new Object[]{TriggerType}));
    }
    public ICCTriggers getTriggers() throws COMException {
        return new ICCTriggers((DispatchPtr) get("Triggers"));
    }
    public ICCVersion getVersion(java.lang.String Selector) throws COMException {
        return new ICCVersion((DispatchPtr) getN("Version", new Object[]{Selector}));
    }
}
