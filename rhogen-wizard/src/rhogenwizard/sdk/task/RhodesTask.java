package rhogenwizard.sdk.task;

import rhogenwizard.OSValidator;

public abstract class RhodesTask extends RakeTask 
{
	protected String m_rhogenExe = "rhodes";
		
	public RhodesTask()
	{		
		if (OSValidator.OSType.WINDOWS == OSValidator.detect()) 
		{
			 m_rhogenExe  = m_rhogenExe + ".bat";
		} 
	}
}
