package rhogenwizard.sdk.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import rhogenwizard.ConsoleHelper;
import rhogenwizard.ILogDevice;
import rhogenwizard.OSHelper;
import rhogenwizard.OSValidator;
import rhogenwizard.PlatformType;
import rhogenwizard.SysCommandExecutor;
import rhogenwizard.debugger.backend.DebugServer;
import rhogenwizard.debugger.backend.DebugState;
import rhogenwizard.debugger.backend.DebugVariableType;
import rhogenwizard.debugger.backend.IDebugCallback;
import rhogenwizard.sdk.facade.RhoTaskHolder;
import rhogenwizard.sdk.helper.TaskResultConverter;

public class RunDebugRhodesAppTaskTest
{
    private SynchronousQueue<String> m_eventQueue;
    private Semaphore m_semaphore;

    private static class DebugCallback implements IDebugCallback
    {
        private final SynchronousQueue<String> m_eventQueue;
        private final Semaphore m_semaphore;

        public DebugCallback(SynchronousQueue<String> eventQueue, Semaphore semaphore)
        {
            m_eventQueue = eventQueue;
            m_semaphore = semaphore;
        }

        @Override
        public void connected()
        {
            send("connected");
        }

        @Override
        public void stopped(DebugState state, String file, int line, String className,
                String method)
        {
            send("stopped [" + DebugState.getName(state) + "] [" + file + "] ["
                + line + "] [" + className + "] [" + method + "]");
        }

        @Override
        public void resumed()
        {
            send("resumed");
        }

        @Override
        public void evaluation(boolean valid, String code, String value)
        {
            send("evaluation [" + valid + "] [" + code + "] [" + value + "]");
        }

        @Override
        public void unknown(String cmd)
        {
            send("unknown [" + cmd + "]");
        }

        @Override
        public void exited()
        {
            send("exited");
        }

        @Override
        public void watch(DebugVariableType type, String variable, String value)
        {
            send("watch [" + DebugVariableType.getName(type) + "] [" + variable + "] ["
                + value + "]");
        }

        @Override
        public void watchBOL(DebugVariableType type)
        {
            send("watchBOL [" + DebugVariableType.getName(type) + "]");
        }

        @Override
        public void watchEOL(DebugVariableType type)
        {
            send("watchEOL [" + DebugVariableType.getName(type) + "]");
        }

        private void send(String event)
        {
            try
            {
                m_eventQueue.put(event);
                m_semaphore.acquire();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException("Can not send event. Impossible!", e);
            }
        }
    }

    private static ILogDevice nullLogDevice = new ILogDevice()
    {
        @Override
        public void log(String str)
        {
        }
    };
    private static final String workspaceFolder = new File(
            System.getProperty("java.io.tmpdir"), "junitworkfiles").getPath();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        ConsoleHelper.disableConsoles();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
        m_eventQueue = new SynchronousQueue<String>();
        m_semaphore = new Semaphore(0);

        OSHelper.deleteFolder(workspaceFolder);

