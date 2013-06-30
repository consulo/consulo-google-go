package ro.redeul.google.go.components;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.projectRoots.impl.SdkImpl;
import ro.redeul.google.go.config.sdk.GoSdkData;
import ro.redeul.google.go.config.sdk.GoSdkType;
import ro.redeul.google.go.sdk.GoSdkUtil;

/**
 * Author: Toader Mihai Claudiu <mtoader@gmail.com>
 * <p/>
 * Date: 7/26/11
 * Time: 1:17 PM
 */
@Deprecated //TODO [VISTALL] use BundledSdkProvider
public class GoBundledSdkDetector implements ApplicationComponent {

    private static final Logger LOG = Logger.getInstance("#ro.redeul.google.go.components.GoBundledSdkDetector");

    @Override
    public void initComponent() {
        final SdkTable jdkTable = SdkTable.getInstance();

        List<Sdk> goSdks = GoSdkUtil.getSdkOfType(GoSdkType.getInstance());

        String homePath = PathManager.getHomePath() + "/bundled/go-sdk";

        File bundledGoSdkHomePath = new File(homePath);
        if ( ! bundledGoSdkHomePath.exists() || ! bundledGoSdkHomePath.isDirectory() ) {
            return;
        }

        LOG.debug("Bundled Go SDK path exists: " + homePath);

        for (Sdk sdk : goSdks) {
            if ( homePath.startsWith(sdk.getHomePath()) ) {
                LOG.debug("Bundled Go SDK at registered already with name: " + sdk.getName());
                return;
            }
        }

        // validate the sdk
        GoSdkData sdkData = GoSdkUtil.testGoogleGoSdk(homePath);

        if ( sdkData == null ) {
            // skip since the folder isn't a proper go sdk
            return;
        }

        LOG.info("We have a bundled go sdk (at " + homePath + ") that is not in the jdk table. Attempting to add");
        try {
            final SdkImpl bundledGoSdk;
            final GoSdkType goSdkType = GoSdkType.getInstance();

            goSdkType.setSdkData(sdkData);
            String newSdkName = SdkConfigurationUtil.createUniqueSdkName(goSdkType, sdkData.GO_HOME_PATH, Arrays.asList(jdkTable.getAllSdks()));
            bundledGoSdk = new SdkImpl(newSdkName, goSdkType);
            bundledGoSdk.setHomePath(homePath);
			bundledGoSdk.setBundled();
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    final SdkModificator sdkModificator = bundledGoSdk.getSdkModificator();
                    goSdkType.setupSdkPaths(bundledGoSdk);
                    jdkTable.addSdk(bundledGoSdk);
                }
            });

        } catch (Exception e) {
            LOG.error("Exception while adding the bundled sdk", e);
        }
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "GoBundledSdkDetector";
    }
}
