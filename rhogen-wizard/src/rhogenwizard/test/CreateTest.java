package rhogenwizard.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rhogenwizard.OSHelper;
import rhogenwizard.sdk.facade.RhoTaskHolder;
import rhogenwizard.sdk.helper.TaskResultConverter;
import rhogenwizard.sdk.task.GenerateRhoconnectAdapterTask;
import rhogenwizard.sdk.task.GenerateRhoconnectAppTask;
import rhogenwizard.sdk.task.GenerateRhodesAppTask;
import rhogenwizard.sdk.task.GenerateRhodesExtensionTask;
import rhogenwizard.sdk.task.GenerateRhodesModelTask;

public class CreateTest extends TestCase
{
    private static final String workspaceFolder = new File(
            System.getProperty("java.io.tmpdir"), "junitworkfiles").getPath();

    boolean checkCreateRhodesFile(String path)
    {
        String pathToBuildYml = path + File.separator + "build.yml";
        File f = new File(pathToBuildYml);

        return f.isFile();
    }

    boolean checkCreateRhoconnectFile(String path)
    {
        String pathToBuildYml = path + File.separator + "config.ru";
        File f = new File(pathToBuildYml);

        return f.isFile();
    }

    @Before
    public void setUp() throws Exception
    {
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
    public void test1CreateRhodesApp()
    {
        String appName = "test001";

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(GenerateRhodesAppTask.appName, appName);
        params.put(GenerateRhodesAppTask.workDir, workspaceFolder);

        Map results = RhoTaskHolder.getInstance().runTask(GenerateRhodesAppTask.class, params);

        try
        {
            assertEquals(TaskResultConverter.getResultIntCode(results), 0);
        }
        catch (Exception e)
        {
            fail("fail on check result [test1]");
        }

        assertEquals(checkCreateRhodesFile(workspaceFolder + File.separator + appName), true);
    }

    @Test
    public void test2CreateRhodesModel()
    {
        String appName = "test002";
        String modelName = "model002";
        String projectLoc = workspaceFolder + File.separator + appName;

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(GenerateRhodesAppTask.appName, appName);
        params.put(GenerateRhodesAppTask.workDir, workspaceFolder);

        Map results = RhoTaskHolder.getInstance().runTask(GenerateRhodesAppTask.class, params);

        try
        {
            assertEquals(TaskResultConverter.getResultIntCode(results), 0);
        }
        catch (Exception e)
        {
            fail("fail on check create app result [test2]");
        }

        assertEquals(checkCreateRhodesFile(projectLoc), true);

        // create model
        params.clear();
        params = new HashMap<String, Object>();

        params.put(GenerateRhodesModelTask.modelName, modelName);
        params.put(GenerateRhodesModelTask.workDir, projectLoc);
        params.put(GenerateRhodesModelTask.modelFields, "a, b, c");

        Map modelResults =
                RhoTaskHolder.getInstance().runTask(GenerateRhodesModelTask.class, params);

        try
        {
            assertEquals(TaskResultConverter.getResultIntCode(modelResults), 0);
        }
        catch (Exception e)
        {
            fail("fail on check create model result [test2]");
        }
    }

    @Test
    public void test3CreateRhoconnectApp()
    {
        String appName = "test003";

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(GenerateRhoconnectAppTask.appName, appName);
        params.put(GenerateRhoconnectAppTask.workDir, workspaceFolder);

        Map results =
                RhoTaskHolder.getInstance().runTask(GenerateRhoconnectAppTask.class, params);

        try
        {
            assertEquals(TaskResultConverter.getResultIntCode(results), 0);
        }
        catch (Exception e)
        {
            fail("fail on check result [test3]");
        }

        assertEquals(checkCreateRhoconnectFile(workspaceFolder + File.separator + appName),
                true);
    }

    @Test
    public void test4CreateRhoconnectSrcAdapter()
    {
        String appName = "test004";
        String adapterName = "adapter001";
        String projectLocation = workspaceFolder + File.separator + appName;

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(GenerateRhoconnectAppTask.appName, appName);
        params.put(GenerateRhoconnectAppTask.workDir, workspaceFolder);

        Map results =
                RhoTaskHolder.getInstance().runTask(GenerateRhoconnectAppTask.class, params);

        try
        {
            assertEquals(TaskResultConverter.getResultIntCode(results), 0);
        }
        catch (Exception e)
        {
            fail("fail on check result [test4]");
        }

        assertEquals(checkCreateRhoconnectFile(projectLocation), true);

        params.clear();
        params.put(GenerateRhoconnectAdapterTask.sourceName, adapterName);
        params.put(GenerateRhoconnectAdapterTask.workDir, projectLocation);

        results =
                RhoTaskHolder.getInstance()
                        .runTask(GenerateRhoconnectAdapterTask.class, params);

        try
        {
            assertEquals(TaskResultConverter.getResultIntCode(results), 0);
        }
        catch (Exception e)
        {
            fail("fail on check result [test4]");
        }
    }

    @Test
    public void test5CreateRhodesExtension() throws Exception
    {
        String appName = "test005";
        String extensionName = "extension005";
        String projectLoc = workspaceFolder + File.separator + appName;

        // create application
        {
            Map<String, Object> params = new HashMap<String, Object>();

            params.put(GenerateRhodesAppTask.appName, appName);
            params.put(GenerateRhodesAppTask.workDir, workspaceFolder);

            Map<String, ?> results =
                    RhoTaskHolder.getInstance().runTask(GenerateRhodesAppTask.class, params);

            assertEquals(TaskResultConverter.getResultIntCode(results), 0);

            assertTrue(checkCreateRhodesFile(projectLoc));
        }

        // create extension
        {
            Map<String, Object> params = new HashMap<String, Object>();

            params.put(GenerateRhodesExtensionTask.extName, extensionName);
            params.put(GenerateRhodesExtensionTask.workDir, projectLoc);

            Map<String, ?> modelResults =
                    RhoTaskHolder.getInstance().runTask(GenerateRhodesExtensionTask.class,
                            params);

            assertEquals(TaskResultConverter.getResultIntCode(modelResults), 0);
        }
    }
}
