package rhogenwizard.wizards.rhohub;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import rhogenwizard.DialogUtils;
import rhogenwizard.ShowPerspectiveJob;
import rhogenwizard.constants.UiConstants;
import rhogenwizard.project.extension.ProjectNotFoundException;
import rhogenwizard.rhohub.GitCredentialsProvider;
import rhogenwizard.rhohub.IRemoteProjectDesc;
import rhogenwizard.rhohub.IRhoHubSetting;
import rhogenwizard.rhohub.RhoHub;
import rhogenwizard.rhohub.RhoHubBundleSetting;
import rhogenwizard.wizards.BaseAppWizard;

public class LinkWizard extends BaseAppWizard
{
    private LinkProjectPage m_pageLink    = null;
    
    private IProject        m_selectedProject = null;
    private IRhoHubSetting  m_setting = null;
    
    public LinkWizard(IProject project)
    {
        super();
        setNeedsProgressMonitor(true);
        
        m_selectedProject = project;
        
        m_setting = RhoHubBundleSetting.createGetter(m_selectedProject);
    }

    /**
     * Adding the page to the wizard.
     */
    public void addPages()
    {       
        m_pageLink = new LinkProjectPage(m_selectedProject, m_setting);

        addPage(m_pageLink);
    }

    /**
     * This method is called when 'Finish' button is pressed in the wizard. We
     * will create an operation and run it using wizard as execution context.
     */
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

            if (m_pageLink.isNewProject())
            {
                RhoHub.getInstance(m_setting).createRemoteAppFromLocalSources(m_selectedProject, new GitCredentialsProvider());
            }
            else
            {
                if (DialogUtils.quetsion("RhoHub", "Sources from remote project on RhoHub will be replaced on local sources. Contionue?"))
                {
                    RhoHub.getInstance(m_setting).updateRemoteAppFromLocalSources(m_selectedProject, m_pageLink.getSelectedProjectUrl(), new GitCredentialsProvider());
                }
            }
            
            ShowPerspectiveJob job = new ShowPerspectiveJob("show rhodes perspective",
                UiConstants.rhodesPerspectiveId);
            job.schedule();

            monitor.done();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}