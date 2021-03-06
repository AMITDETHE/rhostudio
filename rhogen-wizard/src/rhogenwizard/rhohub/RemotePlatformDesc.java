package rhogenwizard.rhohub;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RemotePlatformDesc extends BaseRemoteDesc
{
    public static class RemotePlatformDescFactory implements JsonAbstractFactory<RemotePlatformDesc>
    {
        @Override
        public RemotePlatformDesc getInstance(JSONObject object)
        {
            return new RemotePlatformDesc(object);
        }
    }
    
    private String     m_intName    = null;
    private String     m_publicName = null;
    
    public RemotePlatformDesc(JSONObject object)
    {
        super(object);
                
        try
        {
            JSONArray plInfo = m_baseObject.names();
            
            m_publicName = (String)plInfo.get(0);
            m_intName    = m_baseObject.getString(m_publicName);
        }
        catch (JSONException e)
        {
            m_intName    = null;
            m_publicName = null;
        }
    }
    
    public String getInternalName()
    {
        return m_intName;
    }
    
    public String getPublicName()
    {
        return m_publicName;
    }
    
    public String getPlatformName()
    {
        return m_publicName.split("\\-")[0];
    }
    
    public String getPlatformVersion()
    {
        String[] components = m_publicName.split("\\-");
        
        if (components.length > 1) 
            return m_publicName.split("\\-")[1];
        else
            return m_publicName.split("\\-")[0];
    }
}
