// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCTriggerType extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7EF3-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCTriggerType.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCTriggerType() throws COMException {
        super();
    }
    public ICCTriggerType(String progid) throws COMException {
        super(progid);
    }
    public ICCTriggerType(IUnknown other) throws COMException {
        super(other);
    }
    public ICCTriggerType(GUID ClsID) throws COMException {
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
    public java.lang.Object getActionsArray() throws COMException {
        return (java.lang.Object) get("ActionsArray");
    }
    public void Apply(ICCElement pElement, java.lang.String Comment, boolean Force, boolean Recurse) throws COMException {
        invokeN("Apply", new Object[]{pElement, Comment, new java.lang.Boolean(Force), new java.lang.Boolean(Recurse)}, 4);
    }
    public void Apply(ICCElement pElement, java.lang.String Comment, boolean Force, boolean Recurse, java.lang.Object DirectoryListsSubset) throws COMException {
        invokeN("Apply", new Object[]{pElement, Comment, new java.lang.Boolean(Force), new java.lang.Boolean(Recurse), DirectoryListsSubset}, 5);
    }
    public ICCTriggerTypeBuilder CreateBuilderFromType() throws COMException {
        return new ICCTriggerTypeBuilder((DispatchPtr) invoke("CreateBuilderFromType"));
    }
    public void CreateLock(java.lang.String Comment, boolean Obsolete) throws COMException {
        invokeN("CreateLock", new Object[]{Comment, new java.lang.Boolean(Obsolete)}, 2);
    }
    public void CreateLock(java.lang.String Comment, boolean Obsolete, java.lang.Object ExemptUsersStringArray) throws COMException {
        invokeN("CreateLock", new Object[]{Comment, new java.lang.Boolean(Obsolete), ExemptUsersStringArray}, 3);
    }
    public java.lang.Object getExemptUsersStringArray() throws COMException {
        return (java.lang.Object) get("ExemptUsersStringArray");
    }
    public int getFiring() throws COMException {
        return ((java.lang.Integer) get("Firing")).intValue();
    }
    public java.lang.String getGroup() throws COMException {
        return (java.lang.String) get("Group");
    }
    public java.lang.Object getInclusionsArray() throws COMException {
        return (java.lang.Object) get("InclusionsArray");
    }
    public int getKindOfTrigger() throws COMException {
        return ((java.lang.Integer) get("KindOfTrigger")).intValue();
    }
    public ICCLock getLock() throws COMException {
        return new ICCLock((DispatchPtr) get("Lock"));
    }
    public int getNumberOfActions() throws COMException {
        return ((java.lang.Integer) get("NumberOfActions")).intValue();
    }
    public int getNumberOfExemptUsers() throws COMException {
        return ((java.lang.Integer) get("NumberOfExemptUsers")).intValue();
    }
    public int getNumberOfInclusions() throws COMException {
        return ((java.lang.Integer) get("NumberOfInclusions")).intValue();
    }
    public int getNumberOfOperationKinds() throws COMException {
        return ((java.lang.Integer) get("NumberOfOperationKinds")).intValue();
    }
    public int getNumberOfRestrictions() throws COMException {
        return ((java.lang.Integer) get("NumberOfRestrictions")).intValue();
    }
    public java.lang.Object getOperationKindsArray() throws COMException {
        return (java.lang.Object) get("OperationKindsArray");
    }
    public java.lang.String getOwner() throws COMException {
        return (java.lang.String) get("Owner");
    }
    public boolean getDebugPrinting() throws COMException {
        return ((java.lang.Boolean) get("DebugPrinting")).booleanValue();
    }
    public void RemoveType(boolean RemoveAllInstances, boolean IgnorePreopTriggers, java.lang.String Comment) throws COMException {
        invokeN("RemoveType", new Object[]{new java.lang.Boolean(RemoveAllInstances), new java.lang.Boolean(IgnorePreopTriggers), Comment}, 3);
    }
    public java.lang.Object getRestrictionsArray() throws COMException {
        return (java.lang.Object) get("RestrictionsArray");
    }
    public void SetExemptUsersStringArray(java.lang.Object ExemptUsersStringArray, java.lang.String Comment) throws COMException {
        invokeN("SetExemptUsersStringArray", new Object[]{ExemptUsersStringArray, Comment}, 2);
    }
    public void SetGroup(java.lang.String NewGroup, java.lang.String Comment) throws COMException {
        invokeN("SetGroup", new Object[]{NewGroup, Comment}, 2);
    }
    public void SetName(java.lang.String NewName, java.lang.String Comment) throws COMException {
        invokeN("SetName", new Object[]{NewName, Comment}, 2);
    }
    public void SetOwner(java.lang.String NewOwner, java.lang.String Comment) throws COMException {
        invokeN("SetOwner", new Object[]{NewOwner, Comment}, 2);
    }
    public void SetDebugPrinting(boolean NewDebugPrinting, java.lang.String Comment) throws COMException {
        invokeN("SetDebugPrinting", new Object[]{new java.lang.Boolean(NewDebugPrinting), Comment}, 2);
    }
    public ICCVOB getVOB() throws COMException {
        return new ICCVOB((DispatchPtr) get("VOB"));
    }
}
