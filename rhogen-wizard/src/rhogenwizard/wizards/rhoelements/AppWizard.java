package rhogenwizard.wizards.rhoelements;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.runtime.CoreException;
import java.io.*;

import org.eclipse.ui.*;
import rhogenwizard.BuildInfoHolder;
import rhogenwizard.DialogUtils;
import rhogenwizard.RunExeHelper;
import rhogenwizard.ShowMessageJob;
import rhogenwizard.ShowPerspectiveJob;
import rhogenwizard.constants.CommonConstants;
import rhogenwizard.constants.MsgConstants;
import rhogenwizard.constants.UiConstants;
import rhogenwizard.project.IRhomobileProject;
import rhogenwizard.project.ProjectFactory;
import rhogenwizard.project.RhodesProject;
import rhogenwizard.project.RhoelementsProject;
import rhogenwizard.project.extension.AlredyCreatedException;
import rhogenwizard.project.extension.ProjectNotFoundException;
import rhogenwizard.sdk.facade.RhoTaskHolder;
import rhogenwizard.sdk.helper.TaskResultConverter;
import rhogenwizard.sdk.task.GenerateRhodesAppTask;
import rhogenwizard.sdk.task.GenerateRhoelementsAppTask;
import rhogenwizard.wizards.rhodes.AppWizardPage;

public class AppWizard extends Wizard implements INewWizard 
{
	private AppWizardPage  m_pageApp = null;
	private ISelection     selection = null;
	
	/**
	 * Constructor for SampleNewWizard.
	 */
	public AppWizard() 
	{
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() 
	{
		m_pageApp = new AppWizardPage(selection);
		m_pageApp.setTitle(MsgConstants.titleRhoelementsPage);
		addPage(m_pageApp);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() 
	{
		final BuildInfoHolder holder = m_pageApp.getBuildInformation();
		
		IRunnableWithProgress op = new IRunnableWithProgress() 
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException 
			{
				try
				{
					doFinish(holder, monitor);
				}
				catch (CoreException e) 
				{
					throw new InvocationTargetException(e);
				} 
				catch (ProjectNotFoundException e) 
				{
					e.printStackTrace();
				} 
				finally 
				{
					monitor.done();
				}
			}
		};
		
		try 
		{
			getContainer().run(true, false, op);
		} 
		catch (InterruptedException e) 
		{
			return false;
		} 
		catch (InvocationTargetException e) 
		{
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		
		return true;
	}
	
	private void createProjectFiles(BuildInfoHolder infoHolder, IProgressMonitor monitor) throws Exception
	{
		monitor.setTaskName("Generate application...");
		
		Map<String, Object> params = new HashMap<String, Object>();

		params.put(GenerateRhoelementsAppTask.appName, infoHolder.appName);
		params.put(GenerateRhoelementsAppTask.workDir, infoHolder.getProjectLocationPath().toOSString());
		
		Map results = RhoTaskHolder.getInstance().runTask(GenerateRhoelementsAppTask.class, params);
		
		if (TaskResultConverter.getResultIntCode(results) != 0)
		{
			throw new IOException(MsgConstants.errInstallRhodes);
		}	
	}
	
	/**
	 * @throws ProjectNotFoundExtension 
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 * @throws  
	 */
	private void doFinish(BuildInfoHolder infoHolder, IProgressMonitor monitor) throws CoreException, ProjectNotFoundException 
	{
		IRhomobileProject newProject = null;
		
		try 
		{
			monitor.beginTask("Creating " + infoHolder.appName, 2);
			monitor.worked(1);
			monitor.setTaskName("Create project...");
			
			newProject = ProjectFactory.getInstance().createProject(RhoelementsProject.class, infoHolder);

			if (CommonConstants.checkRhodesVersion)
			{
				monitor.setTaskName("Check Rhoelements version...");
				
				try
				{
					if (!RunExeHelper.checkRhodesVersion(CommonConstants.rhodesVersion))
					{
						throw new IOException();
					}
				}
				catch (IOException e)
				{
					newProject.deleteProjectFiles();
//					ShowMessageJob msgJob = new ShowMessageJob("", "Error", "Installed Rhodes have old version, need rhodes version equal or greater " 
//							+ CommonConstants.rhodesVersion + " Please reinstall it (See 'http://docs.rhomobile.com/rhodes/install' for more information)");
//					msgJob.run(monitor);
					return;					
				}
			}
			
			if (!infoHolder.existCreate) 
			{
				createProjectFiles(infoHolder, monitor);
			}

			newProject.refreshProject();

			ShowPerspectiveJob job = new ShowPerspectiveJob("show RhoMobile perspective", UiConstants.rhodesPerspectiveId);
			job.schedule();
			
			monitor.worked(1);
		} 
		catch (IOException e)
		{
			newProject.deleteProjectFiles();
//			ShowMessageJob msgJob = new ShowMessageJob("", "Error", "Cannot find Rhoelements gem, install rhostudio need rhodes version equal or greater " 
//					+ CommonConstants.rhodesVersion + " (See 'http://docs.rhomobile.com/rhodes/install' for more information)");
//			msgJob.run(monitor);
		}
		catch (AlredyCreatedException e)
		{
		    DialogUtils.warn("Warning", e.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
		this.selection = selection;
	}
}