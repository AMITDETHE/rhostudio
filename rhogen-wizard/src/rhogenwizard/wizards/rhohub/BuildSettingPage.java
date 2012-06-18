package rhogenwizard.wizards.rhohub;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import rhogenwizard.Activator;
import rhogenwizard.DialogUtils;
import rhogenwizard.constants.ConfigurationConstants;
import rhogenwizard.rhohub.RemotePlatformDesc;
import rhogenwizard.rhohub.RemotePlatformList;
import rhogenwizard.rhohub.RhoHub;

class RemotePlatformAdapter implements Callable<RemotePlatformList>
{    
    @Override
    public RemotePlatformList call() throws Exception
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        if (store == null)
            return null;        
        
        return RhoHub.getInstance(store).getPlatformList();
    }
}

public class BuildSettingPage extends WizardPage 
{
    private static int waitTimeOutRhoHubServer = 5;
    
    private Combo m_comboPlatforms         = null;
    private Combo m_comboRhodesAppVersions = null;
    private Text  m_textAppBranch          = null;
    
    private RemotePlatformList         m_remotePlatforms = null;
    private Future<RemotePlatformList> m_getPlatfomListFuture = null;
    
    /**
     * Constructor for SampleNewWizardPage.
     * 
     * @param pageName
     */
    public BuildSettingPage() 
    {
        super("wizardPage");
        setTitle("RhoHub build application wizard");
        setDescription("RhoHub build application wizard");        
    }
    
    @Override
    public void setVisible(boolean visible)
    {
        try
        {
            updateStatus("Please wait until the server request.");
            
            m_remotePlatforms = m_getPlatfomListFuture.get(waitTimeOutRhoHubServer, TimeUnit.MINUTES);            
            initializePlatformsCombo();
            
            updateStatus("Please select parameters.");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            DialogUtils.error("Error", "Not response from Rhohub server. Please try run build sometime later.");
            e.printStackTrace();
        }
        catch (TimeoutException e)
        {
            DialogUtils.error("Error", "Not response from Rhohub server. Please try run build sometime later.");
            e.printStackTrace();
        }
        
        super.setVisible(visible);
    }

    public void createAppSettingBarControls(Composite composite)
    {   
        GridLayout layout = new GridLayout(3, false);
        layout.verticalSpacing = 9;
        
        composite.setLayout(layout);
        
        GridData textAligment = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        
        GridData checkBoxAligment = new GridData();
        checkBoxAligment.horizontalAlignment = GridData.FILL;
        checkBoxAligment.horizontalSpan = 3;
                    
        // 1 row
        Label label = new Label(composite, SWT.NULL);
        label.setText("&Platform:");
        
        m_comboPlatforms = new Combo(composite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        m_comboPlatforms.setLayoutData(textAligment);
        m_comboPlatforms.addModifyListener(new ModifyListener() 
        {
            public void modifyText(ModifyEvent e) 
            {
                dialogChanged();
            }
        });
        
        label = new Label(composite, SWT.NULL);
        
        // 2 row
        label = new Label(composite, SWT.NULL);
        label.setText("&Rhodes version:");
        
        m_comboRhodesAppVersions = new Combo(composite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        m_comboRhodesAppVersions.setLayoutData(textAligment);
        m_comboRhodesAppVersions.addModifyListener(new ModifyListener() 
        {
            public void modifyText(ModifyEvent e) 
            {
                dialogChanged();
            }
        });
        
        label = new Label(composite, SWT.NULL);
        
        // 3 row
        label = new Label(composite, SWT.NULL);
        label.setText("&Application branch:");
        
        m_textAppBranch = new Text(composite, SWT.BORDER | SWT.SINGLE);
        m_textAppBranch.setLayoutData(textAligment);
        m_textAppBranch.addModifyListener(new ModifyListener() 
        {
            public void modifyText(ModifyEvent e) 
            {
                dialogChanged();
            }
        });
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) 
    {   
        Composite container = new Composite(parent, SWT.NULL);
        
        createAppSettingBarControls(container);
        
        initialize();
        setControl(container);
    }
    
    private void initializePlatformsCombo()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
       
        if (store == null || m_remotePlatforms == null || m_remotePlatforms.size() == 0)
        {
            DialogUtils.error("Error", "Rhohub server is not avaialible. Please try run build sometime later.");
            
            m_comboPlatforms.setEnabled(false);
            m_comboRhodesAppVersions.setEnabled(false);            
            this.getShell().close();
            
            return;
        }
        else
        {
            for (RemotePlatformDesc d : m_remotePlatforms)
            {
                m_comboPlatforms.add(d.getPublicName());    
            }
            m_comboPlatforms.select(0);
            
            String platformText = store.getString(ConfigurationConstants.rhoHubSelectedPlatform);
                        
            for (int i=0; i < m_comboPlatforms.getItemCount(); ++i)
            {
                if (m_comboPlatforms.getItem(i).equals(platformText))
                {
                    m_comboPlatforms.select(i);
                    break;
                }
            }
        }
    }
    
    private void initialize() 
    {       
        setDescription("");
        
        m_comboPlatforms.setEnabled(true);
        m_comboRhodesAppVersions.setEnabled(true);
        
        // run async request to rhohub server
        ExecutorService executor = Executors.newSingleThreadExecutor();
        m_getPlatfomListFuture = executor.submit(new RemotePlatformAdapter());

        //TODO HOT-FIX - need call enumerate func for rhodes versions
        m_comboRhodesAppVersions.add("master");
        m_comboRhodesAppVersions.add("3.3.2");
        m_comboRhodesAppVersions.select(0);
        
        m_textAppBranch.setText("master");
    }

    /**
     * Ensures that both text fields are set.
     */
    private void dialogChanged()
    {
        if (m_comboPlatforms.getText().isEmpty())
        {
            updateStatus("RhoHub platform should be selected");
            return;
        }

        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        if (store == null)
            return;
        
        store.setValue(ConfigurationConstants.rhoHubSelectedPlatform, "");
        store.setValue(ConfigurationConstants.rhoHubSelectedRhodesVesion, m_comboRhodesAppVersions.getText());

        // if not selected item from list in store stored empty string
        for (RemotePlatformDesc pl : m_remotePlatforms)
        {
            if (pl.getPublicName().equals(m_comboPlatforms.getText()))
            {
                store.setValue(ConfigurationConstants.rhoHubSelectedPlatform, pl.getInternalName());
                break;
            }
        }
        
        updateStatus("Press finish for creation of project");
        updateStatus(null);
    }

    private void updateStatus(String message)
    {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
}