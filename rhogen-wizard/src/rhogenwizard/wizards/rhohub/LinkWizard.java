package rhogenwizard.wizards.rhohub;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;

import rhogenwizard.Activator;
import rhogenwizard.ShowPerspectiveJob;
import rhogenwizard.constants.CommonConstants;
import rhogenwizard.constants.UiConstants;
import rhogenwizard.project.extension.ProjectNotFoundException;
import rhogenwizard.wizards.BaseAppWizard;

public class LinkWizard extends BaseAppWizard
{
    private BuildCredentialPage m_pageCred    = null;
    private BuildSettingPage    m_pageSetting = null;

    private IProject m_selectedProject = null;
    
    public LinkWizard(IProject project)
    {
        super();
        setNeedsProgressMonitor(true);
        
        m_selectedProject = project;
    }

    /**
     * Adding the page to the wizard.
     */
    public void addPages()
    {       
        m_pageCred    = new BuildCredentialPage(m_selectedProject);
        m_pageSetting = new BuildSettingPage(m_selectedProject);
        
        addPage(m_pageCred);
        addPage(m_pageSetting);
    }

    public boolean performFinish()
    {
        IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException
            {
                try
                {
                    doFinish(monitor);
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
            getContainer().run(true, true, op);
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

    /**
     * @throws ProjectNotFoundExtension
     *             The worker method. It will find the container, create the
     *             file if missing or just replace its contents, and open the
     *             editor on the newly created file.
     */
    private void doFinish(IProgressMonitor monitor) throws CoreException, ProjectNotFoundException
    {
        try
        {
            monitor.beginTask("Start building on rhohub server", 1);
            
            if (CommonConstants.checkRhohubVersion)
            {
//                monitor.setTaskName("Check Rhodes version...");
//
//                try
//                {
//                    if (!RunExeHelper.checkRhodesVersion(CommonConstants.rhodesVersion))
//                    {
//                        throw new IOException();
//                    }
//                }
//                catch (IOException e)
//                {
//                    String msg = "Installed Rhohub have old version, need rhodes version equal or greater " 
//                        + CommonConstants.rhodesVersion + " Please reinstall it (See 'http://docs.rhomobile.com/rhodes/install' for more information)";
//                    DialogUtils.error("Error", msg);
//                    return;
//                }
            }
            
            monitor.worked(1);
            
            IPreferenceStore store = Activator.getDefault().getPreferenceStore();

            if (store != null)
            {

            }
            
            ShowPerspectiveJob job = new ShowPerspectiveJob("show rhodes perspective",
                UiConstants.rhodesPerspectiveId);
            job.schedule();

            monitor.worked(1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}