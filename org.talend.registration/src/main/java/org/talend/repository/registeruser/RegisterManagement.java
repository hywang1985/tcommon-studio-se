// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.registeruser;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.adaptor.LocationManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.update.core.SiteManager;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.prefs.PreferenceManipulator;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.RegistrationPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.registeruser.proxy.RegisterUserPortTypeProxy;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;

/**
 * DOC mhirt class global comment. Detailled comment <br/>
 * 
 * $Id: RegisterManagement.java 38235 2010-03-10 03:32:12Z nrousseau $
 * 
 */
public class RegisterManagement {

    private static final int REGISTRATION_MAX_TRIES = 6;

    // REGISTRATION_DONE = 1 : registration OK
    private static final int REGISTRATION_DONE = 2;

    private static RegisterManagement instance = null;

    private static Long registNumber = null;

    private static String perfileName = "connection_user.properties"; //$NON-NLS-1$

    private static String path = null;

    private static File perfile = null;

    private static Properties proper = null;

    public static RegisterManagement getInstance() {
        if (instance == null) {
            instance = new RegisterManagement();
        }
        return instance;
    }

    @Deprecated
    public boolean register(String email, String country, boolean isProxyEnabled, String proxyHost, String proxyPort,
            String designerVersion, String projectLanguage, String osName, String osVersion, String javaVersion,
            long totalMemory, Long memRAM, int nbProc) throws BusinessException {
        registNumber = null;
        BigInteger result = BigInteger.valueOf(-1);

        // if proxy is enabled
        if (isProxyEnabled) {
            // get parameter and put them in System.properties.
            System.setProperty("http.proxyHost", proxyHost); //$NON-NLS-1$
            System.setProperty("http.proxyPort", proxyPort); //$NON-NLS-1$

            // override automatic update parameters
            if (proxyPort != null && proxyPort.trim().equals("")) { //$NON-NLS-1$
                proxyPort = null;
            }
            SiteManager.setHttpProxyInfo(true, proxyHost, proxyPort);
        }

        RegisterUserPortTypeProxy proxy = new RegisterUserPortTypeProxy();
        proxy.setEndpoint("http://www.talend.com/TalendRegisterWS/registerws.php"); //$NON-NLS-1$
        try {
            IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                    IBrandingService.class);
            result = proxy.registerUserWithAllUserInformationsAndReturnId(email, country, designerVersion, brandingService
                    .getShortProductName(), projectLanguage, osName, osVersion, javaVersion, totalMemory + "", memRAM //$NON-NLS-1$
                    + "", nbProc + ""); //$NON-NLS-1$ //$NON-NLS-2$
            if (result.signum() > 0) {
                PlatformUI.getPreferenceStore().setValue("REGISTRATION_DONE", 1); //$NON-NLS-1$
                registNumber = result.longValue();
                // validateRegistration(brandingService.getAcronym(), result.longValue());
                PreferenceManipulator prefManipulator = new PreferenceManipulator();
                // prefManipulator.addUser(email);
                // prefManipulator.setLastUser(email);

                // Create a default connection:
                if (prefManipulator.readConnections().isEmpty()) {
                    ConnectionBean recup = ConnectionBean.getDefaultConnectionBean();
                    recup.setUser(email);
                    recup.setComplete(true);
                    prefManipulator.addConnection(recup);
                }

            }
        } catch (RemoteException e) {
            decrementTry();
            throw new BusinessException(e);
        }
        return result.signum() > 0;
    }

    public boolean updateUser(String email, String pseudo, String oldPassword, String password, String firstname,
            String lastname, String country, boolean isProxyEnabled, String proxyHost, String proxyPort) throws BusinessException {
        BigInteger result = BigInteger.valueOf(-1);
        registNumber = null;
        // if proxy is enabled
        if (isProxyEnabled) {
            // get parameter and put them in System.properties.
            System.setProperty("http.proxyHost", proxyHost); //$NON-NLS-1$
            System.setProperty("http.proxyPort", proxyPort); //$NON-NLS-1$

            // override automatic update parameters
            if (proxyPort != null && proxyPort.trim().equals("")) { //$NON-NLS-1$
                proxyPort = null;
            }
            SiteManager.setHttpProxyInfo(true, proxyHost, proxyPort);
        }

        // OS
        String osName = System.getProperty("os.name"); //$NON-NLS-1$
        String osVersion = System.getProperty("os.version"); //$NON-NLS-1$

        // Java version
        String javaVersion = System.getProperty("java.version"); //$NON-NLS-1$

        // Java Memory
        long totalMemory = Runtime.getRuntime().totalMemory();

        // RAM
        com.sun.management.OperatingSystemMXBean composantSystem = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        Long memRAM = new Long(composantSystem.getTotalPhysicalMemorySize() / 1024);

        // CPU
        int nbProc = Runtime.getRuntime().availableProcessors();

        // VERSION

        String version = RegistrationPlugin.getDefault().getBundle().getHeaders()
                .get(org.osgi.framework.Constants.BUNDLE_VERSION).toString();

        RegisterUserPortTypeProxy proxy = new RegisterUserPortTypeProxy();
        proxy.setEndpoint("http://www.talend.com/TalendRegisterWS/registerws.php"); //$NON-NLS-1$
        try {
            IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                    IBrandingService.class);
            result = proxy.updateUser(email, pseudo, oldPassword, password, firstname, lastname, country, version,
                    brandingService.getShortProductName(), osName, osVersion, javaVersion, totalMemory + "", memRAM //$NON-NLS-1$
                            + "", nbProc + ""); //$NON-NLS-1$ //$NON-NLS-2$
            if (result != null && result.signum() > 0) {
                PlatformUI.getPreferenceStore().setValue("REGISTRATION_DONE", 1); //$NON-NLS-1$
                saveRegistoryBean();
                registNumber = result.longValue();
                // validateRegistration(brandingService.getAcronym(), result.longValue());
                PreferenceManipulator prefManipulator = new PreferenceManipulator();
                // prefManipulator.addUser(email);
                // prefManipulator.setLastUser(email);

                // Create a default connection:
                if (prefManipulator.readConnections().isEmpty()) {
                    ConnectionBean recup = ConnectionBean.getDefaultConnectionBean();
                    recup.setUser(email);
                    recup.setComplete(true);
                    prefManipulator.addConnection(recup);
                }
            } else {
                checkErrors(result.intValue());
            }
        } catch (RemoteException e) {
            decrementTry();
            throw new BusinessException(e);
        }
        if (result != null) {
            return result.signum() > 0;
        } else {
            return false;
        }
    }

    public boolean createUser(String email, String pseudo, String password, String firstname, String lastname, String country,
            boolean isProxyEnabled, String proxyHost, String proxyPort) throws BusinessException {
        BigInteger result = BigInteger.valueOf(-1);
        registNumber = null;
        // if proxy is enabled
        if (isProxyEnabled) {
            // get parameter and put them in System.properties.
            System.setProperty("http.proxyHost", proxyHost); //$NON-NLS-1$
            System.setProperty("http.proxyPort", proxyPort); //$NON-NLS-1$

            // override automatic update parameters
            if (proxyPort != null && proxyPort.trim().equals("")) { //$NON-NLS-1$
                proxyPort = null;
            }
            SiteManager.setHttpProxyInfo(true, proxyHost, proxyPort);
        }

        // OS
        String osName = System.getProperty("os.name"); //$NON-NLS-1$
        String osVersion = System.getProperty("os.version"); //$NON-NLS-1$

        // Java version
        String javaVersion = System.getProperty("java.version"); //$NON-NLS-1$

        // Java Memory
        long totalMemory = Runtime.getRuntime().totalMemory();

        // RAM
        com.sun.management.OperatingSystemMXBean composantSystem = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        Long memRAM = new Long(composantSystem.getTotalPhysicalMemorySize() / 1024);

        // CPU
        int nbProc = Runtime.getRuntime().availableProcessors();

        // VERSION

        String version = RegistrationPlugin.getDefault().getBundle().getHeaders()
                .get(org.osgi.framework.Constants.BUNDLE_VERSION).toString();

        RegisterUserPortTypeProxy proxy = new RegisterUserPortTypeProxy();
        proxy.setEndpoint("http://www.talend.com/TalendRegisterWS/registerws.php"); //$NON-NLS-1$
        try {
            IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                    IBrandingService.class);
            result = proxy.createUser(email, pseudo, password, firstname, lastname, country, version, brandingService
                    .getShortProductName(), osName, osVersion, javaVersion, totalMemory + "", memRAM //$NON-NLS-1$
                    + "", nbProc + ""); //$NON-NLS-1$ //$NON-NLS-2$
            if (result.signum() > 0) {
                PlatformUI.getPreferenceStore().setValue("REGISTRATION_DONE", 1); //$NON-NLS-1$
                saveRegistoryBean();
                registNumber = result.longValue();
                // validateRegistration(brandingService.getAcronym(), result.longValue());
                PreferenceManipulator prefManipulator = new PreferenceManipulator();
                // prefManipulator.addUser(email);
                // prefManipulator.setLastUser(email);

                // Create a default connection:
                if (prefManipulator.readConnections().isEmpty()) {
                    ConnectionBean recup = ConnectionBean.getDefaultConnectionBean();
                    recup.setUser(email);
                    recup.setComplete(true);
                    prefManipulator.addConnection(recup);
                }
            } else {
                checkErrors(result.intValue());
            }
        } catch (RemoteException e) {
            decrementTry();
            throw new BusinessException(e);
        }
        return result.signum() > 0;
    }

    private void checkErrors(int signum) {
        String message = ""; //$NON-NLS-1$
        switch (signum) {
        case -10:
            message = Messages.getString("RegisterManagement.impossible"); //$NON-NLS-1$
            break;
        case -110:
            message = Messages.getString("RegisterManagement.userNameInDatabase"); //$NON-NLS-1$
            break;
        case -120:
            message = Messages.getString("RegisterManagement.alreadyRegistered"); //$NON-NLS-1$
            break;
        case -130:
            message = Messages.getString("RegisterManagement.userNameInvalid"); //$NON-NLS-1$
            break;
        case -140:
            message = Messages.getString("RegisterManagement.passwdInvalid"); //$NON-NLS-1$
            break;
        case -150:
            message = Messages.getString("RegisterManagement.userNameDifferent"); //$NON-NLS-1$
            break;
        case -160:
            message = Messages.getString("RegisterManagement.notInBlackList"); //$NON-NLS-1$
            break;
        case -170:
            message = Messages.getString("RegisterManagement.emailNotContain"); //$NON-NLS-1$
            break;
        case -180:
            message = Messages.getString("RegisterManagement.emailInvalid"); //$NON-NLS-1$
            break;
        case -190:
            message = Messages.getString("RegisterManagement.emailNotInBlackList"); //$NON-NLS-1$
            break;
        case -200:
            message = Messages.getString("RegisterManagement.userNameInDatabase"); //$NON-NLS-1$
            break;
        case -210:
            message = Messages.getString("RegisterManagement.userNameCharacter"); //$NON-NLS-1$
            break;
        case -220:
            message = Messages.getString("RegisterManagement.userNameInvalid"); //$NON-NLS-1$
            break;
        case -230:
            message = Messages.getString("RegisterManagement.realnameInvalid"); //$NON-NLS-1$
            break;
        case -240:
            message = Messages.getString("RegisterManagement.emailInvalid"); //$NON-NLS-1$
            break;
        case -300:
            message = Messages.getString("RegisterManagement.passwordWrong"); //$NON-NLS-1$
            break;
        default:
            signum = -1;
        }
        MessageDialog.openError(null, Messages.getString("RegisterManagement.errors"), message); //$NON-NLS-1$
    }

    public String checkUser(String email, boolean isProxyEnabled, String proxyHost, String proxyPort) throws BusinessException {

        // if proxy is enabled
        if (isProxyEnabled) {
            // get parameter and put them in System.properties.
            System.setProperty("http.proxyHost", proxyHost); //$NON-NLS-1$
            System.setProperty("http.proxyPort", proxyPort); //$NON-NLS-1$

            // override automatic update parameters
            if (proxyPort != null && proxyPort.trim().equals("")) { //$NON-NLS-1$
                proxyPort = null;
            }
            SiteManager.setHttpProxyInfo(true, proxyHost, proxyPort);
        }

        RegisterUserPortTypeProxy proxy = new RegisterUserPortTypeProxy();
        proxy.setEndpoint("http://www.talend.com/TalendRegisterWS/registerws.php"); //$NON-NLS-1$
        try {
            return proxy.checkUser(email);
        } catch (RemoteException e) {
            throw new BusinessException(e);
        }
    }

    public void validateRegistration() {
        if (registNumber == null) {
            // PTODO
            // if did not register this time.
            return;
        }
        URL registURL = null;
        try {
            IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                    IBrandingService.class);
            registURL = new URL("http://www.talend.com/designer_post_reg.php?prd=" + brandingService.getAcronym() + "&cid=" //$NON-NLS-1$ //$NON-NLS-2$
                    + registNumber);
            PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(registURL);
        } catch (PartInitException e) {
            // if no default browser (like on linux), try to open directly with firefox.
            try {
                Runtime.getRuntime().exec("firefox " + registURL.toString()); //$NON-NLS-1$
            } catch (IOException e2) {
                if (PlatformUI.getWorkbench().getBrowserSupport().isInternalWebBrowserAvailable()) {
                    IWebBrowser browser;
                    try {
                        browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser("registrationId"); //$NON-NLS-1$
                        browser.openURL(registURL);
                    } catch (PartInitException e1) {
                        ExceptionHandler.process(e);
                    }
                } else {
                    ExceptionHandler.process(e);
                }
            }
        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * DOC mhirt Comment method "isProductRegistered".
     * 
     * @return
     */
    public boolean isProductRegistered() {
        initPreferenceStore();
        ConnectionUserPerReader read = ConnectionUserPerReader.getInstance();
        String registration = read.readRegistration();
        String registration_done = read.readRegistrationDone();
        if (!registration.equals("2") && !registration_done.equals("1")) { //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        return true;
    }

    /**
     * DOC mhirt Comment method "init".
     * 
     * @return
     */
    private void initPreferenceStore() {
        String tmp = LocationManager.getConfigurationLocation().getURL().getPath();
        String s = new Path(LocationManager.getConfigurationLocation().getURL().getPath()).toFile().getPath();
        path = tmp.substring(tmp.indexOf("/") + 1, tmp.length());//$NON-NLS-1$

    }

    public void saveRegistoryBean() {
        ConnectionUserPerReader read = ConnectionUserPerReader.getInstance();
        read.saveRegistoryBean();
    }

    /**
     * DOC mhirt Comment method "incrementTryNumber".
     */
    public static void decrementTry() {
        IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
        prefStore.setValue("REGISTRATION_TRIES", prefStore.getInt("REGISTRATION_TRIES") - 1); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // public static void main(String[] args) {
    // try {
    // boolean result = RegisterManagement.register("a@a.fr", "fr", "Beta2");
    // System.out.println(result);
    // } catch (BusinessException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
}
