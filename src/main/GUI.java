package main;

import util.OsuUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Properties;


@SuppressWarnings("serial")
public class GUI extends JFrame {

    private final String propertyName = "Magic Copy config.properties";
    private final String version = "v2023-10-16";
    private final JTextField textField_Source;
    private final JTextField textField_Target;
    private final String startPath = System.getProperty("user.dir");
    private JPanel contentPane;
    private String defaultPath = "C:/Program Files (x86)/osu!/Songs";
    // Variables
    private File inputFile;
    private File outputFile;
    private boolean keysound = false;
    private boolean clear = true;

    /**
     * Create the frame.
     */
    public GUI() {
        readFromProperty(startPath);
        setTitle("Magic Copy by DH " + version);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 767, 241);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lbl_Source = new JLabel("Source");
        lbl_Source.setBounds(46, 39, 56, 16);
        contentPane.add(lbl_Source);

        textField_Source = new JTextField();
        textField_Source.setEditable(false);
        textField_Source.setBounds(121, 36, 483, 22);
        contentPane.add(textField_Source);
        textField_Source.setColumns(10);

        JButton btnBrowse_Source = new JButton("Browse");
        btnBrowse_Source.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                File f = getOsuFile();
                if (f != null) {
                    inputFile = f;
                    textField_Source.setText(inputFile.getAbsolutePath());
                    defaultPath = inputFile.getParent();
                }
            }
        });
        btnBrowse_Source.setBounds(616, 35, 97, 25);
        contentPane.add(btnBrowse_Source);

        JLabel lbl_Target = new JLabel("Target");
        lbl_Target.setBounds(46, 89, 56, 16);
        contentPane.add(lbl_Target);

        textField_Target = new JTextField();
        textField_Target.setEditable(false);
        textField_Target.setColumns(10);
        textField_Target.setBounds(121, 86, 483, 22);
        contentPane.add(textField_Target);

        JButton btnBrowse_Target = new JButton("Browse");
        btnBrowse_Target.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                File f = getOsuFile();
                if (f != null) {
                    outputFile = f;
                    textField_Target.setText(outputFile.getAbsolutePath());
                }
            }
        });
        btnBrowse_Target.setBounds(616, 85, 97, 25);
        contentPane.add(btnBrowse_Target);

        JButton btnCopy = new JButton("Copy");
        btnCopy.setFont(new Font("Tahoma", Font.BOLD, 24));
        btnCopy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (inputFile.equals(outputFile)) {
                    JOptionPane.showMessageDialog(null, "Source can't be same as Target!");
                } else if (inputFile == null) {
                    JOptionPane.showMessageDialog(null, "Please select a Source file!");
                } else if (outputFile == null) {
                    JOptionPane.showMessageDialog(null, "Please select a Target file!");
                } else {
                    copyHS();
                }
            }
        });
        btnCopy.setBounds(233, 119, 244, 71);
        contentPane.add(btnCopy);

        JCheckBox chckbxKeysound = new JCheckBox("Keysound");
        chckbxKeysound.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                keysound = chckbxKeysound.isSelected();
            }
        });
        chckbxKeysound.setSelected(keysound);
        chckbxKeysound.setBounds(46, 117, 113, 25);
        contentPane.add(chckbxKeysound);
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GUI frame = new GUI();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void copyHS() {
        try {
            int mode = OsuUtils.getMode(inputFile);
            writeToProperty(startPath);
            if (mode == 3) {
                MagicCopyMania mc = new MagicCopyMania(inputFile, outputFile, keysound, clear);
                mc.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void readFromProperty(String path) {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            String propertyPath = path + "\\" + propertyName;
            File f = new File(propertyPath);
            if (f.exists() && f.isFile()) {
                input = new FileInputStream(propertyPath);
                prop.load(input);
                if (prop.getProperty("Path") != null) {
                    defaultPath = prop.getProperty("Path");
                }
                if (prop.getProperty("keysound") != null) {
                    keysound = prop.getProperty("keysound").equalsIgnoreCase("true");
                }

                input.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeToProperty(String path) {
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            String propertyPath = path + "\\" + propertyName;
            File f = new File(propertyPath);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileInputStream input = new FileInputStream(propertyPath);
            prop.load(input);
            prop.setProperty("Path", defaultPath);
            String ks;
            if (keysound) {
                ks = "true";
            } else {
                ks = "false";
            }
            prop.setProperty("keysound", ks);
            input.close();
            // save properties to project root folder
            output = new FileOutputStream(propertyPath);
            prop.store(output, null);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getOsuFile() {
        File f = null;
        FileFilter filter = new FileNameExtensionFilter("OSU file", "osu");
        final JFileChooser jFileChooser1 = new javax.swing.JFileChooser(defaultPath);
        jFileChooser1.addChoosableFileFilter(filter);
        jFileChooser1.setFileFilter(filter);
        // Open details
        Action details = jFileChooser1.getActionMap().get("viewTypeDetails");
        details.actionPerformed(null);
        int returnVal = jFileChooser1.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            f = jFileChooser1.getSelectedFile();
        }
        return f;
    }
}