        File newWsFodler = new File(workspaceFolder);
        newWsFodler.mkdir();
    }

    @After
    public void tearDown() throws Exception
    {
        OSHelper.deleteFolder(workspaceFolder);
    }

    @Test
    public void testRunDebugRhodesAppTask() throws Throwable
    {
        String appName = "app";
        String projectLocation = OSHelper.concat(workspaceFolder, appName).getPath();

        String signature1 = "RhoSimulator -approot=/private" + projectLocation;
        String signature2 =
            "rake run:android:rhosimulator_debug rho_debug_port=9000 rho_reload_app_changes=0";

        Set<Integer> before1 = getProcessesIds(signature1);
        Set<Integer> before2 = getProcessesIds(signature2);

        try
        {
            // create application
            {
                Map<String, Object> params = new HashMap<String, Object>();

                params.put(GenerateRhodesAppTask.appName, appName);
                params.put(GenerateRhodesAppTask.workDir, workspaceFolder);

                Map<String, ?> results =
                    RhoTaskHolder.getInstance().runTask(GenerateRhodesAppTask.class, params);

                assertEquals(0, TaskResultConverter.getResultIntCode(results));
            }

            // write new application.rb
            {
                String text[] =
                {
                    /* 01 */"require 'rho/rhoapplication'",
                    /* 02 */"class AppApplication < Rho::RhoApplication",
                    /* 03 */"  def initialize",
                    /* 04 */"    super",
                    /* 05 */"    x = 0",
                    /* 06 */"    x = x + 1",
                    /* 07 */"    $y = 11",
                    /* 08 */"    m",
                    /* 09 */"    $y = $y + 22",
                    /* 10 */"  end",
                    /* 11 */"  def m",
                    /* 12 */"    z = 0",
                    /* 13 */"    zz = 0",
                    /* 14 */"  end",
                    /* 15 */"end",
                    /* 16 */""
                };
                String appRb =
                    OSHelper.concat(projectLocation, "app", "application.rb").getPath();
                writeTextFile(appRb, join("\n", text));
            }

            // start debug server
            DebugCallback debugCallback = new DebugCallback(m_eventQueue, m_semaphore);
            final DebugServer debugServer = new DebugServer(debugCallback);
            final Throwable[] exception = new Throwable[1];
            Thread debugServerThread = new Thread(new Runnable()
            {
                @Override
                public void run()
            {
                try
                {
                    debugServer.run();
                }
                catch (Throwable t)
                {
                    exception[0] = t;
                }
            }
            });
            debugServerThread.start();

            // run debug Rhodes application [android] [rhosimulator]
            {
                Map<String, Object> params = new HashMap<String, Object>();

                ILaunch launch = new Launch(null, ILaunchManager.DEBUG_MODE, null);

                params.put(RunDebugRhodesAppTask.workDir, projectLocation);
                params.put(RunDebugRhodesAppTask.appName, appName);
                params.put(RunDebugRhodesAppTask.platformType, PlatformType.eAndroid);
                params.put(RunDebugRhodesAppTask.reloadCode, false);
                params.put(RunDebugRhodesAppTask.launchObj, launch);
                params.put(RunDebugRhodesAppTask.traceFlag, false);

                Map<String, ?> results =
                    RhoTaskHolder.getInstance().runTask(RunDebugRhodesAppTask.class, params);
                assertEquals(TaskResultConverter.okCode,
                    TaskResultConverter.getResultIntCode(results));
            }

            suspend("connected");

            debugServer.debugBreakpoint("application.rb", 5);
            debugServer.debugBreakpoint("application.rb", 6);
            debugServer.debugRemoveBreakpoint("application.rb", 5);

            pass(
                "unknown [HOST=127.0.0.1]",
                "unknown [PORT=9000]",
                "unknown [DEBUG PATH=/private" + projectLocation + "/app/]",
                "stopped [breakpoint] [application.rb] [6] [AppApplication] [initialize]");

            debugServer.debugEvaluate("x");

            pass("evaluation [true] [x] [0]");

            debugServer.debugBreakpoint("application.rb", 7);
            debugServer.debugResume();

            pass(
                "resumed",
                "stopped [breakpoint] [application.rb] [7] [AppApplication] [initialize]");

            debugServer.debugEvaluate("x");

            pass("evaluation [true] [x] [1]");

            debugServer.debugStepOver();

            pass(
                "unknown [STEPOVER start]",
                "resumed",
                "stopped [stopped (over)] [application.rb] [8] [AppApplication] [initialize]");

            debugServer.debugEvaluate("$y");

            pass("evaluation [true] [$y] [11]");

            debugServer.debugBreakpoint("application.rb", 12);
            debugServer.debugResume();

            pass(
                "resumed",
                "stopped [breakpoint] [application.rb] [12] [AppApplication] [m]");

            debugServer.debugBreakpoint("application.rb", 13);
            debugServer.debugRemoveAllBreakpoints();
            debugServer.debugStepReturn();

            pass(
                "resumed",
                "stopped [stopped (return)] [application.rb] [9] [AppApplication] [initialize]");

            debugServer.debugEvaluate("(1+2");

            pass("evaluation [false] [(1+2] [\"/private" + projectLocation
                + "/app/application.rb:9: syntax error, unexpected $end, expecting ')'\"]");

            debugServer.debugEvaluate("\"\\\\n\"");

            pass("evaluation [true] [\"\\\\n\"] [\"\\\\n\"]");

            debugServer.debugEvaluate("$y\n2+2 # comment");

            pass("evaluation [true] [$y\n2+2 # comment] [4]");

            debugServer.debugTerminate();

            pass("exited");

            debugServer.shutdown();

            resume();

            debugServerThread.join();
            if (exception[0] != null)
            {
                throw exception[0];
            }

        }
        finally
        {

            Set<Integer> after1 = getProcessesIds(signature1);
            Set<Integer> after2 = getProcessesIds(signature2);

            Set<Integer> diff1 = new HashSet<Integer>(after1);
            diff1.removeAll(before1);
            for (int pid : diff1)
            {
                OSHelper.killProcess(pid);
            }

            Set<Integer> diff2 = new HashSet<Integer>(after2);
            diff2.removeAll(before2);
            for (int pid : diff2)
            {
                OSHelper.killProcess(pid);
            }
        }
    }

    private void suspend(String s) throws InterruptedException
    {
        String event = m_eventQueue.poll(10, TimeUnit.SECONDS);
        if (event == null)
        {
            fail("timeout for \"" + s + "\"");
        }
        assertEquals(s, event);
    }

    private void resume()
    {
        assertEquals(0, m_semaphore.availablePermits());
        m_semaphore.release();
    }

    private void pass(String... events) throws InterruptedException
    {
        for (String event : events)
        {
            resume();
            suspend(event);
        }
    }

    private static String join(String delimiter, String... text)
    {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String line : text)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                sb.append(delimiter);
            }
            sb.append(line);
        }
        return sb.toString();
    }

    private static String readTextFile(String filename) throws IOException
    {
        FileReader fr = new FileReader(filename);
        try
        {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[16 * 1024];
            while (true)
            {
                int read = fr.read(buffer);
                if (read == -1)
                {
                    break;
                }
                sb.append(buffer, 0, read);
            }
            return sb.toString();
        }
        finally
        {
            fr.close();
        }
    }

    private static void writeTextFile(String filename, String text) throws IOException
    {
        FileWriter fw = new FileWriter(filename);
        try
        {
            fw.write(text);
        }
        finally
        {
            fw.close();
        }
    }

    private static String getProcessesListing() throws Exception
    {
        // TODO: add Windows and Linux processing
        if (!OSValidator.isMac())
        {
            return "";
        }

        List<String> cmdLine = Arrays.asList("ps", "ax");

        SysCommandExecutor executor = new SysCommandExecutor();
        executor.setOutputLogDevice(nullLogDevice);
        executor.setErrorLogDevice(nullLogDevice);
        executor.runCommand(cmdLine);

        return executor.getCommandOutput();
    }

    private static Set<Integer> getProcessesIds(String signature) throws Exception
    {
        Pattern pattern = Pattern.compile("^ *(\\d+).*");

        String listing = getProcessesListing();
        Set<Integer> ids = new HashSet<Integer>();
        for (String line : listing.split("\n"))
        {
            if (!line.contains(signature))
            {
                continue;
            }
            Matcher matcher = pattern.matcher(line);
            if (!matcher.matches())
            {
                continue;
            }
            ids.add(Integer.parseInt(matcher.group(1)));
        }
        return ids;
    }
}
