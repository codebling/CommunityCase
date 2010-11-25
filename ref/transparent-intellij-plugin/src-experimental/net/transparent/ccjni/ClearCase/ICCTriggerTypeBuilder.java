// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCTriggerTypeBuilder extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7EF2-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCTriggerTypeBuilder.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCTriggerTypeBuilder() throws COMException {
        super();
    }
    public ICCTriggerTypeBuilder(String progid) throws COMException {
        super(progid);
    }
    public ICCTriggerTypeBuilder(IUnknown other) throws COMException {
        super(other);
    }
    public ICCTriggerTypeBuilder(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public java.lang.String getName() throws COMException {
        return (java.lang.String) get("Name");
    }
    public void setName(java.lang.String newName) throws COMException {
        put("Name", newName);
    }
    public java.lang.Object getActionsArray() throws COMException {
        return (java.lang.Object) get("ActionsArray");
    }
    public void AddExecAction(java.lang.String Action) throws COMException {
        invokeN("AddExecAction", new Object[]{Action}, 1);
    }
    public void AddExecUNIXAction(java.lang.String Action) throws COMException {
        invokeN("AddExecUNIXAction", new Object[]{Action}, 1);
    }
    public void AddExecWinAction(java.lang.String Action) throws COMException {
        invokeN("AddExecWinAction", new Object[]{Action}, 1);
    }
    public void AddMkattrAction(ICCAttributeType pAttributeType, java.lang.Object Value) throws COMException {
        invokeN("AddMkattrAction", new Object[]{pAttributeType, Value}, 2);
    }
    public void AddMkhlinkFromAction(ICCHyperlinkType pHyperlinkType, java.lang.String FromPath) throws COMException {
        invokeN("AddMkhlinkFromAction", new Object[]{pHyperlinkType, FromPath}, 2);
    }
    public void AddMkhlinkToAction(ICCHyperlinkType pHyperlinkType, java.lang.String ToPath) throws COMException {
        invokeN("AddMkhlinkToAction", new Object[]{pHyperlinkType, ToPath}, 2);
    }
    public void AddMklabelAction(ICCLabelType pLabelType) throws COMException {
        invokeN("AddMklabelAction", new Object[]{pLabelType}, 1);
    }
    public ICCTriggerType Create(java.lang.String Comment) throws COMException {
        return new ICCTriggerType((DispatchPtr) invokeN("Create", new Object[]{Comment}, 1));
    }
    public boolean getDebugPrinting() throws COMException {
        return ((java.lang.Boolean) get("DebugPrinting")).booleanValue();
    }
    public void setDebugPrinting(boolean newDebugPrinting) throws COMException {
        put("DebugPrinting", new java.lang.Boolean(newDebugPrinting));
    }
    public java.lang.Object getExemptUsersStringArray() throws COMException {
        return (java.lang.Object) get("ExemptUsersStringArray");
    }
    public void setExemptUsersStringArray(java.lang.Object newExemptUsersStringArray) throws COMException {
        put("ExemptUsersStringArray", newExemptUsersStringArray);
    }
    public void FireOn(int OperationKind) throws COMException {
        invokeN("FireOn", new Object[]{new java.lang.Integer(OperationKind)}, 1);
    }
    public int getFiring() throws COMException {
        return ((java.lang.Integer) get("Firing")).intValue();
    }
    public void setFiring(int newFiring) throws COMException {
        put("Firing", new java.lang.Integer(newFiring));
    }
    public void IncludeOn(java.lang.Object InclusionType) throws COMException {
        invokeN("IncludeOn", new Object[]{InclusionType}, 1);
    }
    public java.lang.Object getInclusionsArray() throws COMException {
        return (java.lang.Object) get("InclusionsArray");
    }
    public int getKindOfTrigger() throws COMException {
        return ((java.lang.Integer) get("KindOfTrigger")).intValue();
    }
    public void setKindOfTrigger(int newKindOfTrigger) throws COMException {
        put("KindOfTrigger", new java.lang.Integer(newKindOfTrigger));
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
    public void RemoveAction(int index) throws COMException {
        invokeN("RemoveAction", new Object[]{new java.lang.Integer(index)}, 1);
    }
    public void RemoveInclusion(java.lang.Object InclusionType) throws COMException {
        invokeN("RemoveInclusion", new Object[]{InclusionType}, 1);
    }
    public void RemoveOperationKind(int OperationKind) throws COMException {
        invokeN("RemoveOperationKind", new Object[]{new java.lang.Integer(OperationKind)}, 1);
    }
    public void RemoveRestriction(java.lang.Object RestrictionType) throws COMException {
        invokeN("RemoveRestriction", new Object[]{RestrictionType}, 1);
    }
    public ICCTriggerType Replace(java.lang.String Comment) throws COMException {
        return new ICCTriggerType((DispatchPtr) invokeN("Replace", new Object[]{Comment}, 1));
    }
    public void RestrictBy(java.lang.Object RestrictionType) throws COMException {
        invokeN("RestrictBy", new Object[]{RestrictionType}, 1);
    }
    public java.lang.Object getRestrictionsArray() throws COMException {
        return (java.lang.Object) get("RestrictionsArray");
    }
    public ICCVOB getVOB() throws COMException {
        return new ICCVOB((DispatchPtr) get("VOB"));
    }
}
