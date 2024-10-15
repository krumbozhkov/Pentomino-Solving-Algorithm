import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.awt.Font;

public class UI extends JPanel {
    private JFrame window;
    private int[][] state;
    private int size;
    private int panelSizeX; // Width of the JPanel
    private int panelSizeY; // Height of the JPanel

    // Define BufferedImage variables for your blocks
    private BufferedImage backgroundImage;
    private final BufferedImage[] blockImages = new BufferedImage[12];

    // Custom font
    private Font customFont;
    // New JLabel for displaying elapsed time
    private JLabel elapsedTimeLabel;
    private JLabel gridDimentions;
    public String gridXSize;

    //------------------------------------------------------------------------------------
    public UI(int x, int y, int _size) {
        size = _size;

        // Load the custom font
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("Project1-1/tetrisfont.ttf"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            // Handle font loading errors here
        }
        // Store the current layout manager
        //LayoutManager savedLayoutManager = getLayout();

        setLayout(null);
        elapsedTimeLabel = new JLabel("Time: 0 ms");
        elapsedTimeLabel.setFont(customFont.deriveFont(Font.PLAIN, 35));
        elapsedTimeLabel.setForeground(Color.WHITE);
        elapsedTimeLabel.setBounds(50, 400, 300, 50); // Adjust the location and size as needed
        this.add(elapsedTimeLabel);

        gridDimentions = new JLabel("Grid size: 0");
        gridDimentions.setFont(customFont.deriveFont(Font.PLAIN, 35));
        gridDimentions.setForeground(Color.WHITE);
        gridDimentions.setBounds(50, 50, 2000, 300); // Set location and size
        this.add(gridDimentions);
        // Load the background image and calculate panel size based on its dimensions
        loadBackgroundImage();
        panelSizeX = backgroundImage.getWidth() - 600; // Set panel width to match the background image
        panelSizeY = backgroundImage.getHeight() - 300; // Set panel height to match the background image

        setPreferredSize(new Dimension(panelSizeX, panelSizeY)); // Set the panel size

        window = new JFrame("Pentomino");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.getContentPane().setPreferredSize(new Dimension(panelSizeX, panelSizeY)); // Set content pane size
        window.getContentPane().add(this);
        window.pack();
        window.setVisible(true);



        state = new int[x][y];
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[i].length; j++) {
                state[i][j] = -1;
            }
        }

        // Load block images
        loadBlockImages();
    }

    public void updateElapsedTimeLabel(long elapsedTime) {
        elapsedTimeLabel.setText("Time: " + elapsedTime + " ms");
    }
    public void gridSize (String gridXSize){
        gridDimentions.setText("Grid size: "+gridXSize);
    }
    //------------------------------------------------------------------------------------
    private void loadBackgroundImage() {
        try {
            File backgroundFile = new File("Project1-1/Background1.png");
            backgroundImage = ImageIO.read(backgroundFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------------------------------------------------------
    // Load block images in the constructor
    private void loadBlockImages() {
        try {
            for (int i = 0; i < 12; i++) {
                File blockFile = new File("Project1-1/Block" + (i + 1) + ".png");
                blockImages[i] = ImageIO.read(blockFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-------------------------------------------------------------------------------------------------
    public void setState(int[][] _state) {
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[i].length; j++) {
                state[i][j] = _state[i][j];
            }
        }
        repaint();
    }

    //------------------------------------------------------------------------------------
    public void paintComponent(Graphics g) {
        Graphics2D localGraphics2D = (Graphics2D) g;
        if (backgroundImage != null) {
            localGraphics2D.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            localGraphics2D.setColor(Color.DARK_GRAY);
            localGraphics2D.fillRect(0, 0, getWidth(), getHeight());
        }

        // Calculate the center offset
        int offsetX = (getWidth() - panelSizeX + 1100) / 2;
        int offsetY = (getHeight() - panelSizeY + 180) / 2;

        // Draw blocks
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[0].length; j++) {
                int blockID = state[i][j];
                if (blockID >= 0 && blockID < 12) {
                    BufferedImage blockImage = blockImages[blockID];
                    if (blockImage != null) {
                        // Calculate the position of the image
                        int x = i * size + offsetX;
                        int y = j * size + offsetY;

                        localGraphics2D.drawImage(blockImage, x, y, size, size, null);
                    }
                }
            }
        }

    }
}
