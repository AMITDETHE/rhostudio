package rhogenwizard.wizards.rhohub;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
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

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class BuildSettingPage extends WizardPage 
{
    private Combo m_comboPlatforms = null;
    
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
        
        // 2 row
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

    /**
     * Tests if the current workbench selection is a suitable container to use.
     */
    private void initialize() 
    {       
        setDescription("");
        
        m_comboPlatforms.setEnabled(true);
        
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        if (store == null)
            return;
       
        RemotePlatformList remotePlatforms = RhoHub.getInstance(store).getPlatformList();
        
        if (remotePlatforms == null)
        {
            DialogUtils.error("Error", "Rhohub server is not avaialible");
            m_comboPlatforms.setEnabled(false);
            return;
        }
        else
        {
            for (RemotePlatformDesc d : remotePlatforms)
            {
                m_comboPlatforms.add(d.getPublicName());    
            }
            
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
        
        store.setValue(ConfigurationConstants.rhoHubSelectedPlatform, m_comboPlatforms.getText());
        
        updateStatus("Press finish for creation of project");
        updateStatus(null);
    }

    private void updateStatus(String message)
    {
        setErrorMessage(message);
        setPageComplete(message == null);
    }
}