package rhogenwizard.project;

import java.io.File;
import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import rhogenwizard.BuildInfoHolder;
import rhogenwizard.project.extension.AlredyCreatedException;
import rhogenwizard.project.extension.BadProjectTagException;
import rhogenwizard.project.extension.CheckProjectException;
import rhogenwizard.project.extension.ProjectNotFoundExtension;

public class ProjectFactory implements IProjectFactory
{
	private static ProjectFactory factoryInstance = null;
	
	public static IProjectFactory getInstance()
	{
		if (factoryInstance == null)
			factoryInstance = new ProjectFactory();
		
		return (IProjectFactory) factoryInstance;
	}
	    
    /**
     * Just do the basics: create a basic project.
     *
     * @param location
     * @param projectName
     * @throws AlredyCreatedException 
     * @throws CoreException 
     */
    private IProject createBaseProject(BuildInfoHolder projectInfo) throws AlredyCreatedException, CoreException 
    {
        // it is acceptable to use the ResourcesPlugin class
        IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectInfo.appName);

        if (!newProject.exists())
        {
            URI projectLocation = projectInfo.getProjectLocation();
            String path = URIUtil.toPath(projectLocation).toOSString();
            
            if (!projectInfo.existCreate) {
            	path = path + File.separatorChar + projectInfo.appName;
            }

            IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());

            if (isProjectLocationInWorkspace(path))
            	 projectInfo.isInDefaultWs = true;
            
            if (!projectInfo.isInDefaultWs)
            {
            	desc.setLocationURI(URIUtil.toURI(path));
            }
            
            newProject.create(desc, null);
            
            if (!newProject.isOpen()) {
                newProject.open(null);
            }
        }
        else
        {
        	throw new AlredyCreatedException(newProject);
        }

        return newProject;
    }

    private IRhomobileProject createRhomobileProject(Class projectTag, IProject project) throws BadProjectTagException
    {
    	if (projectTag.equals(RhodesProject.class))
    	{
    		return new RhodesProject(project);
    	}
    	else if (projectTag.equals(RhoconnectProject.class))
    	{
    		return new RhoconnectProject(project);
    	}
    	
    	throw new BadProjectTagException(projectTag);
    }
    
    public boolean isProjectLocationInWorkspace(final String projectPath)
    {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root  = workspace.getRoot();
		String wsPath        = root.getLocation().toOSString();
		
		return projectPath.contains(wsPath); 
    }
    
	public IRhomobileProject createProject(Class projectTag, BuildInfoHolder projectInfo) throws CoreException, ProjectNotFoundExtension, AlredyCreatedException, BadProjectTagException
	{
    	IPath projectPath = (IPath) projectInfo.getProjectLocationPath();
		
        Assert.isNotNull(projectInfo.appName);
        Assert.isTrue(projectInfo.appName.trim().length() != 0);

        IProject project = createBaseProject(projectInfo);

        IRhomobileProject rhoProject = createRhomobileProject(projectTag, project);
        
        rhoProject.addNature();
        
        return rhoProject;
	}

	@Override
	public IProject getSelectedProject() 
	{
		IProject project = null;
		
		IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
		
		if (workbenchWindows.length > 0)
		{
			IWorkbenchPage page = workbenchWindows[0].getActivePage(); 
		
			ISelection selection = page.getSelection();
	
			if (selection instanceof IStructuredSelection)
			{
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object res = sel.getFirstElement();
				
				if (res instanceof IResource)
				{
				   project = ((IResource)res).getProject();
				}		
			}
		}
		
		return project;	
	}

	@Override
	public IRhomobileProject convertFromProject(IProject project) 
	{
		//project.getNature(natureId)
		return null;
	}
}
