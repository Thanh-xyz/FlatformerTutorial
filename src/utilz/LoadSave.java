package utilz;

import main.Game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class LoadSave {

    public static final String PLAYER_ATLAS = "/player_sprites.png";
    public static final String LEVEL_ATLAS = "/outside_sprites.png";
    public static final String LEVEL_ONE_ATLAS = "/level_one_data.png";

    public static BufferedImage GetPlayerAtlas(String fileName) {
        BufferedImage image = null;
        InputStream is = LoadSave.class.getResourceAsStream(fileName);

        try {
            image = ImageIO.read(is);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return image;
    }

    //Lay du lieu ban do
    public static int[][] GetLevelData() {
        int[][] lvlData = new int[Game.GAME_HEIGHT][Game.GAME_WIDTH];
        BufferedImage img = GetPlayerAtlas(LEVEL_ONE_ATLAS);

        for (int j = 0; j < img.getHeight(); j++) {
            for (int i = 0; i < img.getWidth(); i++) {
                Color color = new Color(img.getRGB(i, j));
                int value = color.getRed();
                if (value >= 48)
                    value = 0;
                lvlData[j][i] = value;
            }
        }
        return lvlData;
    }
}
