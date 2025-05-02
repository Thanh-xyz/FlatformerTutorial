package entities;

import audio.AudioPlayer;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static utilz.Constants.ANI_SPEED;
import static utilz.Constants.Directions.*;
import static utilz.Constants.GRAVITY;
import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;

public class Player extends Entity {
    private BufferedImage[][] animations;
    private boolean moving = false, attacking = false;
    private boolean left, right, jump;
    private int[][] lvdData;
    private float xDrawOffset = 21 * Game.SCALE;
    private float yDrawOffset = 4 * Game.SCALE;

    // Jumping / Gravity
    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

    //StatusBarUI
    private BufferedImage statusBarImg;

    private int statusBarWidth = (int) (192 * Game.SCALE);
    private int statusBarHeight = (int) (58 * Game.SCALE);
    private int statusBarX = (int) (10 * Game.SCALE);
    private int statusBarY = (int) (10 * Game.SCALE);

    private int healthBarWidth = (int) (150 * Game.SCALE);
    private int healthBarHeight = (int) (4 * Game.SCALE);
    private int healthBarXStart = (int) (34 * Game.SCALE);
    private int healthBarYStart = (int) (14 * Game.SCALE);
    private int healthWidth = healthBarWidth;

    private int powerBarWidth = (int) (104 * Game.SCALE);
    private int powerBarHeight = (int) (2 * Game.SCALE);
    private int powerBarXStart = (int) (44 * Game.SCALE);
    private int powerBarYStart = (int) (34 * Game.SCALE);
    private int powerWidth = powerBarWidth;
    private int powerMaxValue = 200;
    private int powerValue = powerMaxValue;

    private int flipX = 0;
    private int flipW = 1;

    private boolean attackChecked;
    private Playing playing;

    private int tileY = 0;
    private boolean powerAttackActive;
    private int powerAttackTick;
    private int powerGrowSpeed = 15;
    private int powerGrowTick;

