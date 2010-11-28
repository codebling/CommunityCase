// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCHyperlinkType extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7EDF-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCHyperlinkType.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCHyperlinkType() throws COMException {
        super();
    }
    public ICCHyperlinkType(String progid) throws COMException {
        super(progid);
    }
    public ICCHyperlinkType(IUnknown other) throws COMException {
        super(other);
    }
    public ICCHyperlinkType(GUID ClsID) throws COMException {
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
    public void Apply(ICCVOBObject pFromObject, java.lang.String FromText, ICCVOBObject pToObject, java.lang.String ToText, boolean Unidirectional, java.lang.String Comment) throws COMException {
        invokeN("Apply", new Object[]{pFromObject, FromText, pToObject, ToText, new java.lang.Boolean(Unidirectional), Comment}, 6);
    }
    public void CreateLock(java.lang.String Comment, boolean Obsolete) throws COMException {
        invokeN("CreateLock", new Object[]{Comment, new java.lang.Boolean(Obsolete)}, 2);
    }
    public void CreateLock(java.lang.String Comment, boolean Obsolete, java.lang.Object ExemptUsersStringArray) throws COMException {
        invokeN("CreateLock", new Object[]{Comment, new java.lang.Boolean(Obsolete), ExemptUsersStringArray}, 3);
    }
    public java.lang.String getGroup() throws COMException {
        return (java.lang.String) get("Group");
    }
    public boolean getHasSharedMastership() throws COMException {
        return ((java.lang.Boolean) get("HasSharedMastership")).booleanValue();
    }
    public ICCLock getLock() throws COMException {
        return new ICCLock((DispatchPtr) get("Lock"));
    }
    public java.lang.String getMaster() throws COMException {
        return (java.lang.String) get("Master");
    }
    public java.lang.String getOwner() throws COMException {
        return (java.lang.String) get("Owner");
    }
    public void RemoveType(boolean RemoveAllInstances, java.lang.String Comment) throws COMException {
        invokeN("RemoveType", new Object[]{new java.lang.Boolean(RemoveAllInstances), Comment}, 2);
    }
    public int getScope() throws COMException {
        return ((java.lang.Integer) get("Scope")).intValue();
    }
    public void SetGroup(java.lang.String NewGroup, java.lang.String Comment) throws COMException {
        invokeN("SetGroup", new Object[]{NewGroup, Comment}, 2);
    }
    public void SetMaster(java.lang.String Replica, java.lang.String Comment) throws COMException {
        invokeN("SetMaster", new Object[]{Replica, Comment}, 2);
    }
    public void SetName(java.lang.String NewName, java.lang.String Comment) throws COMException {
        invokeN("SetName", new Object[]{NewName, Comment}, 2);
    }
    public void SetOwner(java.lang.String NewOwner, java.lang.String Comment) throws COMException {
        invokeN("SetOwner", new Object[]{NewOwner, Comment}, 2);
    }
    public void SetScope(boolean Global, boolean Acquire, java.lang.String Comment) throws COMException {
        invokeN("SetScope", new Object[]{new java.lang.Boolean(Global), new java.lang.Boolean(Acquire), Comment}, 3);
    }
    public void ShareMastership(java.lang.String Comment) throws COMException {
        invokeN("ShareMastership", new Object[]{Comment}, 1);
    }
    public ICCVOB getVOB() throws COMException {
        return new ICCVOB((DispatchPtr) get("VOB"));
    }
    public java.lang.Object get_AttributeTypesObjectArray() throws COMException {
        return (java.lang.Object) get("_AttributeTypesObjectArray");
    }
    public int get_NumberOfAttributeTypes() throws COMException {
        return ((java.lang.Integer) get("_NumberOfAttributeTypes")).intValue();
    }
    public void _SetAttributeTypesObjectArray(java.lang.Object AttributeTypesObjectArray, java.lang.String Comment) throws COMException {
        invokeN("_SetAttributeTypesObjectArray", new Object[]{AttributeTypesObjectArray, Comment}, 2);
    }
}
