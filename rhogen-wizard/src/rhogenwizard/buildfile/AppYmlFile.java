package rhogenwizard.buildfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

import rhogenwizard.RunExeHelper;

public class AppYmlFile extends YmlFile 
{
	public static final String configFileName = "build.yml"; 

	public static AppYmlFile createFromProject(IProject project) throws FileNotFoundException 
	{
		if (project != null)
		{
			String projectPath = project.getLocation().toOSString();
			String projectFullPath = projectPath + "/" + configFileName; 
			
			AppYmlFile ymlFile = new AppYmlFile(projectFullPath);
			
			if (ymlFile.getData() == null)
				return null;
			
			return ymlFile;
		}
		
		return null;
	}
	
	public static boolean isExists(String projectPath)
	{
		String buildFilePath = projectPath + File.separator + configFileName;
		File buildFile = new File(buildFilePath);
		return buildFile.exists();
	}
	
	public AppYmlFile(String ymlFileName) throws FileNotFoundException 
	{
		super(ymlFileName);
	}

	public AppYmlFile(File ymlFile) throws FileNotFoundException 
	{
		super(ymlFile);
	}
	
	public String getSdkConfigPath()
	{
		return getSdkPath() + File.separator + SdkYmlFile.configName;
	}
	
	public String getAppLog()
	{
		return super.getString("applog");
	}
	
	public String getSdkPath()
	{
		String sdkPath = (String) super.get("sdk");
		
		if (sdkPath == null)
		{
			sdkPath = RunExeHelper.getSdkInfo();
		}
		
		return sdkPath; 
	}
	
	public void setAppLog(String appLog)
	{
		super.set("applog", appLog);
	}
	
	public void setSdkPath(String sdkPath)
	{
		super.set("sdk", sdkPath);
	}
	
	public void setCapabilities(List<String> capList)
	{
		super.set("capabilities", capList);
	}
	
	public List<String> getCapabilities()
	{
		return (List<String>)super.getObject("capabilities");
	}
	
	public String getAppName()
	{
		return super.getString("name");
	}
	
	public void setAppName(String appName)
	{
		super.set("name", appName);
	}

	public String getAndroidVer() 
	{
		if (super.getObject("android", "version") != null)
		{
			return super.getObject("android", "version").toString();
		}
		
		return null;
	}

	public String getBlackberryVer() 
	{
		return super.get("bbver").toString();
	}

	public void setAndroidVer(String selVersion)
	{
		super.set("android", "version", selVersion);
	}

	public void setBbVer(String selVersion) 
	{
		super.set("bbver", selVersion);
	}

	public String getAndroidEmuName() 
	{
		return super.get("android", "emulator");
	}

	public void setAndroidEmuName(String newName)
	{
		super.set("android", "emulator", newName);
	}

	public String getIphoneVer() 
	{
		return super.get("iphone", "emulatortarget");
	}
	
	public void setIphoneVer(String iphoneTarget) 
	{
		super.set("iphone", "emulatortarget", iphoneTarget);
	}

	public void removeAndroidEmuName() 
	{
		remove("android", "emulator");
	}
}
