import java.io.*;
import java.util.SortedSet;
import java.util.TreeSet;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

/*  Local Password Manager
 *  Kaleb Jacobsen
 *
 * This project is designed to take in a user-specific key word and PIN number. Using these values, plaintext is encrypted into ciphertext.
 * The intended use for this function is to take the URL of any website and create a complicated password unique to the individual user.
 *
 * This file handles the GUI and much of the functionality of the final app, but relies on Encryption.java for the actual encryption and
 * Alphabet.java for valid output characters. Also: HomeEncryptBG2.jpg, HomeEncryptExit.png and HomeEncryptExitRollover.png should be accessible
 * in order to correctly display the GUI.
 *
 * Current working version: 1.5 (Last update Oct. 27, 2014)
 * -Added static length option for exceptions to the standard password length.
 * -Fixing bug where app crashes if the plaintext is shorter than the keyword.
 * -Added default coloring to make app usable with missing background files.
 *
 * In progress: 1.6 (Jun. 1, 2015)
 * -Adding plaintext list selection
 * -Added NO SPECIAL argument (Done!)
 * -Fixed bug where removing the first item in the edit list caused a blank item to appear.
 */


class BgPanel extends JPanel { //BgPanel will be called behind EncryptionWorkspace and displays the background.
   private static final long serialVersionUID = 1L; //Do not know what serial means, but added to satisfy warnings.
   private File backgroundImageLocation = new File("HomeEncryptBG2.jpg");
   @Override
   public void paintComponent(Graphics g) {
      if (backgroundImageLocation.exists() && !backgroundImageLocation.isHidden()) {
         Image bg = new ImageIcon(backgroundImageLocation.getPath()).getImage();
         g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
      } else {
         g.setColor(Color.BLACK);
         g.fillRect(0, 0, this.getWidth(), this.getHeight()); //If the background image is not found, BG will be grey and still usable.
         g.setColor(Color.DARK_GRAY);
         g.fillRect(5,5, this.getWidth()-10,this.getHeight()-10);
      }

   }
}

class EncryptionWorkspace extends JPanel { //This is the primary class for displaying the GUI and calling Encryption commands
   private static final long serialVersionUID = 1L;
   private final String LENGTHARG = "_L:";
   private final String NOSPECIALARG = "_NSP";


   private JPanel openingPanel = new JPanel();
   private JPanel encryptionPanel = new JPanel();
   private JPanel newUserPanel = new JPanel();
   private JPanel optionsPanel = new JPanel();
   private JPanel archiveEditPanel = new JPanel();
   private JLabel userInfo = new JLabel();

   final private JTextField keyWord = new JTextField(10);
   final private JComboBox<String> PIN = new JComboBox();
   final private JTextField encryptionTextbox = new JTextField(15);
   final private JTextField sizeOptionField = new JTextField(2);
   final private JTextField newUserNameField = new JTextField(10);
   final private JTextField newUserPINField = new JTextField(10);

   private Font encryptionFont = new Font("DialogInput",Font.PLAIN,14);

   private JRadioButton clipYes = new JRadioButton("<HTML><font color = '#BBFAF7'>Yes</font></html>");
   private JRadioButton clipNo = new JRadioButton("<HTML><font color = '#BBFAF7'>No</font></html>");
   private JRadioButton symYes = new JRadioButton ("<HTML><font color = '#BBFAF7'>Yes</font></html>");
   private JRadioButton symNo = new JRadioButton ("<HTML><font color = '#BBFAF7'>No</font></html>");

   private JCheckBox staticLength = new JCheckBox ("<HTML><font color = '#BBFAF7'>Static?</font></html>");

   private JButton exitHome;
   private JButton exit;
   private JButton newUserSave;
   private JButton newUserCancel;

   private File plainTextArchive, plainTextUsers, exitButtonImage;
   private SortedSet<String> archivedText = new TreeSet<String>();
   private Map<String, Integer> Users = new HashMap<String, Integer>();

   private JComboBox<String> plainComboBox;
   private JComboBox<String> editArchive;

