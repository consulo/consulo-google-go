package ro.redeul.google.go.ide;

import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.intellij.openapi.options.ConfigurationException;

/**
 * Author: Toader Mihai Claudiu <mtoader@gmail.com>
 * <p/>
 * Date: 5/29/11
 * Time: 11:33 AM
 */
public class GoConfigurableForm {

    public JPanel componentPanel;

    private JRadioButton internalBuildSystemRadioButton;
    private JRadioButton makefileBasedRadioButton;

    public void enableShowHide(){
    }

    public boolean isModified(GoProjectSettings.GoProjectSettingsBean settingsBean) {
        switch (settingsBean.BUILD_SYSTEM_TYPE) {
            case Internal:
                return !internalBuildSystemRadioButton.isSelected();
            case Makefile:
                return !makefileBasedRadioButton.isSelected();
        }

        return false;
    }

    public void apply(GoProjectSettings.GoProjectSettingsBean settingsBean) throws ConfigurationException {
        if ( internalBuildSystemRadioButton.isSelected() ) {
            settingsBean.BUILD_SYSTEM_TYPE = GoProjectSettings.BuildSystemType.Internal;
        } else if ( makefileBasedRadioButton.isSelected() ) {
            settingsBean.BUILD_SYSTEM_TYPE = GoProjectSettings.BuildSystemType.Makefile;
        }

    }

    public void reset(GoProjectSettings.GoProjectSettingsBean settingsBean) {
        switch (settingsBean.BUILD_SYSTEM_TYPE) {
            case Internal:
                internalBuildSystemRadioButton.setSelected(true);
                break;
            case Makefile:
                makefileBasedRadioButton.setSelected(true);
                break;
        }
    }
}
