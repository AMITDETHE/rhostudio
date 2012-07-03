package rhogenwizard.buildfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rhogenwizard.OSValidator;
import rhogenwizard.SysCommandExecutor;

public class SdkYmlAdapter
{
	private static String getPathToYaml() throws Exception
	{
		SysCommandExecutor executor = new SysCommandExecutor();
		
		int ret = executor.runCommand(SysCommandExecutor.RUBY_BAT, Arrays.asList("set-rhodes-sdk"));
		
		if (ret != 0) 
		{
			return null;
		}
		
		String rawPath = executor.getCommandOutput();
		
		rawPath = rawPath.replaceAll("\\p{Cntrl}", "");  
		
		File rawFile = new File(rawPath);
		
		if (!rawFile.isDirectory()) 
		{
			return null;
		}
		
		File parentDir = rawFile.getParentFile();
		
		return parentDir.getAbsolutePath();
	}
	
	public static SdkYmlFile getRhobuildFile() throws Exception
	{
		String pathToRhodes = getPathToYaml();
		
		if (pathToRhodes != null)
		{
			SdkYmlFile ymlFile = new SdkYmlFile(pathToRhodes + File.separator + SdkYmlFile.configName);
		
			return ymlFile;
		}
		
		return null;
	}
	
	public static void setNewRhodesPath(String path) throws Exception
	{
		SysCommandExecutor executor = new SysCommandExecutor();
		
		executor.runCommand(SysCommandExecutor.RUBY_BAT, Arrays.asList("set-rhodes-sdk", path));
	}
}