   private String userKeyWord = "Default";
   private String userName = "Default";
   private String saveArgText = null;
   private boolean clipboardBehavior = true;
   private boolean isFirstEncrypt = true;
   private boolean individualLength = false;
   private int userPINNumber = 1234;
   private int saveMaxSize;
   private int saveEditIndex = 0;
   private int firstArgumentIndex = 0; //Used for determining where arguments begin so only plaintext is used.

   private Encryption password = new Encryption();
   private StringBuilder secondLevelKey = new StringBuilder();

   private Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
   private Action clipKey = new ClipAction();

   @SuppressWarnings({ "unchecked", "rawtypes" })
      EncryptionWorkspace() {
         setOpaque(false); //All components in the EncryptionWorkspace should not be opaque, as there is a background I want to show through placed behind it.
         loadOptions();  //Pulls all options from a text file, does nothing if the file does not exist (default values written into constructors)
         loadArchive();  //Pulls all old text, does nothing if the file does not exist (The program will create the file later)

         plainComboBox = new JComboBox(archivedText.toArray());
         plainComboBox.setMaximumRowCount(6);

         encryptionTextbox.setFont(encryptionFont);

         openingPanel.setOpaque(false);
         openingPanel.setLayout (new GridBagLayout());
         GridBagConstraints c3 = new GridBagConstraints();
         GridBagConstraints c5 = new GridBagConstraints();
         JButton userLogin = new JButton("Login");
         userLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               checkLoginInfo();
            }
         });

         JButton newUser = new JButton("New User");
         newUser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               makeNewUser();
            }
         });

         JPanel userWord = new JPanel();
         userWord.setOpaque(false);
         userWord.setLayout(new FlowLayout());
         userWord.add (new JLabel("<HTML><font color = '#BBFAF7'>Key Word: </font></html>"));
         userWord.add(keyWord);

         JPanel userPIN = new JPanel();
         userPIN.setOpaque(false);
         userPIN.setLayout(new FlowLayout());
         userPIN.add(new JLabel("<HTML><font color = '#BBFAF7'>User: </font></html>")); //HTML can be used in strings, had trouble using tags that were divided: "<tag>" + var + "</tag>"
         userPIN.add(PIN);
         JPanel userOk = new JPanel();
         userOk.setOpaque(false);
         userOk.add(userLogin);
         userOk.add(newUser);

         exitButtonImage = new File("HomeEncryptExitRollover.PNG");
         if (exitButtonImage.exists() && !exitButtonImage.isHidden()){
            ImageIcon exitIcon = new ImageIcon(exitButtonImage.getPath());
            exitHome = new JButton("",exitIcon); //Quit program
            exitHome.setRolloverIcon(new ImageIcon("HomeEncryptExitRollover.PNG"));
            exitHome.setBorderPainted(false); //removes border
         } else {
            System.out.println("Loading Exit Image Failed!");
            exitHome = new JButton ("<HTML><font color='#BBFAF7'>Exit</font></color>");
         }
         exitHome.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               writeArchive();
               System.exit(0);
            }
         });
         exitHome.setOpaque(false);
         exitHome.setContentAreaFilled(false); //removes gray area in background

         c5.weightx = .25;
         c5.gridx = 2;
         c5.gridy = 0;
         c5.gridwidth = 1;
         c5.fill = GridBagConstraints.NONE;
         c5.anchor = GridBagConstraints.EAST; //If extra space is present, where should this anchor
         c5.insets.set(0,0,0,0);
         openingPanel.add(exitHome, c5);

         c3.gridy = 0;
         c3.gridx = 0;
         c3.insets.set(25, 0, 0, 0);
         openingPanel.add(userWord, c3);
         c3.gridy = 1;
         c3.insets.set(3,0,0,0);
         openingPanel.add(userPIN, c3);
         c3.gridy = 2;
         openingPanel.add(userOk, c3);

         add(openingPanel);

         encryptionPanel.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints(); //Constraints are used to adjust items added to GridBagLayout, see below
         encryptionPanel.setOpaque(false);
         encryptionPanel.setPreferredSize(new Dimension(300,170));

         newUserPanel.setLayout(new GridBagLayout());
         GridBagConstraints newUserC = new GridBagConstraints();
         newUserPanel.setOpaque(false);
         newUserPanel.setPreferredSize(new Dimension(300, 170));


         newUserCancel = new JButton("Cancel");
         newUserCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               SwitchScreens(newUserPanel, openingPanel);
            }
         });
         newUserSave = new JButton("Save");
         newUserSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Users.put(newUserNameField.getText(), Integer.parseInt(newUserPINField.getText()));
               updateUsers();
               SwitchScreens(newUserPanel, openingPanel);
            }
         });

         newUserC.gridy = 0;
         newUserPanel.add(new JLabel("<HTML><font color = '#BBFAF7'>Username: </font></html>"), newUserC);
         newUserPanel.add(newUserNameField, newUserC);
         newUserC.gridy = 1;
         newUserPanel.add(new JLabel("<HTML><font color = '#BBFAF7'>PIN: </font></html>"), newUserC);
         newUserPanel.add(newUserPINField, newUserC);
         newUserC.gridy = 2;
         newUserPanel.add(newUserCancel, newUserC);
         newUserPanel.add(newUserSave, newUserC);

         plainComboBox.setEditable(true); //Allows new entries to be added.
         plainComboBox.addActionListener(new ActionListener() { //Placing the listener in the ComboBox triggers the listener both when ENTER is pressed (while box is focused) and when a new item is selected. (Also applies to any setSelected calls)
            public void actionPerformed(ActionEvent e) {
               plainComboBox.setSelectedItem(password.removeHTTP((String)plainComboBox.getSelectedItem()));

               /*What's going on below is new and takes some explaining.
                * If there is a static size found in the plaintext in the form of _L:XX, that needs to be the size used for encryption.
                *
                * So, if there is a static size:
                * => save the current size in a variable
                * => change the max size to the individual size
                * => check that the plain text (without the length argument) is shorter than the individual size.
                * => Encrypt the plaintext (Without the length argument)
                * => put the length argument back onto the plaintext string
                * => change the max size back to the size saved in a variable before.
                *
                * If there is not a static size:
                * => check that the plaintext is shorter than the max length
                * => encrypt it
                * => If the Static Length box is checked:
                *      => Add the length argument to the end of the plaintext.
                *
                * Note: The code looks for _L:xx for the length argument, only double digit numbers will work correctly.
                * This is reflected in the error messages in the save method.
                */

               //Edited to work for either the length argument or the no special character argument.
               //Above description is relevant, but not complete.

               if(((String)plainComboBox.getSelectedItem()).contains(LENGTHARG) || ((String)plainComboBox.getSelectedItem()).contains(NOSPECIALARG)) {

                  saveMaxSize = password.getMaxSize(); //Regardless of the action below, remember the max size.

                  if (((String)plainComboBox.getSelectedItem()).contains(LENGTHARG) && ((String)plainComboBox.getSelectedItem()).contains(NOSPECIALARG)) {
                     if (((String)plainComboBox.getSelectedItem()).indexOf(LENGTHARG) > ((String)plainComboBox.getSelectedItem()).indexOf(NOSPECIALARG)) {
                        //Finds the end of the plaintext and begining of the arguments. If more arguments are added later this will need more than
                        //two options.
                        firstArgumentIndex = ((String)plainComboBox.getSelectedItem()).indexOf(NOSPECIALARG);
                     } else {
                        firstArgumentIndex = ((String)plainComboBox.getSelectedItem()).indexOf(LENGTHARG);
                     }
                  } else if (((String)plainComboBox.getSelectedItem()).contains(LENGTHARG)) {
                     firstArgumentIndex = ((String)plainComboBox.getSelectedItem()).indexOf(LENGTHARG);
                  } else {
                     firstArgumentIndex = ((String)plainComboBox.getSelectedItem()).indexOf(NOSPECIALARG);
                  }
                  saveArgText = ((String)plainComboBox.getSelectedItem()).substring(firstArgumentIndex);

                  if (((String)plainComboBox.getSelectedItem()).contains(LENGTHARG)){
                     password.setMaxSize(Integer.parseInt((((String)plainComboBox.getSelectedItem()).substring(((String)plainComboBox.getSelectedItem()).indexOf(LENGTHARG)+3, ((String)plainComboBox.getSelectedItem()).indexOf(LENGTHARG)+5))));
                     if (password.getSymbolStatus() == false && !((String)plainComboBox.getSelectedItem()).contains(NOSPECIALARG)) {
                        saveArgText = saveArgText + NOSPECIALARG;
                     }
                  }
                  if (((String)plainComboBox.getSelectedItem()).contains(NOSPECIALARG)) {
                     password.requireSymbol(false);
                     if (individualLength && !((String)plainComboBox.getSelectedItem()).contains(LENGTHARG)) {
                        saveArgText = saveArgText + LENGTHARG + password.getMaxSize();
                     }

                  }

                  plainComboBox.setSelectedItem(((String)plainComboBox.getSelectedItem()).substring(0,firstArgumentIndex));
                  if (((String)plainComboBox.getSelectedItem()).length() > password.getMaxSize()) { //If longer than allowed, shorten the string. (HTTP removed before counting)
                     plainComboBox.setSelectedItem(((String)plainComboBox.getSelectedItem()).substring(0,password.getMaxSize()));
                  }

                  encryptionTextbox.setText(password.getEncryption((String)plainComboBox.getSelectedItem()));

                  password.requireSymbol(true);
                  plainComboBox.setSelectedItem(((String)plainComboBox.getSelectedItem()) + saveArgText);
                  password.setMaxSize(saveMaxSize);

               } else {
                  if (((String)plainComboBox.getSelectedItem()).length() > password.getMaxSize()) { //If longer than allowed, shorten the string. (HTTP removed before counting)
                     plainComboBox.setSelectedItem(((String)plainComboBox.getSelectedItem()).substring(0,password.getMaxSize()));
                  }

                  encryptionTextbox.setText(password.getEncryption((String)plainComboBox.getSelectedItem()));

                  if (individualLength) {
                     if (password.getMaxSize() < 10) {
                        plainComboBox.setSelectedItem(((String)plainComboBox.getSelectedItem()) + LENGTHARG + "0" + password.getMaxSize());
                     } else {
                        plainComboBox.setSelectedItem(((String)plainComboBox.getSelectedItem()) + LENGTHARG + password.getMaxSize());
                     }
                  }

                  if (password.getSymbolStatus() == false) {
                     plainComboBox.setSelectedItem(((String)plainComboBox.getSelectedItem()) + NOSPECIALARG);
                  }
               }

               if (!archivedText.contains(plainComboBox.getSelectedItem())) { //If not in the archive: add to the archive, ComboBox list and list of items that can be deleted.
                  archivedText.add((String)plainComboBox.getSelectedItem());
                  editArchive.addItem((String)plainComboBox.getSelectedItem());
                  plainComboBox.addItem((String)plainComboBox.getSelectedItem());
               }

               if (clipboardBehavior) { //If allowed to access the clipboard, place the text there
                  clip.setContents(new StringSelection(encryptionTextbox.getText()), null); //StringSlection converts Strings to Transferable data
               }

               if (isFirstEncrypt) {
                  userInfo.setText("****** " + Integer.toString(userPINNumber));
                  isFirstEncrypt = false;
               }
            }
         });

         JButton grabClipboardText = new JButton("Clipboard");
         grabClipboardText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               loadFromClipboard();
            }
         });
         //Below maps the insert key to the action "clipAction". The addition to the ActionMap then tells the button what
         //"clipAction" should do. clipKey has the same action as clicking the button, but the button's default actionlistener
         //will not actually trigger.
         grabClipboardText.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("INSERT"), "clipAction");
         grabClipboardText.getActionMap().put("clipAction", clipKey);

         JButton options = new JButton("Options");
         options.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               remove(encryptionPanel);
               add(optionsPanel);

               repaint();
               revalidate();
               sizeOptionField.setText(String.valueOf(password.getMaxSize()));

               staticLength.setSelected(individualLength);

               clipYes.setSelected(clipboardBehavior); //Setting radio buttons based on loaded options
               clipNo.setSelected(!clipboardBehavior);

               symYes.setSelected(password.getSymbolStatus());
               symNo.setSelected(!password.getSymbolStatus());
            }
         });

         JPanel userInfoHolder = new JPanel();
         userInfoHolder.setOpaque(false);
         userInfoHolder.setLayout(new BorderLayout());
         userInfoHolder.add(userInfo, BorderLayout.CENTER);

         JPanel plainText = new JPanel();
         plainText.setOpaque(false);
         plainText.setLayout(new GridLayout(2,1));
         plainText.add (new JLabel("<HTML><font color = '#BBFAF7'>Plain Text: </font></html>"));
         plainText.add(plainComboBox);

         exitButtonImage = new File("HomeEncryptExitRollover.PNG");
         if (exitButtonImage.exists() && !exitButtonImage.isHidden()){
            ImageIcon exitIcon = new ImageIcon(exitButtonImage.getPath());
            exit = new JButton("",exitIcon); //Quit program
            exit.setRolloverIcon(new ImageIcon("HomeEncryptExitRollover.PNG"));
            exit.setBorderPainted(false); //removes border
         } else {
            System.out.println("Loading Exit Image Failed!");
            exit = new JButton ("<HTML><font color='#BBFAF7'>Exit</font></color>");
         }
         exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               writeArchive();
               System.exit(0);
            }
         });
         exit.setOpaque(false);
         exit.setContentAreaFilled(false); //removes gray area in background

         JPanel encryptResult = new JPanel();
         encryptResult.setOpaque(false);
         encryptResult.setLayout(new GridLayout(2,1));
         encryptResult.add (new JLabel("<HTML><font color = '#BBFAF7'>Result: </font></html>"));
         encryptResult.add(encryptionTextbox);

         int spacingTest = 40;
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0; //X position, chosen relative to other components. (I.E. Nothing will be skipped if 0-2 is not used and the grid starts at 3, but 4 will go next to it)
         c.gridy = 0; //Y position, same
         c.gridwidth = 5; //Units on grid this should occupy. Had some trouble making this work right, not sure why.
         c.weightx = .25;
         c.insets.set(0,spacingTest,0,0);
         encryptionPanel.add(userInfoHolder, c); //Always add the component and the constraints
         c.weightx = .25;
         c.gridx = 5;
         c.gridwidth = 1;
         c.fill = GridBagConstraints.NONE;
         c.anchor = GridBagConstraints.EAST; //If extra space is present, where should this anchor
         c.insets.set(0,0,0,0);
         encryptionPanel.add(exit, c);
         c.anchor = GridBagConstraints.CENTER;
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 1;
         c.gridx = 0;
         c.gridwidth = 6;
         c.gridy = 1;
         c.insets.set(0,spacingTest,0,spacingTest);
         encryptionPanel.add(plainText, c);
         c.gridy = 2;
         encryptionPanel.add(encryptResult, c);
         c.gridy = 3;
         c.gridwidth = 3;
         c.insets.set(0,spacingTest,20,0);
         encryptionPanel.add(grabClipboardText, c);
         c.gridx = 3;
         c.insets.set(0,0,20,spacingTest);
         encryptionPanel.add(options, c);

         optionsPanel.setOpaque(false);
         optionsPanel.setLayout(new GridBagLayout());
         GridBagConstraints c4 = new GridBagConstraints();

         JPanel maxSizeOption = new JPanel();
         maxSizeOption.setOpaque(false);
         maxSizeOption.add(new JLabel("<HTML><font color = '#BBFAF7'>Max Character #</font></html>"));
         maxSizeOption.add(sizeOptionField);
         staticLength.setOpaque(false);
         staticLength.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               individualLength = staticLength.isSelected();
            }
         });
         maxSizeOption.add(staticLength);

         JPanel clipboardOption = new JPanel();
         clipboardOption.setOpaque(false);
         clipboardOption.add(new JLabel("<HTML><font color = '#BBFAF7'>Result to clipboard?</font></html>"));
         ButtonGroup clipButtons = new ButtonGroup(); //Buttons must be added to the panel AND buttonGroup. Only one option in a buttonGroup can be active
         clipYes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               clipboardBehavior = true;
            }
         });
         clipYes.setOpaque(false);
         clipNo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               clipboardBehavior = false;
            }
         });
         clipNo.setOpaque(false);
         clipButtons.add(clipYes);
         clipButtons.add(clipNo);
         clipboardOption.add(clipYes);
         clipboardOption.add(clipNo);

         JPanel symbolOption = new JPanel();
         symbolOption.setOpaque(false);
         symbolOption.add(new JLabel("<HTML><font color = '#BBFAF7'>Require a symbol?</font></html>"));
         ButtonGroup symButtons = new ButtonGroup();
         symYes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               password.requireSymbol(true);
            }
         });
         symYes.setOpaque(false);
         symNo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               password.requireSymbol(false);
            }
         });
         symNo.setOpaque(false);
         symButtons.add(symYes);
         symButtons.add(symNo);
         symbolOption.add(symYes);
         symbolOption.add(symNo);

         JPanel optionsButtons = new JPanel();
         optionsButtons.setOpaque(false);

         JButton saveOptions = new JButton ("Save");
         saveOptions.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               if (sizeOptionField.getText().matches("^[0-9]{0,99}$")) { //Regex, must be a digit
                  if (Integer.parseInt(sizeOptionField.getText()) < 99) {
                     if(Integer.parseInt(sizeOptionField.getText()) > 5) {
                        password.setMaxSize(Integer.parseInt(sizeOptionField.getText()));
                        writeOptions();
                        remove(optionsPanel);
                        add(encryptionPanel);

                        writeArchive();

                        repaint();
                        revalidate();
                     } else {
                        JOptionPane.showMessageDialog(optionsPanel, "Error: passwords must be 6 characters or more");
                     }
                  } else {
                     JOptionPane.showMessageDialog(optionsPanel, "Error: passwords cannot be longer than 99 characters");
                  }
               } else {
                  JOptionPane.showMessageDialog(optionsPanel, "Error: Must enter a valid number");
               }
            }
         });

         JButton editOption = new JButton ("Edit Archive");
         editOption.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               remove(optionsPanel);
               add(archiveEditPanel);

               repaint();
               revalidate();
            }
         });

         optionsButtons.add(saveOptions);
         optionsButtons.add(editOption);

         c4.insets.set(10,0,0,0);
         c4.gridy = 0;
         optionsPanel.add(maxSizeOption, c4);
         c4.insets.set(0,0,0,0);
         c4.gridy = 1;
         optionsPanel.add(clipboardOption, c4);
         c4.gridy = 2;
         optionsPanel.add(symbolOption, c4);
         c4.gridy = 3;
         optionsPanel.add (optionsButtons, c4);
         c4.gridy = 4;

         archiveEditPanel.setOpaque(false);
         archiveEditPanel.setLayout(new GridBagLayout());
         GridBagConstraints c2 = new GridBagConstraints(); //Could have used the existing C, but wanted to keep both separate

         JButton backToOptions = new JButton("Back");
         backToOptions.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               remove(archiveEditPanel);
               add(optionsPanel);

               repaint();
               revalidate();
            }
         });

         editArchive = new JComboBox(archivedText.toArray());

         JButton removeSelection = new JButton ("Remove Entry");
         removeSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               if(editArchive.getItemCount() > 1) {
                  saveEditIndex = editArchive.getSelectedIndex();
                  archivedText.remove(editArchive.getSelectedItem());
                  plainComboBox.removeItem(editArchive.getSelectedItem());

                  editArchive.removeItem(editArchive.getSelectedItem());
                  if (saveEditIndex == 0) {
                     editArchive.setSelectedIndex(0);
                  } else {
                     editArchive.setSelectedIndex(saveEditIndex-1);
                  }
               } else {
                  JOptionPane.showMessageDialog(archiveEditPanel, "Error: Cannot remove last entry");
               }
            }
         });

         JPanel editButtons = new JPanel();
         editButtons.add(backToOptions);
         editButtons.add(removeSelection);
         editButtons.setOpaque(false);

         c2.gridy = 0;
         c2.insets.set(25,0,0,0); //Insets set how much of a gap is left between components. Values go Top, Left, Bottom, Right
         archiveEditPanel.add (editArchive, c2);
         c2.gridy = 1;
         c2.insets.set(10,0,0,0);
         archiveEditPanel.add(editButtons, c2);
      }

   private void loadOptions() {

      try {
         File optionFile = new File("options.txt");
         if (optionFile.exists()) {
            DataInputStream in = new DataInputStream (new FileInputStream(optionFile)); //DataInput gives useful methods, but requires a FileInput to construct
            password.setMaxSize(Integer.parseInt(in.readUTF())); //readUTF, reads in next available String
            this.clipboardBehavior = Boolean.parseBoolean(in.readUTF());
            password.requireSymbol(Boolean.parseBoolean(in.readUTF()));
            in.close();

         }
      }
      catch(IOException e) {
      }
   }

   private void writeOptions() {
      try {
         File optionFile = new File("options.txt");
         DataOutputStream out = new DataOutputStream (new FileOutputStream(optionFile)); //DataOutput overwrites a file instead of appending
         out.writeUTF(String.valueOf(password.getMaxSize())); //writeUTF writes a string
         out.writeUTF(String.valueOf(this.clipboardBehavior));
         out.writeUTF(String.valueOf(password.getSymbolStatus()));
         out.close();
      }
      catch(IOException e) {
      }

   }

   private void loadArchive() {
      try {
         plainTextArchive = new File("plainTextArchive.txt");
         plainTextUsers = new File("plainTextUsers.txt");

         if (plainTextUsers.canRead()) {
            BufferedReader in = new BufferedReader(new FileReader(plainTextUsers));
            String line = in.readLine();
            while (line != null) {
               String lineSplit[] = line.split("\\\\s");
               Users.put(lineSplit[0], Integer.parseInt(lineSplit[1]));
               PIN.addItem(lineSplit[0]);
               line = in.readLine();
               // TODO: Make sure if the file is corrupted it doesn't cause a crash.
            }
         } else {
            FileWriter fstream = new FileWriter("plainTextUsers.txt");
            fstream.write("");
         }


         DataInputStream in = new DataInputStream(new FileInputStream(plainTextArchive));

         while (in.available() > 0) { //If there is still more, read in the next String.
            archivedText.add(in.readUTF());
         }
         in.close();
      }
      catch (IOException e) {
      }
   }
   //Duplicate for multiple files support
   private void loadArchive(String fileName) {
      try {
         plainTextArchive = new File(fileName + ".txt");
         DataInputStream in = new DataInputStream(new FileInputStream(plainTextArchive));

         while (in.available() > 0) { //If there is still more, read in the next String.
            archivedText.add(in.readUTF());
         }
         in.close();
      }
      catch (IOException e) {
      }
   }

   private void updateUsers() {
      PIN.removeAllItems();
      for (String user : Users.keySet()) {
         PIN.addItem(user);
      }
   }

   private void writeArchive() {
      try {
         plainTextArchive = new File("plainTextArchive.txt");
         DataOutputStream out = new DataOutputStream(new FileOutputStream(plainTextArchive));
         String[] textToArchive = archivedText.toArray(new String[0]);
         for (int i = 0; i< textToArchive.length; i++) {
            out.writeUTF(textToArchive[i]);
         }

         out.close();
      } catch (IOException e) {
      }
   }

   //Duplicate for multiple files support
   private void writeArchive(String fileName) {
      try {
         plainTextArchive = new File(fileName + ".txt");
         DataOutputStream out = new DataOutputStream(new FileOutputStream(plainTextArchive));
         String[] textToArchive = archivedText.toArray(new String[0]);
         for (int i = 0; i< textToArchive.length; i++) {
            out.writeUTF(textToArchive[i]);
         }

         out.close();
      } catch (IOException e) {
      }
   }

   private void loadFromClipboard() {
      try {
         plainComboBox.setSelectedItem(clip.getData(DataFlavor.stringFlavor)); //getData reads from clip, requires a type of Flavor.
      } catch (UnsupportedFlavorException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private class ClipAction extends AbstractAction {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent ae) {
         loadFromClipboard();
      }
   }

   private void checkLoginInfo() {
      userKeyWord = keyWord.getText();
      // TODO: Make Dynamic
      userPINNumber = Users.get(PIN.getSelectedItem().toString());
      if (userKeyWord.length() > password.getMaxSize()) {
         userKeyWord = userKeyWord.substring(0,password.getMaxSize()); //Key should not be longer than password.
      }
      if (true) { // TODO userPINText.matches("^[0-9]{1,4}$")) { //Regex: string contains only chars 0-9 and is 1 to 4 chars long
         userInfo.setForeground(new Color(247, 233, 8)); //Same color as #BBFAF7, converted on the Internet to RGB
         userInfo.setText(userKeyWord + " " + Integer.toString(userPINNumber));
         remove(openingPanel);
         add(encryptionPanel);

         repaint(); //Both commands are needed any time I remove and add during runtime. Forces objects to display correctly.
         revalidate();

         password.setKey(userKeyWord); //Key not set in password's constructor, first time userKeyWord is passed.
         password.setUserPIN(userPINNumber);
      } else {
         JOptionPane.showMessageDialog(openingPanel, "The PIN must be a number shorter than 5 digits");
      }
   }

   private void makeNewUser() {
      System.out.println("Making new User!! (But not actually cause there's no code here yet)");
      SwitchScreens(openingPanel, newUserPanel);
   }

   
  /* //Integrating into Encryption.java
   private void runDblEncryption() {
      Two levels of encryption happen here. First the plain text with the users key word. Then the plaintext with
       * the previous result set as the new key. The key is then reset to the user key.
       
      secondLevelKey.replace(0, secondLevelKey.length(), password.getEncryption((String)plainComboBox.getSelectedItem()));
      password.setKey(secondLevelKey.toString());
      password.clear();
      encryptionTextbox.setText(password.getEncryption((String)plainComboBox.getSelectedItem()));
      password.setKey(userKeyWord);
      password.clear();
   }
*/
   private void SwitchScreens(JPanel oldScreen, JPanel newScreen) {
      remove(oldScreen);
      add(newScreen);
      repaint();
      revalidate();
   }
}

