package rhogenwizard.debugger.model.actions;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.dltk.internal.debug.core.model.ScriptLineBreakpoint;

import rhogenwizard.Activator;
import rhogenwizard.debugger.backend.DebugServer;

public class BpSetterImpl implements IBpSetter
{
	private final List<IBpAction> m_actions;
	private final IBreakpoint     m_bp;
	private       DebugServer     m_debugServer = null;
	
	public BpSetterImpl(List<IBpAction> actions, IBreakpoint bp, DebugServer debugServer)
	{
		m_actions     = actions;
		m_bp          = bp;
		m_debugServer = debugServer;
	}
	
	@Override
	public void setupBreakpoint() 
	{
		ScriptLineBreakpoint lineBr = (ScriptLineBreakpoint) m_bp;
		
		try 
		{
			int lineNum    = lineBr.getLineNumber();
			String srcFile = "unknown.rb"; //ResourceNameSelector.getInstance().convertBpName(ProjectFactory.getInstance().typeFromProject(m_debugProject), lineBr);
			
			m_debugServer.debugBreakpoint(srcFile, lineNum);		
		}
		catch (CoreException e) 
		{
			Activator.logError(e);
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkConditions() 
	{
		for (IBpAction action : m_actions)
		{
			if (!action.checkAction())
				return false;
		}
		
		return true;
	}
}
