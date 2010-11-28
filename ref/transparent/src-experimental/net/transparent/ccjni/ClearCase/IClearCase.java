// created by JCOMGen
// from TypeLib at C:\Program Files\Rational\clearcase\bin\ccauto.dll
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class IClearCase extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7EC6-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, IClearCase.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public IClearCase() throws COMException {
        super();
    }
    public IClearCase(String progid) throws COMException {
        super(progid);
    }
    public IClearCase(IUnknown other) throws COMException {
        super(other);
    }
    public IClearCase(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public ICCActivity getActivity(java.lang.String Selector) throws COMException {
        return new ICCActivity((DispatchPtr) getN("Activity", new Object[]{Selector}));
    }
    public ICCAttributes getAttributesEmpty() throws COMException {
        return new ICCAttributes((DispatchPtr) get("AttributesEmpty"));
    }
    public ICCAttributeTypes getAttributeTypesEmpty() throws COMException {
        return new ICCAttributeTypes((DispatchPtr) get("AttributeTypesEmpty"));
    }
    public ICCBranches getBranchesEmpty() throws COMException {
        return new ICCBranches((DispatchPtr) get("BranchesEmpty"));
    }
    public ICCBranchTypes getBranchTypesEmpty() throws COMException {
        return new ICCBranchTypes((DispatchPtr) get("BranchTypesEmpty"));
    }
    public ICCCheckedOutFile getCheckedOutFile(java.lang.String Path) throws COMException {
        return new ICCCheckedOutFile((DispatchPtr) getN("CheckedOutFile", new Object[]{Path}));
    }
    public ICCCheckedOutFiles getCheckedOutFilesEmpty() throws COMException {
        return new ICCCheckedOutFiles((DispatchPtr) get("CheckedOutFilesEmpty"));
    }
    public void CheckLicense() throws COMException {
        invoke("CheckLicense");
    }
    public ICCCheckedOutFileQuery CreateCheckedOutFileQuery() throws COMException {
        return new ICCCheckedOutFileQuery((DispatchPtr) invoke("CreateCheckedOutFileQuery"));
    }
    public ICCCheckedOutFile CreateElement(java.lang.String Path, java.lang.String Comment, boolean SetMaster) throws COMException {
        return new ICCCheckedOutFile((DispatchPtr) invokeN("CreateElement", new Object[]{Path, Comment, new java.lang.Boolean(SetMaster)}, 3));
    }
    public ICCElement getElement(java.lang.String Path) throws COMException {
        return new ICCElement((DispatchPtr) getN("Element", new Object[]{Path}));
    }
    public ICCElements getElementsEmpty() throws COMException {
        return new ICCElements((DispatchPtr) get("ElementsEmpty"));
    }
    public ICCHistoryRecords getHistoryRecordsEmpty() throws COMException {
        return new ICCHistoryRecords((DispatchPtr) get("HistoryRecordsEmpty"));
    }
    public ICCHyperlink getHyperlink(java.lang.String Selector) throws COMException {
        return new ICCHyperlink((DispatchPtr) getN("Hyperlink", new Object[]{Selector}));
    }
    public ICCHyperlinks getHyperlinksEmpty() throws COMException {
        return new ICCHyperlinks((DispatchPtr) get("HyperlinksEmpty"));
    }
    public ICCHyperlinkTypes getHyperlinkTypesEmpty() throws COMException {
        return new ICCHyperlinkTypes((DispatchPtr) get("HyperlinkTypesEmpty"));
    }
    public void setIsWebGUI(boolean newIsWebGUI) throws COMException {
        put("IsWebGUI", new java.lang.Boolean(newIsWebGUI));
    }
    public ICCLabels getLabelsEmpty() throws COMException {
        return new ICCLabels((DispatchPtr) get("LabelsEmpty"));
    }
    public ICCLabelTypes getLabelTypesEmpty() throws COMException {
        return new ICCLabelTypes((DispatchPtr) get("LabelTypesEmpty"));
    }
    public ICCLocks getLocksEmpty() throws COMException {
        return new ICCLocks((DispatchPtr) get("LocksEmpty"));
    }
    public void SetAbortPrompts() throws COMException {
        invoke("SetAbortPrompts");
    }
    public ICCTriggers getTriggersEmpty() throws COMException {
        return new ICCTriggers((DispatchPtr) get("TriggersEmpty"));
    }
    public ICCTriggerTypes getTriggerTypesEmpty() throws COMException {
        return new ICCTriggerTypes((DispatchPtr) get("TriggerTypesEmpty"));
    }
    public ICCVersion getVersion(java.lang.Object Path) throws COMException {
        return new ICCVersion((DispatchPtr) getN("Version", new Object[]{Path}));
    }
    public ICCVersions getVersionsEmpty() throws COMException {
        return new ICCVersions((DispatchPtr) get("VersionsEmpty"));
    }
    public ICCView getView(java.lang.String Identifier) throws COMException {
        return new ICCView((DispatchPtr) getN("View", new Object[]{Identifier}));
    }
    public ICCViews getViews(boolean FailIfErrors, java.lang.String Region) throws COMException {
        return new ICCViews((DispatchPtr) getN("Views", new Object[]{new java.lang.Boolean(FailIfErrors), Region}));
    }
    public ICCViews getViewsEmpty() throws COMException {
        return new ICCViews((DispatchPtr) get("ViewsEmpty"));
    }
    public ICCVOB getVOB(java.lang.String Identifier) throws COMException {
        return new ICCVOB((DispatchPtr) getN("VOB", new Object[]{Identifier}));
    }
    public ICCVOBs getVOBs(boolean FailIfErrors, java.lang.String Region) throws COMException {
        return new ICCVOBs((DispatchPtr) getN("VOBs", new Object[]{new java.lang.Boolean(FailIfErrors), Region}));
    }
    public ICCVOBs getVOBsEmpty() throws COMException {
        return new ICCVOBs((DispatchPtr) get("VOBsEmpty"));
    }
    public ICCActivities getActivitiesEmpty() throws COMException {
        return new ICCActivities((DispatchPtr) get("ActivitiesEmpty"));
    }
    public ICCActivity getActivityOfVersion(ICCVersion pVersion) throws COMException {
        return new ICCActivity((DispatchPtr) getN("ActivityOfVersion", new Object[]{pVersion}));
    }
    public ICCBaseline getBaseline(java.lang.String Selector) throws COMException {
        return new ICCBaseline((DispatchPtr) getN("Baseline", new Object[]{Selector}));
    }
    public ICCBaselines getBaselinesEmpty() throws COMException {
        return new ICCBaselines((DispatchPtr) get("BaselinesEmpty"));
    }
    public ICCComponent getComponent(java.lang.String Selector) throws COMException {
        return new ICCComponent((DispatchPtr) getN("Component", new Object[]{Selector}));
    }
    public ICCComponents getComponentsEmpty() throws COMException {
        return new ICCComponents((DispatchPtr) get("ComponentsEmpty"));
    }
    public ICCBaselineComparison CreateBaselineComparison() throws COMException {
        return new ICCBaselineComparison((DispatchPtr) invoke("CreateBaselineComparison"));
    }
    public ICCFolder getFolder(java.lang.String Selector) throws COMException {
        return new ICCFolder((DispatchPtr) getN("Folder", new Object[]{Selector}));
    }
    public ICCFolders getFoldersEmpty() throws COMException {
        return new ICCFolders((DispatchPtr) get("FoldersEmpty"));
    }
    public boolean getIsClearCaseLT() throws COMException {
        return ((java.lang.Boolean) get("IsClearCaseLT")).booleanValue();
    }
    public boolean getIsClearCaseLTClient() throws COMException {
        return ((java.lang.Boolean) get("IsClearCaseLTClient")).booleanValue();
    }
    public boolean getIsClearCaseLTServer() throws COMException {
        return ((java.lang.Boolean) get("IsClearCaseLTServer")).booleanValue();
    }
    public ICCProject getProject(java.lang.String Selector) throws COMException {
        return new ICCProject((DispatchPtr) getN("Project", new Object[]{Selector}));
    }
    public ICCProjects getProjectsEmpty() throws COMException {
        return new ICCProjects((DispatchPtr) get("ProjectsEmpty"));
    }
    public ICCProjectVOB getProjectVOB(java.lang.String Identifier) throws COMException {
        return new ICCProjectVOB((DispatchPtr) getN("ProjectVOB", new Object[]{Identifier}));
    }
    public ICCProjectVOBs getProjectVOBsEmpty() throws COMException {
        return new ICCProjectVOBs((DispatchPtr) get("ProjectVOBsEmpty"));
    }
    public ICCStream getStream(java.lang.String Selector) throws COMException {
        return new ICCStream((DispatchPtr) getN("Stream", new Object[]{Selector}));
    }
    public ICCStreams getStreamsEmpty() throws COMException {
        return new ICCStreams((DispatchPtr) get("StreamsEmpty"));
    }
    public java.lang.String getUniversalSelector(ICCVOBObject pVOBObject) throws COMException {
        return (java.lang.String) getN("UniversalSelector", new Object[]{pVOBObject});
    }
}
