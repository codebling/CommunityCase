// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCView extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7ECD-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCView.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCView() throws COMException {
        super();
    }
    public ICCView(String progid) throws COMException {
        super(progid);
    }
    public ICCView(IUnknown other) throws COMException {
        super(other);
    }
    public ICCView(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public java.lang.String getTagName() throws COMException {
        return (java.lang.String) get("TagName");
    }
    public boolean getBuildsNonShareableDOs() throws COMException {
        return ((java.lang.Boolean) get("BuildsNonShareableDOs")).booleanValue();
    }
    public void setBuildsNonShareableDOs(boolean newBuildsNonShareableDOs) throws COMException {
        put("BuildsNonShareableDOs", new java.lang.Boolean(newBuildsNonShareableDOs));
    }
    public java.lang.String getConfigSpec() throws COMException {
        return (java.lang.String) get("ConfigSpec");
    }
    public void setConfigSpec(java.lang.String newConfigSpec) throws COMException {
        put("ConfigSpec", newConfigSpec);
    }
    public java.lang.String getDisplayableConfigSpec() throws COMException {
        return (java.lang.String) get("DisplayableConfigSpec");
    }
    public java.lang.String getHost() throws COMException {
        return (java.lang.String) get("Host");
    }
    public boolean getIsActive() throws COMException {
        return ((java.lang.Boolean) get("IsActive")).booleanValue();
    }
    public void setIsActive(boolean newIsActive) throws COMException {
        put("IsActive", new java.lang.Boolean(newIsActive));
    }
    public boolean getIsSnapShot() throws COMException {
        return ((java.lang.Boolean) get("IsSnapShot")).booleanValue();
    }
    public ICCActivity getCurrentActivity() throws COMException {
        return new ICCActivity((DispatchPtr) get("CurrentActivity"));
    }
    public boolean getIsUCMView() throws COMException {
        return ((java.lang.Boolean) get("IsUCMView")).booleanValue();
    }
    public void SetActivity(ICCActivity NewActivity, java.lang.String Comment) throws COMException {
        invokeN("SetActivity", new Object[]{NewActivity, Comment}, 2);
    }
    public ICCStream getStream() throws COMException {
        return new ICCStream((DispatchPtr) get("Stream"));
    }
}
