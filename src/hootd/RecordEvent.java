/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hootd;

import javax.sound.sampled.*;
import java.io.*;

/**
 *
 * @author jesse
 */
public class RecordEvent extends Event {

    private static String FILE_EXT = ".wav";

    @Override
    public void process() {
        Logger.log("Recording for " + this.duration + " seconds");
        String filename = "Recording - " + this.startDateTime.getTime() + FILE_EXT;
        filename = filename.replace(':',' ');
        recordAudio(filename, 44100, 16, this.duration);
        this.isFinished = true;
    }

    private void recordAudio(String outputFile, int samplerate, int bitrate, int duration) {
        try {
            AudioFormat linearFormat = new AudioFormat(samplerate, bitrate, 1, true, false);

            float sampleRate = linearFormat.getSampleRate();
            float bitRate = linearFormat.getSampleSizeInBits();
            int audioChannels = linearFormat.getChannels();
            //calculate the number of bytes we should hold to record audio for this.duration (seconds)
            int numberOfBytesToRecord = (int)Math.ceil((sampleRate * bitRate * audioChannels * this.duration) / 8);

            //Calculate the needed size of the byte array to store a clip
            byte[] audioData = new byte[numberOfBytesToRecord];

            TargetDataLine targetDataLine = null;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, linearFormat);
            try {
                targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
                targetDataLine.open(linearFormat);
            } catch (LineUnavailableException e) {
                Logger.log("Unable to lock a recording device audio channel (DataLine):" + e.getMessage());
                return;
            }
            targetDataLine.start();

            AudioInputStream linearStream = new AudioInputStream(targetDataLine);

            linearStream.read(audioData, 0, audioData.length);
            targetDataLine.stop();
            targetDataLine.close();

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile, false);
                ByteArrayInputStream baiStream = new ByteArrayInputStream(audioData);
                AudioInputStream aiStream = new AudioInputStream(baiStream, linearFormat, audioData.length);
                AudioSystem.write(aiStream, AudioFileFormat.Type.AIFF, fileOutputStream);
                aiStream.close();
                baiStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
                Logger.log("Saved audio file: " + outputFile);
            } catch (Exception e) {
                Logger.log("Error while attempting to save a recorded audio file: " + e.getMessage());
            }

        } catch (Exception e) {
            Logger.log("Error while attempting to record audio: " + e.getMessage());
        }
    }

    @Override
    public String printEvent() {
        String returnString = "";
        returnString += ("Start record:" + this.startDateTime.getTime() + "\n");
        returnString += ("Record duration:" + this.duration);
        return returnString;
    }
}
