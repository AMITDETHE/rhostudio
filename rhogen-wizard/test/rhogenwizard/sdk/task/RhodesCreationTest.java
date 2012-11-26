package rhogenwizard.sdk.task;

import java.io.File;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import rhogenwizard.ConsoleHelper;
import rhogenwizard.OSHelper;
import rhogenwizard.sdk.task.generate.GenerateRhodesAppTask;
import rhogenwizard.sdk.task.generate.GenerateRhodesExtensionTask;
import rhogenwizard.sdk.task.generate.GenerateRhodesModelTask;
import rhogenwizard.sdk.task.generate.GenerateRhodesSpecTask;

public class RhodesCreationTest extends TestCase
{
    private static final String workspaceFolder = new File(System.getProperty("java.io.tmpdir"),
                                                    "junitworkfiles").getPath();

    private boolean checkCreateRhodesFile(String path)
    {
        return OSHelper.concat(path, "build.yml").isFile();
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        ConsoleHelper.setupNullConsoles();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
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
    public void testCreateRhodesApp() throws Exception
    {
        String appName = "test001";

        RunTask task = new GenerateRhodesAppTask(workspaceFolder, appName);
        task.run();
        assertTrue(task.isOk());

        assertTrue(checkCreateRhodesFile(workspaceFolder + File.separator + appName));
    }

    @Test
    public void testCreateRhodesModel() throws Exception
    {
        String appName = "test002";
        String modelName = "model002";
        String projectLoc = workspaceFolder + File.separator + appName;

        // create application
        {
            RunTask task = new GenerateRhodesAppTask(workspaceFolder, appName);
            task.run();
            assertTrue(task.isOk());

            assertTrue(checkCreateRhodesFile(projectLoc));
        }

        // create model
        {
            RunTask task = new GenerateRhodesModelTask(projectLoc, modelName, "a, b, c");
            task.run();
            assertTrue(task.isOk());

            // TODO: why model directory is capitalized?
            assertTrue(OSHelper.concat(projectLoc, "app", "Model002").isDirectory());
            assertTrue(OSHelper.concat(projectLoc, "app", "test", "model002_spec.rb").isFile());
        }
    }

    @Test
    public void testCreateRhodesExtension() throws Exception
    {
        String appName = "test005";
        String extensionName = "extension005";
        String projectLoc = workspaceFolder + File.separator + appName;

        // create application
        {
            RunTask task = new GenerateRhodesAppTask(workspaceFolder, appName);
            task.run();
            assertTrue(task.isOk());

            assertTrue(checkCreateRhodesFile(projectLoc));
        }

        // create extension
        {
            RunTask task = new GenerateRhodesExtensionTask(projectLoc, extensionName);
            task.run();
            assertTrue(task.isOk());

            // TODO: why extension directory is capitalized?
            assertTrue(OSHelper.concat(projectLoc, "app", "Extension005Test").isDirectory());
            assertTrue(OSHelper.concat(projectLoc, "extensions", extensionName).isDirectory());
        }
    }

    @Test
    public void testCreateRhodesSpec() throws Exception
    {
        String appName = "test006";
        String projectLoc = workspaceFolder + File.separator + appName;

        // create application
        {
            RunTask task = new GenerateRhodesAppTask(workspaceFolder, appName);
            task.run();
            assertTrue(task.isOk());

            assertTrue(checkCreateRhodesFile(projectLoc));
        }

        // create spec
        {
            RunTask task = new GenerateRhodesSpecTask(projectLoc);
            task.run();
            assertTrue(task.isOk());

            // TODO: why extension directory is capitalized?
            assertTrue(OSHelper.concat(projectLoc, "app", "mspec.rb").isFile());
            assertTrue(OSHelper.concat(projectLoc, "app", "spec_runner.rb").isFile());
            assertTrue(OSHelper.concat(projectLoc, "app", "SpecRunner").isDirectory());
        }
    }
}
