package rhogenwizard.sdk.task;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.preference.IPreferenceStore;

import rhogenwizard.Activator;
import rhogenwizard.constants.ConfigurationConstants;

public class RunDebugRhoconnectAppTask extends SeqRunTask
{
    public static final String resProcess = "debug-process";

    private static RunTask[] getTasks(final String workDir, final String appName, final ILaunch launch)
    {
        RunTask storeLastSyncRunAppTask = new RunTask()
        {
            @Override
            public Map<String, ?> getResult()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public void run(IProgressMonitor monitor)
            {
                IPreferenceStore store = Activator.getDefault().getPreferenceStore();
                store.setValue(ConfigurationConstants.lastSyncRunApp, workDir);
            }
        };

        RunTask redisStartbgTask = new ARubyTask(workDir, "rake", "redis:startbg");

        RunTask rhoconnectStartdebugTask = new RubyTask()
        {
            @Override
            protected void exec()
            {
                String[] commandLine = { getCommand("rake"), "rhoconnect:startdebug" };

                Process process;
                try
                {
                    process = DebugPlugin.exec(commandLine, new File(workDir));
                }
                catch (CoreException e)
                {
                    m_taskResult.put(resTag, 0);
                    return;
                }

                IProcess debugProcess = DebugPlugin.newProcess(launch, process, appName);

                int resCode = (debugProcess == null) ? 0 : 1;

                m_taskResult.put(resTag, resCode);
                m_taskResult.put(resProcess, debugProcess);
            }
        };

        return new RunTask[] { new StopSyncAppTask(), storeLastSyncRunAppTask, redisStartbgTask,
            rhoconnectStartdebugTask };
    }

    public RunDebugRhoconnectAppTask(String workDir, String appName, ILaunch launch)
    {
        super(getTasks(workDir, appName, launch));
    }
}
