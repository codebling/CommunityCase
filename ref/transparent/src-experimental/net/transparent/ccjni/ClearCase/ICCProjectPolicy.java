// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCProjectPolicy extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7F28-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCProjectPolicy.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCProjectPolicy() throws COMException {
        super();
    }
    public ICCProjectPolicy(String progid) throws COMException {
        super(progid);
    }
    public ICCProjectPolicy(IUnknown other) throws COMException {
        super(other);
    }
    public ICCProjectPolicy(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public boolean getDeliverRequireCheckin() throws COMException {
        return ((java.lang.Boolean) get("DeliverRequireCheckin")).booleanValue();
    }
    public void setDeliverRequireCheckin(boolean newDeliverRequireCheckin) throws COMException {
        put("DeliverRequireCheckin", new java.lang.Boolean(newDeliverRequireCheckin));
    }
    public boolean getDeliverRequireRebase() throws COMException {
        return ((java.lang.Boolean) get("DeliverRequireRebase")).booleanValue();
    }
    public void setDeliverRequireRebase(boolean newDeliverRequireRebase) throws COMException {
        put("DeliverRequireRebase", new java.lang.Boolean(newDeliverRequireRebase));
    }
    public boolean getUNIXDevelopmentSnapshot() throws COMException {
        return ((java.lang.Boolean) get("UNIXDevelopmentSnapshot")).booleanValue();
    }
    public void setUNIXDevelopmentSnapshot(boolean newUNIXDevelopmentSnapshot) throws COMException {
        put("UNIXDevelopmentSnapshot", new java.lang.Boolean(newUNIXDevelopmentSnapshot));
    }
    public boolean getUNIXIntegrationSnapshot() throws COMException {
        return ((java.lang.Boolean) get("UNIXIntegrationSnapshot")).booleanValue();
    }
    public void setUNIXIntegrationSnapshot(boolean newUNIXIntegrationSnapshot) throws COMException {
        put("UNIXIntegrationSnapshot", new java.lang.Boolean(newUNIXIntegrationSnapshot));
    }
    public boolean getWinDevelopmentSnapshot() throws COMException {
        return ((java.lang.Boolean) get("WinDevelopmentSnapshot")).booleanValue();
    }
    public void setWinDevelopmentSnapshot(boolean newWinDevelopmentSnapshot) throws COMException {
        put("WinDevelopmentSnapshot", new java.lang.Boolean(newWinDevelopmentSnapshot));
    }
    public boolean getWinIntegrationSnapshot() throws COMException {
        return ((java.lang.Boolean) get("WinIntegrationSnapshot")).booleanValue();
    }
    public void setWinIntegrationSnapshot(boolean newWinIntegrationSnapshot) throws COMException {
        put("WinIntegrationSnapshot", new java.lang.Boolean(newWinIntegrationSnapshot));
    }
}
