// created by JCOMGen
// from TypeLib at 
// check for latest version at http://www.simtel.net

package net.transparent.ccjni.ClearCase;

import com.develop.jawin.*;
import com.develop.jawin.constants.*;
import com.develop.jawin.marshal.*;
import com.develop.io.*;
import java.io.*;

public class ICCBaselineComparison extends DispatchPtr {
    static public final GUID proxyIID = new GUID("{B22C7F2E-5A5E-11D3-B1CD-00C04F8ECE2F}");


    static public final int iidToken;

    static {
        iidToken = IdentityManager.registerProxy(proxyIID, ICCBaselineComparison.class);
    }

    public int getGuidToken() {
        return iidToken;
    }

    public ICCBaselineComparison() throws COMException {
        super();
    }
    public ICCBaselineComparison(String progid) throws COMException {
        super(progid);
    }
    public ICCBaselineComparison(IUnknown other) throws COMException {
        super(other);
    }
    public ICCBaselineComparison(GUID ClsID) throws COMException {
        super(ClsID);
    }
    public ICCActivities getActivitiesInOneButNotTwo() throws COMException {
        return new ICCActivities((DispatchPtr) get("ActivitiesInOneButNotTwo"));
    }
    public ICCActivities getActivitiesInTwoButNotOne() throws COMException {
        return new ICCActivities((DispatchPtr) get("ActivitiesInTwoButNotOne"));
    }
    public ICCBaseline getBaselineOne() throws COMException {
        return new ICCBaseline((DispatchPtr) get("BaselineOne"));
    }
    public void setBaselineOne(ICCBaseline newBaselineOne) throws COMException {
        put("BaselineOne", newBaselineOne);
    }
    public ICCBaseline getBaselineTwo() throws COMException {
        return new ICCBaseline((DispatchPtr) get("BaselineTwo"));
    }
    public void setBaselineTwo(ICCBaseline newBaselineTwo) throws COMException {
        put("BaselineTwo", newBaselineTwo);
    }
    public ICCActivities getChangedActivities() throws COMException {
        return new ICCActivities((DispatchPtr) get("ChangedActivities"));
    }
    public void Compare() throws COMException {
        invoke("Compare");
    }
    public ICCStream getStreamOne() throws COMException {
        return new ICCStream((DispatchPtr) get("StreamOne"));
    }
    public void setStreamOne(ICCStream newStreamOne) throws COMException {
        put("StreamOne", newStreamOne);
    }
    public ICCStream getStreamTwo() throws COMException {
        return new ICCStream((DispatchPtr) get("StreamTwo"));
    }
    public void setStreamTwo(ICCStream newStreamTwo) throws COMException {
        put("StreamTwo", newStreamTwo);
    }
    public ICCVersions getVersionsInOneButNotTwo() throws COMException {
        return new ICCVersions((DispatchPtr) get("VersionsInOneButNotTwo"));
    }
    public ICCVersions getVersionsInTwoButNotOne() throws COMException {
        return new ICCVersions((DispatchPtr) get("VersionsInTwoButNotOne"));
    }
}
