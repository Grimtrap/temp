import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GraphicsSetCreator extends JFrame{
    private static final String FILE_NAME_TAKEN_MSG = "This file name is already taken. Try another one.";
    private static final String FILE_IO_EXCEPTION_MSG = "There was an error creating the file.";
    private static final String IMG_FILE_IO_EXCEPTION_MSG = "There was an error moving an image file.";
    private static final String FILE_WRITING_EXCEPTION_MSG = "There was an error writing to the file.";
    private static final String GRAPHICS_SET_CREATED_MSG = "Your graphics set was created.";
    private static final String DIRECTORY_CREATION_ERR_MSG = "There was a problem creating the directory.";

    private JPanel mainPanel;
    private JButton createDirectoryButton;
    private MouthShapePanel mouthShapePanel;
    private JPanel avatarPanel;
    private JTextField avatarTextField;
    private JButton chooseAvatarButton;
    private JTextField graphicsSetNameTextField;
    private JButton mouthShapeAddButton;
    private JTextArea mouthShapePhoneticDisplayTextArea;
    private JTextField mouthShapePhoneticTextField;
    private JTextField mouthShapeFileNameTextField;

    private OratioLinkedList<MouthShape> mList;
    private File directory;
    private String graphicsSetName;

    // Constructor
    private GraphicsSetCreator() {
        super("Graphics Set Creator");

        this.mList = new OratioLinkedList<>();

        this.add(this.mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.pack();

        graphicsSetName = JOptionPane.showInputDialog("What would you like to name your graphics set?");

        this.directory = new File("resources\\Graphics\\" + graphicsSetName);

        createDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!directory.mkdirs()) {
                    JOptionPane.showMessageDialog(mainPanel, DIRECTORY_CREATION_ERR_MSG);
                    return;
                }

                String[] fileNames = mouthShapePanel.getMouthShapeTextArea().getText().split("\n+");

                for (int i = 0; i < fileNames.length; i++) {
                    if (!mList.get(i).getFileName().equals(fileNames[i])) {
                        mList.remove(i);
                        i--;
                    }
                }

                for (int i = 0; i < mList.size(); i++) {
                    MouthShape m = mList.get(i);

                    File mFile = new File(m.getFileName());
                    File newLocation = new File(directory, mFile.getName());

                    try {
                        Files.copy(mFile.toPath().toAbsolutePath(), newLocation.toPath().toAbsolutePath());
                    } catch (IOException exception) {
                        JOptionPane.showMessageDialog(mainPanel, IMG_FILE_IO_EXCEPTION_MSG);
                        return;
                    }

                    m.setFileName(newLocation.getPath());
                }

                File avatarImg = new File(avatarTextField.getText().trim());
                File dest = new File(directory, avatarImg.getName());
                try {
                    Files.copy(avatarImg.toPath().toAbsolutePath(), dest.toPath().toAbsolutePath());
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(mainPanel, IMG_FILE_IO_EXCEPTION_MSG);
                    return;
                }

                createMetadataJson(dest, directory);

                JOptionPane.showMessageDialog(mainPanel, GRAPHICS_SET_CREATED_MSG);
                System.exit(0);
            }
        });
        chooseAvatarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "Image files",
                        "jpg", "jpeg", "png", "bmp", "gif"
                );
                fileChooser.setFileFilter(filter);

                int returnState = fileChooser.showOpenDialog(mainPanel);

                if (returnState == JFileChooser.APPROVE_OPTION) {
                    avatarTextField.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
    }

    public static void main(String[] args) {
        new GraphicsSetCreator();
    }

    private void createMetadataJson(File avatar, File directory) {
        File metadata = new File(directory.getPath() + "\\meta.json");

        try {
            if (!metadata.createNewFile()) {
                JOptionPane.showMessageDialog(mainPanel, FILE_NAME_TAKEN_MSG);
            }
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(mainPanel, FILE_IO_EXCEPTION_MSG);
            return;
        } catch (SecurityException exception) {
            System.err.println("this is a SecurityException and i don't know what it is");
            System.exit(1);
        }

        try {
            new JsonGenerator().generateJSON(mList, avatar.getAbsolutePath(), metadata);
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(mainPanel, FILE_WRITING_EXCEPTION_MSG);
        }
    }

    private void createUIComponents() {
        mouthShapePanel = new MouthShapePanel();
    }

    private class MouthShapePanel {
        private JPanel mainPanel;
        private JTextField fileNameTextField;
        private JButton addButton;
        private JButton chooseFileButton;
        private JPanel filePanel;
        private JTextArea mouthShapeTextArea;
        private JTextArea phoneticTextArea;

        MouthShapePanel() {
            this.addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String fileName = fileNameTextField.getText().trim();
                    String[] phoneticArray = phoneticTextArea.getText().trim().split("\n+");

                    // move image from original location to resources root
                    File imgFile = new File(fileName);

                    MouthShape m = new MouthShape(imgFile.getAbsolutePath(), phoneticArray);

                    mList.add(m);

                    mouthShapeTextArea.append(fileNameTextField.getText() + "\n");
                    fileNameTextField.setText("");
                    phoneticTextArea.setText("");
                }
            });
            this.chooseFileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                    FileNameExtensionFilter filter = new FileNameExtensionFilter(
                            "Image files",
                            "jpg", "jpeg", "png", "bmp", "gif"
                    );
                    fileChooser.setFileFilter(filter);

                    int returnState = fileChooser.showOpenDialog(mainPanel);

                    if (returnState == JFileChooser.APPROVE_OPTION) {
                        fileNameTextField.setText(fileChooser.getSelectedFile().getPath());
                    }
                }
            });
        }

        JTextArea getMouthShapeTextArea() {
            return mouthShapeTextArea;
        }

        JPanel getMainPanel() {
            return mainPanel;
        }
    }
}
