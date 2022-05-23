package pipeline.utils;

import javax.sound.sampled.*;
import java.io.File;

public class GlSound
{
    public static void play(String filename) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(filename)));
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
