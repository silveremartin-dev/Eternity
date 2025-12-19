/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.game.eternity2.client;


import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.server.EternityUser;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.game.eternity2.util.PasswordEncoderSingleton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * A class to access information on you PC (you are the client) from the server. It is to be used with a front end either command line or graphical.
 * Can also be used as a standalone application to read and display local Board.
 *
 * @author Silvere Martin-Michiellot
 * @version 1.0
 */

//load or save board
    //edit board
    //compute packets, or stop
    //list best scores including best self
    //statistics : packets sent and received
    //create, upadte, delete account
    //options : server address and port, solver
public class EternityClient extends JFrame {

    private static final Logger logger = LogManager.getLogger(EternityClient.class);

    private final static ResourceBundle eternityResourceBundle = ResourceBundle.getBundle("org.game.eternity.Eternity");

    private final static String LOCALHOST = "127.0.0.1";

    private final static int NOTREADY = -1;
    private final static int STOPPED = 0;
    private final static int STARTED = 1;
    private final static int PAUSED = 2;

    private int state;

    private EternityUser user;
    private InetAddress serverIP;
    private int serverPort;

    private Socket socket;
    private boolean isConnected;

    private EternityBoard16x16 currentEternityBoard;

    public EternityClient(@NotNull EternityUser user, @NotNull InetAddress serverIP, int serverPort) {
        state = NOTREADY;
        if ((user != null)) {
            this.user = user;
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            isConnected = false;
        } else throw new IllegalArgumentException("User and serverIP can't be null.");
    }

    public EternityClient(@NotNull EternityUser user, @NotNull InetAddress serverIP) {
        new EternityClient(user, serverIP, new Integer(eternityResourceBundle.getString("ServerPort")).intValue());
    }

    public EternityClient(@NotNull EternityUser user) throws UnknownHostException {
        new EternityClient(user, InetAddress.getByName(eternityResourceBundle.getString("ServerAddress")));
    }

    public EternityUser getUser() {
        return user;
    }

    public InetAddress getServerIP() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void start() throws IOException {
        if (state!=STOPPED || state==NOTREADY) {
            socket = new Socket(serverIP, serverPort);
            state = STARTED;
            logger.info("Client started.");
        } else throw new IllegalStateException("State must be NOTREADY or STOPPED.");
    }

    public void pause() {
        if (state==STARTED) {
            state = PAUSED;
            logger.info("Client paused.");
        } else throw new IllegalStateException("State must be STARTED.");
    }

    public void stop() throws IOException {
        if (state==STARTED || state==PAUSED) {
            socket.close();
            state = STOPPED;
            logger.info("Client stopped.");
        } else throw new IllegalStateException("State must be STARTED or PAUSED.");
    }

    public EternityUser registerAccount() {
        PasswordEncoderSingleton.hashPassword()
    }

    public EternityUser changePassword() {
        PasswordEncoderSingleton.hashPassword()
    }

    public void deleteAccount() {
    }

    //return the known solutions for this user
    public Set<EternityBoard16x16> getSolutions() {

    }

    public void sendSolution(@NotNull EternityBoard16x16 board) throws IOException {
        if (isConnected) {
            DataOutputStream dataOutputStream;
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write(board);
            dataOutputStream.flush();
            dataOutputStream.close();
        } else throw new RuntimeException("Not connected to server.");
    }

    public EternityBoard16x16 waitForJob() {
        EternityBoard16x16 eternityBoard;
        eternityBoard = socket.receive();
        return eternityBoard;
    }

    public static void main(String[] args) throws Exception {
        Options options;
        HelpFormatter formatter;
        CommandLineParser parser;
        CommandLine cmd;
        Option noGUI;
        Option login;
        Option password;

        options = new Options();
        noGUI = new Option("ng", "nogui", false, "Starts the client headless.");
        noGUI.setRequired(false);
        options.addOption(noGUI);
        noGUI = new Option("l", "login", true, "User's login.");
        noGUI.setRequired(false);
        options.addOption(noGUI);
        noGUI = new Option("p", "password", true, "User's password.");
        noGUI.setRequired(false);
        options.addOption(noGUI);
        formatter = new HelpFormatter();
        parser = new DefaultParser();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("EternityClient", options);
            System.exit(1);
            return;
        }
        if (cmd.hasOption("ng") && !cmd.hasOption("l") && !cmd.hasOption("p")) {
        }

        logger.info("Client starting.");

        //choose solver
        BasicEternitySolver
                AdvancedEternitySolver

