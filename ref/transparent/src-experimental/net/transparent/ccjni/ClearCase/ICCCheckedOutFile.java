// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCCheckedOutFile extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7ED9-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCCheckedOutFile.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCCheckedOutFile() throws COMException {
        super();
    }
    public ICCCheckedOutFile(String progid) throws COMException {
        super(progid);
    }
    public ICCCheckedOutFile(IUnknown other) throws COMException {
        super(other);
    }
    public ICCCheckedOutFile(GUID ClsID) throws COMException {
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
    public ICCBranch getBranch() throws COMException {
        return new ICCBranch((DispatchPtr) get("Branch"));
    }
    public ICCCheckedOutFile CheckOut(int ReservedState, java.lang.String Comment, boolean UseHijacked, int Version, boolean MustBeLatest, boolean PreserveTime) throws COMException {
        return new ICCCheckedOutFile((DispatchPtr) invokeN("CheckOut", new Object[]{new java.lang.Integer(ReservedState), Comment, new java.lang.Boolean(UseHijacked), new java.lang.Integer(Version), new java.lang.Boolean(MustBeLatest), new java.lang.Boolean(PreserveTime)}, 6));
    }
    public ICCElement getElement() throws COMException {
        return new ICCElement((DispatchPtr) get("Element"));
    }
    public java.lang.String getIdentifier() throws COMException {
        return (java.lang.String) get("Identifier");
    }
    public boolean getIsCheckedOut() throws COMException {
        return ((java.lang.Boolean) get("IsCheckedOut")).booleanValue();
    }
    public boolean getIsDifferent() throws COMException {
        return ((java.lang.Boolean) get("IsDifferent")).booleanValue();
    }
    public boolean getIsHijacked() throws COMException {
        return ((java.lang.Boolean) get("IsHijacked")).booleanValue();
    }
    public boolean getIsLatest() throws COMException {
        return ((java.lang.Boolean) get("IsLatest")).booleanValue();
    }
    public ICCLabel getLabel(java.lang.String LabelType) throws COMException {
        return new ICCLabel((DispatchPtr) getN("Label", new Object[]{LabelType}));
    }
    public ICCLabels getLabels() throws COMException {
        return new ICCLabels((DispatchPtr) get("Labels"));
    }
    public ICCVersion getParent() throws COMException {
        return new ICCVersion((DispatchPtr) get("Parent"));
    }
    public ICCVersion getPredecessor() throws COMException {
        return new ICCVersion((DispatchPtr) get("Predecessor"));
    }
    public void RemoveVersion(java.lang.String Comment, boolean DataOnly, boolean EvenIfBranches, boolean EvenIfLabels, boolean EvenIfAttributes, boolean EvenIfHyperlinks) throws COMException {
        invokeN("RemoveVersion", new Object[]{Comment, new java.lang.Boolean(DataOnly), new java.lang.Boolean(EvenIfBranches), new java.lang.Boolean(EvenIfLabels), new java.lang.Boolean(EvenIfAttributes), new java.lang.Boolean(EvenIfHyperlinks)}, 6);
    }
    public ICCBranches getSubBranches() throws COMException {
        return new ICCBranches((DispatchPtr) get("SubBranches"));
    }
    public int getVersionNumber() throws COMException {
        return ((java.lang.Integer) get("VersionNumber")).intValue();
    }
    public ICCView getByView() throws COMException {
        return new ICCView((DispatchPtr) get("ByView"));
    }
    public ICCVersion CheckIn(java.lang.String Comment, boolean EvenIfIdentical, java.lang.String FromPath, int KeepState) throws COMException {
        return new ICCVersion((DispatchPtr) invokeN("CheckIn", new Object[]{Comment, new java.lang.Boolean(EvenIfIdentical), FromPath, new java.lang.Integer(KeepState)}, 4));
    }
    public boolean getIsReserved() throws COMException {
        return ((java.lang.Boolean) get("IsReserved")).booleanValue();
    }
    public void Reserve(java.lang.String Comment) throws COMException {
        invokeN("Reserve", new Object[]{Comment}, 1);
    }
    public ICCVersion UnCheckOut(int KeepState) throws COMException {
        return new ICCVersion((DispatchPtr) invokeN("UnCheckOut", new Object[]{new java.lang.Integer(KeepState)}, 1));
    }
    public void UnReserve(java.lang.String Comment) throws COMException {
        invokeN("UnReserve", new Object[]{Comment}, 1);
    }
}
