package org.robolectric.android.plugins;

import com.google.auto.service.AutoService;

import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.plugins.DefaultSdkProvider;
import org.robolectric.versioning.AndroidVersionInitTools;
import org.robolectric.versioning.AndroidVersions;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.jar.JarFile;

import javax.annotation.Priority;

@AutoService(SdkProvider.class)
@Priority(Integer.MAX_VALUE)
public class AndroidLocalSdkProvider extends DefaultSdkProvider {

    public AndroidLocalSdkProvider(DependencyResolver dependencyResolver) {
        super(dependencyResolver);
    }

    protected void populateSdks(TreeMap<Integer, Sdk> knownSdks) {
        super.populateSdks(knownSdks);
        System.out.println("In android populate sdk.");
        DefaultSdk localBuiltSdk = new DefaultSdk(
                10000,
                "current",
                "r0",
                "UpsideDownCake",
                9);
        File location = localBuiltSdk.getJarPath().toFile();
        AndroidVersions.AndroidRelease release = null;
        try {
            JarFile jarFile = new JarFile(location);
            release = AndroidVersionInitTools.computeReleaseVersion(jarFile);
            System.out.println("Found release :" + release.toString());
            if (release != null) {
                int jdkVersion = getMajorJdkVersion();
                System.out.println("JDK version :" + jdkVersion);
                DefaultSdk currentSdk = new DefaultSdk(
                        release.getSdkInt(),
                        "current",
                        "r0",
                        release.getShortCode(),
                        jdkVersion);
                for (int sdkInt : knownSdks.keySet()) {
                    if (sdkInt >= release.getSdkInt()) {
                        System.out.println("Removing :" + sdkInt);
                        knownSdks.remove(sdkInt);
                    }
                }
                knownSdks.put(release.getSdkInt(), currentSdk);
            } else {
                throw new RuntimeException("Could not read the version of the current android-all sdk"
                        + "this prevents robolectric from determining which shadows to apply.");
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Could not read the version of the current android-all sdk"
            + "this prevents robolectric from determining which shadows to apply.", ioe);
        }
    }

    private int getMajorJdkVersion() {
        String javaVersion = System.getProperty("java.version");
        String[] parts = javaVersion.split("\\.");
        return Integer.valueOf(parts[0]);
    }
}
