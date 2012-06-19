package rhogenwizard.rhohub;

import org.osgi.service.prefs.BackingStoreException;

public interface IRhoHubSettingSaver
{
    //
    public void setLinking() throws BackingStoreException;
    //
    public void setToken(String value) throws BackingStoreException;
    //
    public void setServerUrl(String value) throws BackingStoreException;
    //
    public void setSelectedPlatform(String value) throws BackingStoreException;
    //
    public void setRhodesBranch(String value) throws BackingStoreException;
    //
    public void setAppBranch(String value);
}
