// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCVOB extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7ECB-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCVOB.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCVOB() throws COMException {
        super();
    }
    public ICCVOB(String progid) throws COMException {
        super(progid);
    }
    public ICCVOB(IUnknown other) throws COMException {
        super(other);
    }
    public ICCVOB(GUID ClsID) throws COMException {
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
    public java.lang.String getTagName() throws COMException {
        return (java.lang.String) get("TagName");
    }
    public ICCActivity getActivity(java.lang.String Selector) throws COMException {
        return new ICCActivity((DispatchPtr) getN("Activity", new Object[]{Selector}));
    }
    public java.lang.Object getAdditionalGroupsStringArray() throws COMException {
        return (java.lang.Object) get("AdditionalGroupsStringArray");
    }
    public ICCAttributeType getAttributeType(java.lang.String Name, boolean Local) throws COMException {
        return new ICCAttributeType((DispatchPtr) getN("AttributeType", new Object[]{Name, new java.lang.Boolean(Local)}));
    }
    public ICCAttributeTypes getAttributeTypes(boolean Local, boolean IncludeObsoletes) throws COMException {
        return new ICCAttributeTypes((DispatchPtr) getN("AttributeTypes", new Object[]{new java.lang.Boolean(Local), new java.lang.Boolean(IncludeObsoletes)}));
    }
    public ICCBranchType getBranchType(java.lang.String Name, boolean Local) throws COMException {
        return new ICCBranchType((DispatchPtr) getN("BranchType", new Object[]{Name, new java.lang.Boolean(Local)}));
    }
    public ICCBranchTypes getBranchTypes(boolean Local, boolean IncludeObsoletes) throws COMException {
        return new ICCBranchTypes((DispatchPtr) getN("BranchTypes", new Object[]{new java.lang.Boolean(Local), new java.lang.Boolean(IncludeObsoletes)}));
    }
    public ICCAttributeType CreateAttributeType(java.lang.String Name, int ValueType, java.lang.String Comment, boolean Shared, int Constraint, boolean Global, boolean Acquire) throws COMException {
        return new ICCAttributeType((DispatchPtr) invokeN("CreateAttributeType", new Object[]{Name, new java.lang.Integer(ValueType), Comment, new java.lang.Boolean(Shared), new java.lang.Integer(Constraint), new java.lang.Boolean(Global), new java.lang.Boolean(Acquire)}, 7));
    }
    public ICCBranchType CreateBranchType(java.lang.String Name, java.lang.String Comment, int Constraint, boolean Global, boolean Acquire) throws COMException {
        return new ICCBranchType((DispatchPtr) invokeN("CreateBranchType", new Object[]{Name, Comment, new java.lang.Integer(Constraint), new java.lang.Boolean(Global), new java.lang.Boolean(Acquire)}, 5));
    }
    public ICCHyperlinkType CreateHyperlinkType(java.lang.String Name, java.lang.String Comment, boolean Shared, boolean Global, boolean Acquire) throws COMException {
        return new ICCHyperlinkType((DispatchPtr) invokeN("CreateHyperlinkType", new Object[]{Name, Comment, new java.lang.Boolean(Shared), new java.lang.Boolean(Global), new java.lang.Boolean(Acquire)}, 5));
    }
    public ICCLabelType CreateLabelType(java.lang.String Name, java.lang.String Comment, boolean Shared, int Constraint, boolean Global, boolean Acquire) throws COMException {
        return new ICCLabelType((DispatchPtr) invokeN("CreateLabelType", new Object[]{Name, Comment, new java.lang.Boolean(Shared), new java.lang.Integer(Constraint), new java.lang.Boolean(Global), new java.lang.Boolean(Acquire)}, 6));
    }
    public void CreateLock(java.lang.String Comment, boolean Obsolete) throws COMException {
        invokeN("CreateLock", new Object[]{Comment, new java.lang.Boolean(Obsolete)}, 2);
    }
    public void CreateLock(java.lang.String Comment, boolean Obsolete, java.lang.Object ExemptUsersStringArray) throws COMException {
        invokeN("CreateLock", new Object[]{Comment, new java.lang.Boolean(Obsolete), ExemptUsersStringArray}, 3);
    }
    public ICCTriggerTypeBuilder CreateTriggerTypeBuilder() throws COMException {
        return new ICCTriggerTypeBuilder((DispatchPtr) invoke("CreateTriggerTypeBuilder"));
    }
    public java.lang.String getGroup() throws COMException {
        return (java.lang.String) get("Group");
    }
    public boolean getHasMSDOSTextMode() throws COMException {
        return ((java.lang.Boolean) get("HasMSDOSTextMode")).booleanValue();
    }
    public java.lang.String getHost() throws COMException {
        return (java.lang.String) get("Host");
    }
    public ICCHyperlink getHyperlink(java.lang.String IDString) throws COMException {
        return new ICCHyperlink((DispatchPtr) getN("Hyperlink", new Object[]{IDString}));
    }
    public ICCHyperlinkType getHyperlinkType(java.lang.String Name, boolean Local) throws COMException {
        return new ICCHyperlinkType((DispatchPtr) getN("HyperlinkType", new Object[]{Name, new java.lang.Boolean(Local)}));
    }
    public ICCHyperlinkTypes getHyperlinkTypes(boolean Local, boolean IncludeObsoletes) throws COMException {
        return new ICCHyperlinkTypes((DispatchPtr) getN("HyperlinkTypes", new Object[]{new java.lang.Boolean(Local), new java.lang.Boolean(IncludeObsoletes)}));
    }
    public boolean getIsMounted() throws COMException {
        return ((java.lang.Boolean) get("IsMounted")).booleanValue();
    }
    public void setIsMounted(boolean newIsMounted) throws COMException {
        put("IsMounted", new java.lang.Boolean(newIsMounted));
    }
    public void setIsPersistent(boolean newIsPersistent) throws COMException {
        put("IsPersistent", new java.lang.Boolean(newIsPersistent));
    }
    public boolean getIsReplicated() throws COMException {
        return ((java.lang.Boolean) get("IsReplicated")).booleanValue();
    }
    public ICCLabelType getLabelType(java.lang.String Name, boolean Local) throws COMException {
        return new ICCLabelType((DispatchPtr) getN("LabelType", new Object[]{Name, new java.lang.Boolean(Local)}));
    }
    public ICCLabelTypes getLabelTypes(boolean Local, boolean IncludeObsoletes) throws COMException {
        return new ICCLabelTypes((DispatchPtr) getN("LabelTypes", new Object[]{new java.lang.Boolean(Local), new java.lang.Boolean(IncludeObsoletes)}));
    }
    public ICCLock getLock() throws COMException {
        return new ICCLock((DispatchPtr) get("Lock"));
    }
    public ICCLocks getLocks(boolean IncludeObsoletes) throws COMException {
        return new ICCLocks((DispatchPtr) getN("Locks", new Object[]{new java.lang.Boolean(IncludeObsoletes)}));
    }
    public java.lang.String getMaster() throws COMException {
        return (java.lang.String) get("Master");
    }
    public int getNumberOfAdditionalGroups() throws COMException {
        return ((java.lang.Integer) get("NumberOfAdditionalGroups")).intValue();
    }
    public int getNumberOfReplicas() throws COMException {
        return ((java.lang.Integer) get("NumberOfReplicas")).intValue();
    }
    public java.lang.String getOwner() throws COMException {
        return (java.lang.String) get("Owner");
    }
    public void Protect(java.lang.String NewOwner, java.lang.String NewGroup) throws COMException {
        invokeN("Protect", new Object[]{NewOwner, NewGroup}, 2);
    }
    public void Protect(java.lang.String NewOwner, java.lang.String NewGroup, java.lang.Object GroupsToAddStringArray) throws COMException {
        invokeN("Protect", new Object[]{NewOwner, NewGroup, GroupsToAddStringArray}, 3);
    }
    public void Protect(java.lang.String NewOwner, java.lang.String NewGroup, java.lang.Object GroupsToAddStringArray, java.lang.Object GroupsToRemoveStringArray) throws COMException {
        invokeN("Protect", new Object[]{NewOwner, NewGroup, GroupsToAddStringArray, GroupsToRemoveStringArray}, 4);
    }
    public java.lang.Object getReplicasStringArray() throws COMException {
        return (java.lang.Object) get("ReplicasStringArray");
    }
    public void SetMaster(java.lang.String Replica, java.lang.String Comment) throws COMException {
        invokeN("SetMaster", new Object[]{Replica, Comment}, 2);
    }
    public java.lang.String getThisReplica() throws COMException {
        return (java.lang.String) get("ThisReplica");
    }
    public ICCTriggerType getTriggerType(java.lang.String Name) throws COMException {
        return new ICCTriggerType((DispatchPtr) getN("TriggerType", new Object[]{Name}));
    }
    public ICCTriggerTypes getTriggerTypes(boolean IncludeObsoletes) throws COMException {
        return new ICCTriggerTypes((DispatchPtr) getN("TriggerTypes", new Object[]{new java.lang.Boolean(IncludeObsoletes)}));
    }
}