        if (cmd.hasOption("ng")) {
            EternityNoGUI eternityNoGUI;
            eternityNoGUI = new EternityNoGUI();
        }

        final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash == null) {
            System.out.println("SplashScreen.getSplashScreen() returned null");
            return;
        }
        Graphics2D g = splash.createGraphics();
        if (g == null) {
            System.out.println("g is null");
            return;
        }
        for(int i=0; i<100; i++) {
            renderSplashFrame(g, i);
            splash.update();
            try {
                Thread.sleep(90);
            }
            catch(InterruptedException e) {
            }
        }
        splash.close();

        //also show ranked boards
        //and show boards with tiles
        //and show packet computing progression and number of tests per second

        EternityUser eternityUser = new EternityUser();
        eternityUser.setHardwareArchitecture(System.getProperty("os.arch"));
        eternityUser.setOperatingSystem(System.getProperty("os.name"));
        eternityUser.setOperatingSystemVersion(System.getProperty("os.version"));
        EternityClient eternityClient = new EternityClient(eternityUser);
        eternityClient.start();

        //create engine
        //run
    }

    static void renderSplashFrame(Graphics2D g, int frame) {
        final String[] components = {"images", "tiles", "solvers"};
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(120,140,200,40);
        g.setPaintMode();
        g.setColor(Color.BLACK);
        g.drawString("Loading "+components[(frame/5)%3]+"...", 120, 150);
    }

    public JMenuBar createMenus() {
        JMenuItem mi;
        // ***** create the menuBar ****
        JMenuBar menuBar = new JMenuBar();
        menuBar.getAccessibleContext().setAccessibleName(
                getString("Client.MenuBar.accessible_description"));

        // ***** create File menu
        JMenu fileMenu = menuBar.add(new JMenu(getString("Client.FileMenu.file_label")));
        fileMenu.setMnemonic(getMnemonic("Client.FileMenu.file_mnemonic"));
        fileMenu.getAccessibleContext().setAccessibleDescription(getString("Client.FileMenu.accessible_description"));

        createMenuItem(fileMenu, "Client.FileMenu.about_label", "Client.FileMenu.about_mnemonic",
                "Client.FileMenu.about_accessible_description", new AboutAction(this));

        createMenuItem(fileMenu, "Client.FileMenu.open_label", "Client.FileMenu.open_mnemonic",
                "Client.FileMenu.open_accessible_description", null);

        createMenuItem(fileMenu, "Client.FileMenu.save_label", "Client.FileMenu.save_mnemonic",
                "Client.FileMenu.save_accessible_description", null);

        createMenuItem(fileMenu, "Client.FileMenu.save_as_label", "Client.FileMenu.save_as_mnemonic",
                "Client.FileMenu.save_as_accessible_description", null);
        
        fileMenu.addSeparator();

        createMenuItem(fileMenu, "Client.FileMenu.exit_label", "Client.FileMenu.exit_mnemonic",
                "Client.FileMenu.exit_accessible_description", new ExitAction(this));

        // ***** create Options menu
        JMenu optionsMenu = menuBar.add(new JMenu(getString("Client.OptionsMenu.options_label")));
        optionsMenu.setMnemonic(getMnemonic("Client.OptionsMenu.options_mnemonic"));
        optionsMenu.getAccessibleContext().setAccessibleDescription(getString("Client.OptionsMenu.accessible_description"));

        // ***** create Help menu
        JMenu helpMenu = menuBar.add(new JMenu(getString("Client.HelpMenu.help_label")));
        helpMenu.setMnemonic(getMnemonic("Client.HelpMenu.help_mnemonic"));
        helpMenu.getAccessibleContext().setAccessibleDescription(getString("Client.HelpMenu.accessible_description"));

        createMenuItem(helpMenu, "Client.HelpMenu.help_label", "Client.HelpMenu.help_mnemonic",
                "Client.HelpMenu.help_accessible_description", new AboutAction(this));

        createMenuItem(helpMenu, "Client.HelpMenu.about_label", "Client.HelpMenu.about_mnemonic",
                "Client.HelpMenu.about_accessible_description", new AboutAction(this));

        return menuBar;
    }

    /**
     * Create a checkbox menu menu item
     */
    private JMenuItem createCheckBoxMenuItem(JMenu menu, String label,
                                             String mnemonic,
                                             String accessibleDescription,
                                             Action action) {
        JCheckBoxMenuItem mi = (JCheckBoxMenuItem)menu.add(
                new JCheckBoxMenuItem(getString(label)));
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(
                accessibleDescription));
        mi.addActionListener(action);
        return mi;
    }

    /**
     * Creates a generic menu item
     */
    public JMenuItem createMenuItem(JMenu menu, String label, String mnemonic,
                                    String accessibleDescription, Action action) {
        JMenuItem mi = menu.add(new JMenuItem(getString(label)));
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(action);
        if(action == null) {
            mi.setEnabled(false);
        }
        return mi;
    }

    public JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu("JPopupMenu demo");

        for (LookAndFeelData lafData : lookAndFeelData) {
            createPopupMenuItem(popup, lafData);
        }

        // register key binding to activate popup menu
        InputMap map = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_DOWN_MASK),
                "postMenuAction");
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), "postMenuAction");
        getActionMap().put("postMenuAction", new ActivatePopupMenuAction(this, popup));

        return popup;
    }

    /**
     * Creates a JMenuItem for the Look and Feel popup menu
     */
    public JMenuItem createPopupMenuItem(JPopupMenu menu, LookAndFeelData lafData) {
        JMenuItem mi = menu.add(new JMenuItem(lafData.label));
        popupMenuGroup.add(mi);
        mi.setMnemonic(lafData.mnemonic);
        mi.getAccessibleContext().setAccessibleDescription(lafData.accDescription);
        mi.addActionListener(new ChangeLookAndFeelAction(this, lafData));
        return mi;
    }

    public void showEternityClientApplication() {
        // put EternityClientApplication in a frame and show it
        JFrame f = getFrame();
        f.setTitle(getString("Frame.title"));
        f.getContentPane().add(this, BorderLayout.CENTER);
        f.pack();

        Rectangle screenRect = f.getGraphicsConfiguration().getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
                f.getGraphicsConfiguration());

        // Make sure we don't place the demo off the screen.
        int centerWidth = screenRect.width < f.getSize().width ?
                screenRect.x :
                screenRect.x + screenRect.width/2 - f.getSize().width/2;
        int centerHeight = screenRect.height < f.getSize().height ?
                screenRect.y :
                screenRect.y + screenRect.height/2 - f.getSize().height/2;

        centerHeight = Math.max(centerHeight, screenInsets.top);

        f.setLocation(centerWidth, centerHeight);
        f.setVisible(true);
        numSSs++;
        EternityClientApplications.add(this);
    }

    /**
     * Returns the frame instance
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Returns the menuBar
     */
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Returns the content pane whether we're in an applet
     * or application
     */
    public Container getContentPane() {
        if(contentPane == null) {
            if(getFrame() != null) {
                contentPane = getFrame().getContentPane();
            }
        }
        return contentPane;
    }

    /**
     * Create a frame for EternityClientApplication to reside in if brought up
     * as an application.
     */
    public static JFrame createFrame(GraphicsConfiguration gc) {
        JFrame frame = new JFrame(gc);
        if (numSSs == 0) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            WindowListener l = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    numSSs--;
                    EternityClientApplications.remove(this);
                }
            };
            frame.addWindowListener(l);
        }
        return frame;
    }
    
    /**
     * Set the status
     */
    public void setStatus(String s) {
        // do the following on the gui thread
        SwingUtilities.invokeLater(new EternityClientApplicationRunnable(this, s) {
            public void run() {
                EternityClientApplication.statusField.setText((String) obj);
            }
        });
    }

    /**
     * This method returns a string from the demo's resource bundle.
     */
    public static String getString(String key) {
        String value = null;
        try {
            value = TextAndMnemonicUtils.getTextAndMnemonicString(key);
        } catch (MissingResourceException e) {
            System.out.println("java.util.MissingResourceException: Couldn't find value for: " + key);
        }
        if(value == null) {
            value = "Could not find resource: " + key + "  ";
        }
        return value;
    }

    /**
     * Returns a mnemonic from the resource bundle. Typically used as
     * keyboard shortcuts in menu items.
     */
    public char getMnemonic(String key) {
        return (getString(key)).charAt(0);
    }

    /**
     * Creates an icon from an image contained in the "images" directory.
     */
    public ImageIcon createImageIcon(String filename, String description) {
        String path = "/resources/images/" + filename;
        return new ImageIcon(getClass().getResource(path));
    }
    
    private void updateThisEternityClientApplication() {
        JFrame frame = getFrame();
        if (frame == null) {
            SwingUtilities.updateComponentTreeUI(this);
        } else {
            SwingUtilities.updateComponentTreeUI(frame);
        }

        SwingUtilities.updateComponentTreeUI(popupMenu);
        if (aboutBox != null) {
            SwingUtilities.updateComponentTreeUI(aboutBox);
        }
    }
    
    // *******************************************************
    // **************   ToggleButtonToolbar  *****************
    // *******************************************************
    static Insets zeroInsets = new Insets(1,1,1,1);
    protected static class ToggleButtonToolBar extends JToolBar {
        public ToggleButtonToolBar() {
            super();
            setFloatable(true);
            setRollover(true);
            //https://stackoverflow.com/questions/19070324/how-to-remove-replace-the-close-button-from-a-floating-jtoolbar
        }

        JToggleButton addToggleButton(Action a) {
            JToggleButton tb = new JToggleButton(
                    (String)a.getValue(Action.NAME),
                    (Icon)a.getValue(Action.SMALL_ICON)
            );
            tb.setMargin(zeroInsets);
            tb.setText(null);
            tb.setEnabled(a.isEnabled());
            tb.setToolTipText((String)a.getValue(Action.SHORT_DESCRIPTION));
            tb.setAction(a);
            add(tb);
            return tb;
        }
    }

    // *******************************************************
    // *********  ToolBar Panel / Docking Listener ***********
    // *******************************************************
    static class ToolBarPanel extends JPanel implements ContainerListener {

        public boolean contains(int x, int y) {
            Component c = getParent();
            if (c != null) {
                Rectangle r = c.getBounds();
                return (x >= 0) && (x < r.width) && (y >= 0) && (y < r.height);
            }
            else {
                return super.contains(x,y);
            }
        }

        public void componentAdded(ContainerEvent e) {
            Container c = e.getContainer().getParent();
            if (c != null) {
                c.getParent().validate();
                c.getParent().repaint();
            }
        }

        public void componentRemoved(ContainerEvent e) {
            Container c = e.getContainer().getParent();
            if (c != null) {
                c.getParent().validate();
                c.getParent().repaint();
            }
        }
    }

    // *******************************************************
    // ******************   Runnables  ***********************
    // *******************************************************

    /**
     * Generic EternityClientApplication runnable. This is intended to run on the
     * AWT gui event thread so as not to muck things up by doing
     * gui work off the gui thread. Accepts a EternityClientApplication and an Object
     * as arguments, which gives subtypes of this class the two
     * "must haves" needed in most runnables for this demo.
     */
    static class EternityClientApplicationRunnable implements Runnable {
        protected EternityClient EternityClientApplication;
        protected Object obj;

        public EternityClientApplicationRunnable(EternityClient EternityClientApplication, Object obj) {
            this.EternityClientApplication = EternityClientApplication;
            this.obj = obj;
        }

        public void run() {
        }
    }

    // *******************************************************
    // ********************   Actions  ***********************
    // *******************************************************

    class ActivatePopupMenuAction extends AbstractAction {
        EternityClient EternityClientApplication;
        JPopupMenu popup;
        protected ActivatePopupMenuAction(EternityClient EternityClientApplication, JPopupMenu popup) {
            super("ActivatePopupMenu");
            this.EternityClientApplication = EternityClientApplication;
            this.popup = popup;
        }

        public void actionPerformed(ActionEvent e) {
            Dimension invokerSize = getSize();
            Dimension popupSize = popup.getPreferredSize();
            popup.show(EternityClientApplication, (invokerSize.width - popupSize.width) / 2,
                    (invokerSize.height - popupSize.height) / 2);
        }
    }

    static class ExitAction extends AbstractAction {
        EternityClient EternityClientApplication;
        protected ExitAction(EternityClient EternityClientApplication) {
            super("ExitAction");
            this.EternityClientApplication = EternityClientApplication;
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    class AboutAction extends AbstractAction {
        EternityClient EternityClientApplication;
        protected AboutAction(EternityClient EternityClientApplication) {
            super("AboutAction");
            this.EternityClientApplication = EternityClientApplication;
        }

        public void actionPerformed(ActionEvent e) {
            if(aboutBox == null) {
                aboutBox = new AboutDialog(EternityClientApplication);
            }
            aboutBox.pack();
            aboutBox.setLocationRelativeTo(getFrame());
            aboutBox.setVisible(true);
        }
    }
    
    static class ModuleLoadThread extends Thread {
        EternityClient EternityClientApplication;

        ModuleLoadThread(EternityClient EternityClientApplication) {
            this.EternityClientApplication = EternityClientApplication;
        }

        public void run() {
            SwingUtilities.invokeLater(EternityClientApplication::loadModules);
        }
    }
    
}