    public Player(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height);
        this.playing = playing;
        this.state = IDLE;
        this.maxHealth = 100;
        this.currentHealth = 50;
        this.walkSpeed = Game.SCALE * 1.0f;
        loadAnimations();
        initHitbox(20, 27);
        initAttackBox();
    }

    public void setSpawn(Point spawn) {
        this.x = spawn.x;
        this.y = spawn.y;
        hitbox.x = x;
        hitbox.y = y;
    }

    private void initAttackBox() {
        attackBox = new Rectangle2D.Float(x, y, (int) (20 * Game.SCALE), (int) (20 * Game.SCALE));
        resetAttackBox();
    }

    public void update() {
        updateHealthBar();
        updatePowerBar();

        if (currentHealth <= 0) {
            if (state != DEAD) {
                state = DEAD;
                aniTick = 0;
                aniIndex = 0;
                playing.setPlayerDying(true);
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);
            } else if (aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
                playing.setGameOver(true);
                playing.getGame().getAudioPlayer().stopSong();
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
            } else
                updateAnimationTick();

            return;
        }

        updateAttackBox();

        updatePos();
        if (moving) {
            checkPotionTouched();
            checkSpikesTouched();
            tileY = (int) (hitbox.y / Game.TILES_SIZE);
        }

        if (powerAttackActive) {
            powerAttackTick++;
            if (powerAttackTick >= 35) {
                powerAttackTick = 0;
                powerAttackActive = false;
            }
        }

        if (attacking || powerAttackActive)
            checkAttack();

        updateAnimationTick();
        setAnimation();
    }

    private void checkSpikesTouched() {
        playing.checkSpikesTouched(this);
    }

    private void checkPotionTouched() {
        playing.checkPotionTouched(hitbox);
    }

    private void checkAttack() {
        if (attackChecked || aniIndex != 1)
            return;
        attackChecked = true;

        if (powerAttackActive)
            attackChecked = false;

        playing.checkEnemyHit(attackBox);
        playing.checkObjectHit(attackBox);
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    void updateAttackBox() {
        if (right && left) {
            if (flipW == 1) {
                attackBox.x = hitbox.x + hitbox.width + (int) (10 * Game.SCALE);
            } else {
                attackBox.x = hitbox.x - hitbox.width - (int) (10 * Game.SCALE);
            }
        } else if (right || (powerAttackActive && flipW == 1))
            attackBox.x = hitbox.x + hitbox.width + (int) (10 * Game.SCALE);
        else if (left || (powerAttackActive && flipW == -1))
            attackBox.x = hitbox.x - hitbox.width - (int) (10 * Game.SCALE);
        attackBox.y = hitbox.y + (10 * Game.SCALE);
    }

    private void updateHealthBar() {
        healthWidth = (int) (((float) currentHealth / (float) maxHealth) * healthBarWidth);
    }

    private void updatePowerBar() {
        powerWidth = (int) ((powerValue / (float) powerMaxValue) * powerBarWidth);

        powerGrowTick++;
        if (powerGrowTick >= powerGrowSpeed) {
            powerGrowTick = 0;
            changePower(1);
        }
    }

    public void render(Graphics g, int lvlOffset) {
        g.drawImage(animations[state][aniIndex],
                (int) (hitbox.x - xDrawOffset) - lvlOffset + flipX,
                (int) (hitbox.y - yDrawOffset),
                width * flipW, height, null);
//        drawAttackBox(g, lvlOffset);
        drawUI(g);
    }

    private void drawUI(Graphics g) {
        //BackgroundUI
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);

        // HealthBar
        g.setColor(Color.RED);
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);

        //Power Bar
        g.setColor(Color.YELLOW);
        g.fillRect(powerBarXStart + statusBarX, powerBarYStart + statusBarY, powerWidth, powerBarHeight);
    }

    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= ANI_SPEED) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= GetSpriteAmount(state)) {
                aniIndex = 0;
                attacking = false;
                attackChecked = false;
            }
        }
    }

    private void setAnimation() {
        int startAni = state;

        if (moving)
            state = RUNNING;
        else
            state = IDLE;

        if (inAir) {
            if (airSpeed < 0)
                state = JUMP;
            else {
                state = FALLING;
            }
        }

        if (powerAttackActive) {
            state = ATTACK;
            aniIndex = 1;
            aniTick = 0;
            return;
        }

        if (attacking) {
            state = ATTACK;
            if (startAni != ATTACK) {
                aniIndex = 1;
                aniTick = 0;
                return;
            }
        }

        if (startAni != state) {
            resetAniTick();
        }
    }

    private void resetAniTick() {
        aniTick = 0;
        aniIndex = 0;
    }

    private void updatePos() {
        moving = false;

        if (jump)
            jump();

        if (!inAir)
            if (!powerAttackActive)
                if ((!left && !right) || (left && right))
                    return;

        float xSpeed = 0;

        if (left && !right) {
            xSpeed -= walkSpeed;
            flipX = width;
            flipW = -1;
        }
        if (right && !left) {
            xSpeed += walkSpeed;
            flipX = 0;
            flipW = 1;
        }

        if (powerAttackActive) {
            if ((!left && !right) || (left && right)) {
                if (flipW == -1)
                    xSpeed = -walkSpeed;
                else xSpeed = walkSpeed;
            }

            xSpeed *= 3;
        }

        if (!inAir)
            if (!IsEntityOnFloor(hitbox, lvdData))
                inAir = true;

        if (inAir && !powerAttackActive) {
            if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvdData)) {
                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
                updateXPos(xSpeed);
            } else {
                hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
                if (airSpeed > 0)
                    resetInAir();
                else airSpeed = fallSpeedAfterCollision;
                updateXPos(xSpeed);
            }
        } else {
            updateXPos(xSpeed);
        }
        moving = true;
    }

    private void jump() {
        if (inAir)
            return;
        playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
        inAir = true;
        airSpeed = jumpSpeed;
    }

    private void resetInAir() {
        inAir = false;
        airSpeed = 0;
    }

    private void updateXPos(float xSpeed) {
        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvdData)) {
            hitbox.x += xSpeed;
        } else {
            hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
            if (powerAttackActive) {
                powerAttackActive = false;
                powerAttackTick = 0;
            }
        }
    }

    public void changeHealth(int value) {
        currentHealth += value;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        } else if (currentHealth <= 0)
            currentHealth = 0;
    }

    public void changeHealth(int value, Enemy e) {
        if (state == HIT)
            return;
        changeHealth(value);
        pushBackOffsetDir = UP;
        pushDrawOffset = 0;

        if (e.getHitbox().x < hitbox.x)
            pushBackDir = RIGHT;
        else
            pushBackDir = LEFT;
    }


    public void kill() {
        currentHealth = 0;
    }


    public void changePower(int value) {
        powerValue += value;
        if (powerValue >= powerMaxValue)
            powerValue = powerMaxValue;
        else if (powerValue <= 0)
            powerValue = 0;
    }

    private void loadAnimations() {

        BufferedImage image = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);

        animations = new BufferedImage[7][8];
        for (int i = 0; i < animations.length; i++) {
            for (int j = 0; j < animations[i].length; j++) {
                animations[i][j] = image.getSubimage(j * 64, i * 40, 64, 40);
            }
        }

        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
    }

    public void loadLvlData(int[][] lvdData) {
        this.lvdData = lvdData;
        if (!IsEntityOnFloor(hitbox, lvdData))
            inAir = true;
    }

    public void resetDirBooleans() {
        left = false;
        right = false;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public void resetAll() {
        resetDirBooleans();
        inAir = false;
        attacking = false;
        moving = false;
        airSpeed = 0;
        state = IDLE;
        currentHealth = maxHealth;
        flipX = 0;
        flipW = 1;

        hitbox.x = x;
        hitbox.y = y;

        resetAttackBox();

        if (!IsEntityOnFloor(hitbox, lvdData))
            inAir = true;
    }

    private void resetAttackBox() {
        if (flipW == 1)
            attackBox.x = hitbox.x + hitbox.width + (int) (10 * Game.SCALE);
        else
            attackBox.x = hitbox.x - hitbox.width - (int) (10 * Game.SCALE);
    }

    public int getTileY() {
        return tileY;
    }

    public void powerAttack() {
        if (powerAttackActive)
            return;
        if (powerValue >= 60) {
            powerAttackActive = true;
            changePower(-60);
        }
    }
}
