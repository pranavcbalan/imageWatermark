package in.pranavc.imageWatermark;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by pranav on 21/3/17.
 */
public class WaterMark {

    private static File watermaerkImage, imagesPath;
    private static JProgressBar progressBar;
    private static JLabel count;
    private static Thread t;


    public static void main(String[] args) {
        JPanel groupLayoutPanel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(groupLayoutPanel);
        groupLayout.setAutoCreateGaps(true);
        groupLayoutPanel.setLayout(groupLayout);

        GroupLayout.Group hg1 = groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        GroupLayout.Group hg2 = groupLayout.createParallelGroup();

        GroupLayout.Group vg1 = groupLayout.createParallelGroup();
        GroupLayout.Group vg2 = groupLayout.createParallelGroup();
        GroupLayout.Group vg3 = groupLayout.createParallelGroup();
        GroupLayout.Group vg4 = groupLayout.createParallelGroup();
        GroupLayout.Group vg5 = groupLayout.createParallelGroup();

        JLabel nameLabel = new JLabel("Choose watermark image : ");
        hg1.addComponent(nameLabel);
        vg1.addComponent(nameLabel);


        JLabel waterMArkPath = new JLabel("Image : ");
        final JLabel waterMarkPathLabel = new JLabel("Not selected");
        hg1.addComponent(waterMArkPath);
        hg2.addComponent(waterMarkPathLabel, 300, 300, 300);
        vg3.addComponent(waterMArkPath);
        vg3.addComponent(waterMarkPathLabel);


        JButton button = new JButton("Choose");
        hg2.addComponent(button);
        vg1.addComponent(button);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                watermaerkImage = chooseFileOrFolder("Choose watermark image", false);
                waterMarkPathLabel.setText(watermaerkImage != null ? watermaerkImage.getAbsolutePath() : "");
            }
        });

        JLabel descriptionLabel = new JLabel("Choose image path : ");
        hg1.addComponent(descriptionLabel);
        vg2.addComponent(descriptionLabel);

        JButton button1 = new JButton("Choose");
        hg2.addComponent(button1);
        vg2.addComponent(button1);

        JLabel imagepath = new JLabel("Path : ");
        final JLabel imagepathLabel = new JLabel("Not selected");
        hg1.addComponent(imagepath);
        hg2.addComponent(imagepathLabel, 300, 300, 300);
        vg4.addComponent(imagepath);
        vg4.addComponent(imagepathLabel);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imagesPath = chooseFileOrFolder("Choose input image path", true);
                imagepathLabel.setText(imagesPath != null ? imagesPath.getAbsolutePath() : "");
            }
        });


        JSpinner model = new JSpinner(new SpinnerNumberModel(0.95, 0.01, 1, 0.01));
        JLabel modelLabel = new JLabel("Quality : ");

        JButton jButton = new JButton("Convert");

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (t != null && t.isAlive()) t.interrupt();
                if (watermaerkImage != null && imagepath != null)
                    test(watermaerkImage, imagesPath);
            }
        });


        hg1.addComponent(modelLabel);
        hg2.addComponent(model, 100, 100, 100);

        vg5.addComponent(modelLabel);
        vg5.addComponent(model);


        // Horizontal group
        GroupLayout.SequentialGroup hseq1 = groupLayout.createSequentialGroup();
        hseq1.addGroup(hg1);
        hseq1.addGroup(hg2);

        // Vertical group
        GroupLayout.SequentialGroup vseq1 = groupLayout.createSequentialGroup();
        vseq1.addGroup(vg1);
        vseq1.addGroup(vg3);
        vseq1.addGroup(vg2);
        vseq1.addGroup(vg4);
        vseq1.addGroup(vg5);


        progressBar = new JProgressBar();
        JLabel status = new JLabel("Status : ");
        count = new JLabel("0/0");


        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addGroup(hseq1)
                        .addComponent(jButton)
                        .addGroup(
                                groupLayout.createSequentialGroup()
                                        .addGroup(groupLayout.createParallelGroup().addComponent(status))
                                        .addGroup(groupLayout.createParallelGroup().addComponent(progressBar))
                                        .addGroup(groupLayout.createParallelGroup().addComponent(count))
                        )
        );
        groupLayout.setVerticalGroup(
                groupLayout.createSequentialGroup().addGroup(vseq1)
                        .addGroup(groupLayout.createParallelGroup().addComponent(jButton))
                        .addGroup(
                                groupLayout.createParallelGroup()
                                        .addComponent(status)
                                        .addComponent(progressBar)
                                        .addComponent(count)
                        )
        );


//        groupLayout.setHorizontalGroup(hseq2);
//        groupLayout.setVerticalGroup(vseq2);

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        contentPane.add(groupLayoutPanel);

        JFrame frame = new JFrame("Image watermarking");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(contentPane);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (t != null && t.isAlive()) t.interrupt();
                System.exit(0);
            }
        });
    }


    public static void test(File water, File path) {
        File out = new File(path, "out");
        out.mkdir();
        int totalCount = 0;

        for (File f : path.listFiles()) {
            if (isImage(f)) {
                totalCount++;
            }
        }

        count.setText("0/" + totalCount);
        progressBar.setMaximum(totalCount);

        final int finalTotalCount = totalCount;
        t = new Thread(new Runnable() {
            @Override
            public void run() {


                int progressCount = 0;

                for (File f : path.listFiles()) {
                    if (isImage(f)) {
                        addImageWatermark(water, f, new File(out, f.getName()));
                        count.setText(progressCount + "/" + finalTotalCount);
                        change(++progressCount);
                    }
                }

            }
        });

        t.start();

    }


    public static void change(int aantal) {
        if (aantal < progressBar.getMaximum() && aantal >= 0) {
            progressBar.setValue(aantal);
            progressBar.updateUI();
        }
    }

    static void addImageWatermark(File watermarkImageFile, File sourceImageFile, File destImageFile) {
        try {
            BufferedImage sourceImage = ImageIO.read(sourceImageFile);
            BufferedImage watermarkImage = ImageIO.read(watermarkImageFile);

            // initializes necessary graphic properties
            Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
            g2d.setComposite(alphaChannel);

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            int width = sourceImage.getWidth() / 10;
            int height = watermarkImage.getHeight() * width / watermarkImage.getWidth();
            g2d.drawImage(watermarkImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), sourceImage.getWidth() - width, sourceImage.getHeight() - height, width, height, null);


//            ImageIO.write(sourceImage, "png", destImageFile1);


            ImageWriter writer = null;
            FileImageOutputStream output = null;
            try {
                writer = ImageIO.getImageWritersByFormatName("jpg").next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.95f);
                output = new FileImageOutputStream(destImageFile);
                writer.setOutput(output);
                IIOImage iioImage = new IIOImage(sourceImage, null, null);
                writer.write(null, iioImage, param);
            } catch (IOException ex) {
                throw ex;
            } finally {
                if (writer != null) {
                    writer.dispose();
                }
                if (output != null) {
                    output.close();
                }
            }

            g2d.dispose();


        } catch (IOException ex) {
            System.err.println(ex);
        }
    }


    private static boolean isImage(File f) {
        String mimetype = new MimetypesFileTypeMap().getContentType(f);
        String type = mimetype.split("/")[0];
        return type.equals("image");
    }


    private static File chooseFileOrFolder(String title, boolean folder) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle(title);
        if (!folder)
            chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "gif", "bmp"));

        chooser.setFileSelectionMode(folder ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
            return chooser.getSelectedFile();
        } else {
            System.out.println("No Selection ");
        }
        return null;
    }


}