package entities;

import gamestates.Playing;
import levels.Level;
import utilz.LoadSave;

import static utilz.Constants.EnemyConstants.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class EnemyManager {
    private Playing playing;
    private BufferedImage[][] crabbyArr, pinkStarArr;
    private ArrayList<Crabby> crabbies;
    private ArrayList<PinkStar> pinkStars;

    public EnemyManager(Playing playing) {
        this.playing = playing;
        loadEnemyImgs();
    }

    public void loadEnemies(Level level) {
        crabbies = level.getCrabs();
        pinkStars = level.getPinkStars();
    }

    public void update(int[][] lvlData, Player player) {
        boolean isAnyActive = false;
        for (Crabby crabby : crabbies)
            if (crabby.isActive()) {
                crabby.update(lvlData, player);
                isAnyActive = true;
            }
        for (PinkStar pinkStar : pinkStars)
            if (pinkStar.isActive()) {
                pinkStar.update(lvlData, playing);
                isAnyActive = true;
            }
        if (!isAnyActive)
            playing.setLevelCompleted(true);
    }

    public void draw(Graphics g, int xLvlOffset) {
        drawCrabs(g, xLvlOffset);
        drawPinkStars(g, xLvlOffset);
    }

    private void drawPinkStars(Graphics g, int xLvlOffset) {
        for (PinkStar p : pinkStars)
            if (p.isActive()) {
                g.drawImage(pinkStarArr[p.getState()][p.getAniIndex()], (int) p.getHitbox().x - xLvlOffset - PINKSTAR_DRAWOFFSET_X + p.flipX(), (int) p.getHitbox().y - PINKSTAR_DRAWOFFSET_Y, PINKSTAR_WIDTH * p.flipW(), PINKSTAR_HEIGHT, null);
            }
    }

    private void drawCrabs(Graphics g, int xLvlOffset) {
        for (Crabby c : crabbies)
            if (c.isActive()) {
                g.drawImage(crabbyArr[c.getState()][c.getAniIndex()], (int) c.getHitbox().x - xLvlOffset - CRABBY_DRAWOFFSET_X + c.flipX(), (int) c.getHitbox().y - CRABBY_DRAWOFFSET_Y, CRABBY_WIDTH * c.flipW(), CRABBY_HEIGHT, null);
//                c.drawAttackBox(g, xLvlOffset);
            }
    }

    public void checkEnemyHit(Rectangle2D.Float attackBox) {
        for (Crabby c : crabbies)
            if (c.getCurrentHealth() > 0)
                if (c.isActive())
                    if (attackBox.intersects(c.getHitbox())) {
                        c.hurt(10);
                        return;
                    }

        for (PinkStar p : pinkStars)
            if (p.getCurrentHealth() > 0)
                if (p.isActive())
                    if (attackBox.intersects(p.getHitbox())) {
                        p.hurt(10);
                        return;
                    }
    }

    private void loadEnemyImgs() {
        crabbyArr = new BufferedImage[5][9];
        BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.CRABBY_SPRITE);
        for (int i = 0; i < crabbyArr.length; i++)
            for (int j = 0; j < crabbyArr[i].length; j++)
                crabbyArr[i][j] = temp.getSubimage(j * CRABBY_WIDTH_DEFAULT, i * CRABBY_HEIGHT_DEFAULT, CRABBY_WIDTH_DEFAULT, CRABBY_HEIGHT_DEFAULT);

        pinkStarArr = new BufferedImage[5][8];
        BufferedImage tempp = LoadSave.GetSpriteAtlas(LoadSave.PINKSTAR_SPRITE);
        for (int i = 0; i < pinkStarArr.length; i++)
            for (int j = 0; j < pinkStarArr[i].length; j++)
                pinkStarArr[i][j] = tempp.getSubimage(j * PINKSTAR_WIDTH_DEFAULT, i * PINKSTAR_HEIGHT_DEFAULT, PINKSTAR_WIDTH_DEFAULT, PINKSTAR_HEIGHT_DEFAULT);

    }

    public void resetAllEnemy() {
        for (Crabby c : crabbies) {
            c.resetEnemy();
        }
        for (PinkStar p : pinkStars) {
            p.resetEnemy();
        }
    }
}
