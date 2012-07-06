package rhogenwizard.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.osgi.service.prefs.BackingStoreException;

import rhogenwizard.DialogUtils;
import rhogenwizard.project.ProjectFactory;
import rhogenwizard.project.RhodesProject;
import rhogenwizard.project.RhoelementsProject;
import rhogenwizard.rhohub.IRhoHubSetting;
import rhogenwizard.rhohub.IRhoHubSettingSetter;
import rhogenwizard.rhohub.RhoHub;
import rhogenwizard.rhohub.RhoHubBundleSetting;
import rhogenwizard.wizards.rhohub.BuildWizard;
import rhogenwizard.wizards.rhohub.LinkWizard;

public class RhohubWizardAction implements IWorkbenchWindowActionDelegate
{
    private static int wizardHeigth = 500;
    private static int wizardWidth  = 800;
        
    private IWorkbenchWindow window;

    /**
     * The action has been activated. The argument of the method represents the
     * 'real' action sitting in the workbench UI.
     * 
     * @see IWorkbenchWindowActionDelegate#run
     */
    @Override
    public void run(IAction action)
    {       
        IProject project = ProjectFactory.getInstance().getSelectedProject();

        if (project == null)
        {
            DialogUtils.error("Error", "Before run remote build select RhoMobile project's");
            return;
        }

        if (!RhodesProject.checkNature(project) && !RhoelementsProject.checkNature(project))
        {
            DialogUtils.error("Error", "Remote build can run only for RhoMobile project's");
            return;
        }

        IRhoHubSetting setting = RhoHubBundleSetting.createGetter(project);
        
        if (setting.getToken().isEmpty() || setting.getServerUrl().isEmpty())
        {
            DialogUtils.error("Error", "Before use Rhohub build specify user token and server url in preferences settings.");
            return;
        }
        
        if (!checkProjectProperties(project))
        {
            if (DialogUtils.confirm("Project setting", "For project " + project.getName() + 
                    " not found infomation on RhoHub. Link the project with project on RhoHub server?"))
            {
                LinkWizard linkWizard = new LinkWizard(project);
                
                if (createWizardDialog(linkWizard) == Window.CANCEL)
                    return;
            }
            else
            {
                return;
            }
        }
        
        BuildWizard  buildWizard =  new BuildWizard(project);
        createWizardDialog(buildWizard);
    }
    
    int createWizardDialog(IWizard wizard)
    {
        WizardDialog buildWizardDialog = new WizardDialog(window.getShell(), wizard) 
        {
            @Override
            protected void configureShell(Shell newShell) 
            {
                super.configureShell(newShell);
                newShell.setSize(wizardWidth, wizardHeigth);
            }       
        };
        
        buildWizardDialog.create();
        return buildWizardDialog.open(); 
    }

    private boolean checkProjectProperties(IProject project)
    {
        IRhoHubSetting setting = new RhoHubBundleSetting(project);
        
        if (!setting.isLinking())
        {
            if (RhoHub.getInstance(setting).isRemoteProjectExist(project))
            {
                try
                {
                    IRhoHubSettingSetter settingSave = (IRhoHubSettingSetter)setting;
                    settingSave.setLinking();
                    return true;
                }
                catch (BackingStoreException e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        return setting.isLinking();
    }

    /**
     * Selection in the workbench has been changed. We can change the state of
     * the 'real' action here if we want, but this can only happen after the
     * delegate has been created.
     * 
     * @see IWorkbenchWindowActionDelegate#selectionChanged
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection)
    {
    }

    /**
     * We can use this method to dispose of any system resources we previously
     * allocated.
     * 
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    @Override
    public void dispose()
    {
    }

    /**
     * We will cache window object in order to be able to provide parent shell
     * for the message dialog.
     * 
     * @see IWorkbenchWindowActionDelegate#init
     */
    @Override
    public void init(IWorkbenchWindow window)
    {
        this.window = window;
    }
}
