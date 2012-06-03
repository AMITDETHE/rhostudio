package rhogenwizard.wizards.rhodes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import rhogenwizard.BuildInfoHolder;
import rhogenwizard.DialogUtils;
import rhogenwizard.RunExeHelper;
import rhogenwizard.ShowPerspectiveJob;
import rhogenwizard.constants.CommonConstants;
import rhogenwizard.constants.MsgConstants;
import rhogenwizard.constants.UiConstants;
import rhogenwizard.project.IRhomobileProject;
import rhogenwizard.project.ProjectFactory;
import rhogenwizard.project.RhodesProject;
import rhogenwizard.project.extension.AlredyCreatedException;
import rhogenwizard.project.extension.ProjectNotFoundException;
import rhogenwizard.sdk.task.GenerateRhodesAppTask;
import rhogenwizard.sdk.task.GenerateRhoelementsAppTask;
import rhogenwizard.sdk.task.RunTask;

public class AppWizard extends Wizard implements INewWizard
{
    private AppWizardPage m_pageApp = null;
    private ISelection    selection = null;

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
        addPage(m_pageApp);
    }

    /**
     * This method is called when 'Finish' button is pressed in the wizard. We
     * will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish()
    {
        final BuildInfoHolder holder = m_pageApp.getBuildInformation();
        
//        if (!selection.isEmpty())
//        {
//            
//        }

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

    private void createProjectFiles(BuildInfoHolder infoHolder, IProgressMonitor monitor) throws IOException
    {
        monitor.setTaskName("Generate application...");

        RunTask task;
        if (infoHolder.isRhoelementsApp)
        {
            task = new GenerateRhoelementsAppTask(infoHolder.getProjectLocationPath().toOSString(),
                infoHolder.appName);
        }
        else
        {
            task = new GenerateRhodesAppTask(infoHolder.getProjectLocationPath().toOSString(),
                infoHolder.appName);
        }

        task.run(monitor);

        if (!task.isOk())
        {
            throw new IOException(MsgConstants.errInstallRhodes);
        }
    }

    /**
     * @throws ProjectNotFoundExtension
     *             The worker method. It will find the container, create the
     *             file if missing or just replace its contents, and open the
     *             editor on the newly created file.
     * @throws
     */
    private void doFinish(BuildInfoHolder infoHolder, IProgressMonitor monitor) throws CoreException, ProjectNotFoundException
    {
        try
        {
            monitor.beginTask("Creating " + infoHolder.appName, 2);
            monitor.worked(1);

            if (CommonConstants.checkRhodesVersion)
            {
                monitor.setTaskName("Check Rhodes version...");

                try
                {
                    if (!RunExeHelper.checkRhodesVersion(CommonConstants.rhodesVersion))
                    {
                        throw new IOException();
                    }
                }
                catch (IOException e)
                {
                    String msg = "Installed Rhodes have old version, need rhodes version equal or greater " 
                        + CommonConstants.rhodesVersion + " Please reinstall it (See 'http://docs.rhomobile.com/rhodes/install' for more information)";
                    DialogUtils.error("Error", msg);
                    return;
                }
            }

            if (!infoHolder.existCreate)
            {
                createProjectFiles(infoHolder, monitor);
            }
            
            monitor.setTaskName("Create project...");
            IRhomobileProject newProject = ProjectFactory.getInstance().createProject(RhodesProject.class, infoHolder);
            newProject.refreshProject();
            
            ShowPerspectiveJob job = new ShowPerspectiveJob("show rhodes perspective",
                UiConstants.rhodesPerspectiveId);
            job.schedule();

            monitor.worked(1);
        }
        catch (IOException e)
        {
            String msg = "Cannot find Rhodes, need rhodes version equal or greater " 
                + CommonConstants.rhodesVersion + " (See 'http://docs.rhomobile.com/rhodes/install' for more information)";
            DialogUtils.error("Error", msg);
        }
        catch (AlredyCreatedException e)
        {
            DialogUtils.warning("Warning", e.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize
     * from it.
     * 
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection)
    {      
        IAdaptable firstElement = (IAdaptable) selection.getFirstElement();
        String s = firstElement.toString();
        
        String[] del = s.split("Repository[");
            
        this.selection = selection;
    }
}