class EncrypttionHandling extends JFrame{ //This class is really just used to create and display the GUI.

   static Point mouseCoords;
   private static final long serialVersionUID = 1L;

   public static void main (String args[]) {

      JPanel bgPanel = new BgPanel(); //Background panel, this is the reason everything else was not Opaque
      bgPanel.setLayout(new BorderLayout());
      bgPanel.add(new EncryptionWorkspace(), BorderLayout.CENTER);


      final EncrypttionHandling t = new EncrypttionHandling();
      t.setContentPane(bgPanel);
      t.setDefaultCloseOperation(EXIT_ON_CLOSE);
      t.setBounds(200, 200, 300, 170);
      t.setUndecorated(true); //Removes default frame, including minimize, maximize and exit buttons
      t.setVisible(true);

      //Manual click and drag added to background below
      EncrypttionHandling.mouseCoords = null;
      t.addMouseListener(new MouseListener() {
         @Override
         public void mouseClicked(MouseEvent arg0) {
         }
         @Override
         public void mouseEntered(MouseEvent arg0) {
         }
         @Override
         public void mouseExited(MouseEvent arg0) {
         }
         @Override
         public void mousePressed(MouseEvent arg0) {
            mouseCoords = arg0.getPoint();
         }
         @Override
         public void mouseReleased(MouseEvent arg0) {
            mouseCoords = null;
         }
      });

      t.addMouseMotionListener(new MouseMotionListener() {
         @Override
         public void mouseDragged(MouseEvent arg0) {
            Point currCoords = arg0.getLocationOnScreen();
            t.setLocation(currCoords.x - mouseCoords.x, currCoords.y - mouseCoords.y);
         }
         @Override
         public void mouseMoved(MouseEvent arg0) {
         }

      });
   }
}
