package rhogenwizard.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import rhogenwizard.PlatformType;
import rhogenwizard.ShowMessageJob;
import rhogenwizard.builder.rhodes.SelectPlatformBuildJob;
import rhogenwizard.builder.rhodes.SelectPlatformDialog;
import rhogenwizard.project.ProjectFactory;
import rhogenwizard.project.RhodesProject;
import rhogenwizard.project.RhoelementsProject;

public class ProductionBuildAction implements IWorkbenchWindowActionDelegate 
{
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public ProductionBuildAction() 
	{
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) 
	{
		IProject project = ProjectFactory.getInstance().getSelectedProject();
		
		if (project == null)
		{
			ShowMessageJob msgJob = new ShowMessageJob("", "Error", "Before run production build select RhoMobile project");
			msgJob.schedule();
			return;
		}
		
		if (!RhodesProject.checkNature(project) || RhoelementsProject.checkNature(project))
		{
			ShowMessageJob msgJob = new ShowMessageJob("", "Error", "Production build can run only for RhoMobile project");
			msgJob.schedule();
			return;
		}
		
		Shell windowShell = window.getShell();
		
		SelectPlatformDialog selectDlg = new SelectPlatformDialog(windowShell);
		PlatformType selectPlatform = selectDlg.open();

		SelectPlatformBuildJob buildJob = new SelectPlatformBuildJob("select platform", project.getLocation().toOSString(), selectPlatform);
		buildJob.schedule();
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) 
	{
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose()
	{
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) 
	{
		this.window = window;
	}
}