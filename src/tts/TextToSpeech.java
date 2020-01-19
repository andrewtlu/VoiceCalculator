package tts;

import java.io.File;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.modules.synthesis.Voice;
import marytts.signalproc.effects.AudioEffect;
import marytts.signalproc.effects.AudioEffects;

/* Code based off of:
 * https://github.com/goxr3plus/Java-Text-To-Speech-Tutorial/blob/master/Mary%20TTS%20Program/src/application/TextToSpeech.java
 *
 * Copyright 2017 goxr3plus
 *
 * Original code commenting changed, added an overloaded speak method for ease of use
 */

public class TextToSpeech {
    private AudioPlayer tts;
    private MaryInterface marytts;
    private File log = new File("log/server.log");

    /** Constructor */
    public TextToSpeech() {
        if (log.delete()) {
            System.out.println("Log successfully reset.");
        } else {
            System.out.println("Log reset unsuccessful.");
        }
        try {
            marytts = new LocalMaryInterface();
        } catch (MaryConfigurationException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** Text to Speech */
    public void speak(String text, float gainValue, boolean daemon, boolean join) {
        // Stop the previous player
        stopSpeaking();

        try (AudioInputStream audio = marytts.generateAudio(text)) {
            // Player has to be initialized every time
            tts = new AudioPlayer();
            tts.setAudio(audio);
            tts.setGain(gainValue);
            tts.setDaemon(daemon);
            tts.start();
            if (join) tts.join();
        } catch (SynthesisException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error saying phrase", ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "IO Exception", ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Interrupted", ex);
            tts.interrupt();
        }
    }

    /** Overloaded Text to Speech */
    public void speak(String text) {
        // Stop the previous player
        stopSpeaking();

        try (AudioInputStream audio = marytts.generateAudio(text)) {
            // Player has to be initialized every time
            tts = new AudioPlayer();
            tts.setAudio(audio);
            tts.setGain(0.5f);
            tts.setDaemon(false);
            tts.start();
            tts.join();
        } catch (SynthesisException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error saying phrase", ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "IO Exception", ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Interrupted", ex);
            tts.interrupt();
        }
    }

    /** Stop TTS */
    public void stopSpeaking() {
        // Stop the previous player
        if (tts != null)
            tts.cancel();
    }

    /** Get available voices for TTS */
    public Collection<Voice> getAvailableVoices() {
        return Voice.getAvailableVoices();
    }

    /** Return MaryInterface */
    public MaryInterface getMarytts() {
        return marytts;
    }

    /** Return list of available AudioEffects */
    public List<AudioEffect> getAudioEffects() {
        return StreamSupport.stream(AudioEffects.getEffects().spliterator(), false).collect(Collectors.toList());
    }

    /** Change TTS voice */
    public void setVoice(String voice) {
        marytts.setVoice(voice);
    }

}