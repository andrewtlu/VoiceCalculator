package com.andrewtlu;

/* Code base:
 * https://github.com/goxr3plus/Java-Speech-Recognizer-Tutorial--Calculator/blob/master/Tutorial%201/src/model/SpeechRecognizerMain.java
 *
 * Copyright 2017 goxr3plus, Copyright 2019 dinitrogen-tetroxide
 *
 * Original code modified and used as a starting class structure for the project.
 *
 * Code written in Java 1.8
 *
 * Calculator Information:
 * - Supports input from the positive/negative hundred billions to 10e-12 (12 decimal points)
 *   - Does not support changing float as of now
 * - Can change between degrees and radians
 * - Can get previous answer and current answer
 * - Currently does not have multiple voices, however all this requires is a compatible voice.jar file and a
 *          makeDecision implementation for it
 * - Allows for change from a regular mode to express (Speech output shortened for time)
 *
 * Calculator Defaults:
 * - Angle mode is in radians
 * - Float 12
 * - Regular mode
 * - cmu-rms-hsmm voice
 *
 * Complete list of calculator functions (Also in calculatorassets.SpeechExpression):
 * - Addition
 * - Subtraction
 * - Multiplication
 * - Division
 * - Exponentiation
 * - Square roots
 * - Factorials
 * - Log (base 10)
 * - Natural Log
 * - Various Trig Functions (sin, cos, tan, csc, sec, cot in both radians and degrees)
 *   - Undefined trig functions return value close to infinity instead of throwing ArithmeticException
 */

import com.andrewtlu.calculatorassets.SpeechExpression;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
// import com.andrewtlu.tts.TextToSpeech;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VoiceCalculator {
    private LiveSpeechRecognizer recognizer;
    private Logger logger = Logger.getLogger(getClass().getName());
    private String speechRecognitionResult;
    private SpeechExpression expression = new SpeechExpression();
    // private TextToSpeech tts = new TextToSpeech();
    private boolean express = false;

    private boolean listenForKeyword = true;

    private boolean speechRecognizerThreadRunning = false;
    private ExecutorService eventsExecutorService = Executors.newFixedThreadPool(2);

    /** Constructor for class */
    private VoiceCalculator() {
        logger.log(Level.INFO, "Loading Voice Calculator...\n");  // Start logging

        /* Speech to Text Portion */

        // Configurations
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setGrammarPath("resources/grammarFiles");
        configuration.setGrammarName("commands");
        configuration.setUseGrammar(true);

        try {
            recognizer = new LiveSpeechRecognizer(configuration);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        /* Text to Speech Portion */

        // tts.setVoice("voice-cmu-slt-hsmm");

        startSpeechRecognition();  //Start speech recognition thread
    }

    /** Starts the Speech Recognition Thread */
    private synchronized void startSpeechRecognition() {
        if (speechRecognizerThreadRunning)
            logger.log(Level.INFO, "Speech Recognition Thread already running.\n");  // Checks if thread is running
        else {
            eventsExecutorService.submit(() -> {
                speechRecognizerThreadRunning = true;
                listenForKeyword = true;

                recognizer.startRecognition(true);
                logger.log(Level.INFO, "Voice Calculator is ready.\n");
                // tts.speak("Voice calculator is ready");

                try {
                    while (speechRecognizerThreadRunning) {
                        SpeechResult speechResult = recognizer.getResult();

                        if (!listenForKeyword) {
                            if (speechResult == null)
                                logger.log(Level.INFO, "Speech not understood.\n");
                            else {
                                speechRecognitionResult = speechResult.getHypothesis();

                                System.out.println("Recognized phrase: [" + speechRecognitionResult + "]\n");

                                if (express) makeDecisionExpress(speechRecognitionResult);
                                else makeDecision(speechRecognitionResult);
                            }

                            listenForKeyword = true;
                        } else {
                            if (speechResult.getHypothesis().equals("voice calculator")) {
                                // tts.speak("Yes?", 2.0f, false, true);
                                listenForKeyword = false;
                                logger.log(Level.INFO, "Now listening for command.");
                            }

                            System.out.println();
                        }
                    }
                } catch (Exception ex) {
                    logger.log(Level.WARNING, null, ex);
                    speechRecognizerThreadRunning = false;
                }

                logger.log(Level.INFO, "Voice Calculator exited.");
            });
        }
    }

    /** Do something with speech */
    private void makeDecision(String speech) {
        String processedCommand;

        try {
            if (commandMatches(speech, new String[]{"^(set )(the )?.*", "^(change )(the )?.*"})) {
                processedCommand = processCommand(speech, new String[]{"^(set )(the )?", "^(change )(the )?"});

                switch (processedCommand) {  // todo: test switch statement
                    case "angle mode to degrees":
                        expression.setIsRadian(false);
                        // tts.speak("The angle mode is now in degrees.");
                        break;
                    case "angle mode to ray dee ins":
                        expression.setIsRadian(true);
                        // tts.speak("The angle mode is now in ray dee ins.");
                        break;
                    case "calculator mode to express":
                        express = true;
                        // tts.speak("Mode set to express.");
                        break;
                    case "calculator mode to regular":
                    case "calculator mode to normal":
                        // tts.speak("The calculator is already in normal mode.");
                        break;
                    default:
                        // tts.speak("Set parameters not understood, please try again.");
                }
            }
            else if (commandMatches(speech, new String[]{"^(get )(the )?.*", "^(what was )(the )?.*"})) {
                processedCommand = processCommand(speech, new String[]{"^(get the )", "^(what was the )"});

                switch (processedCommand) {
                    case "previous answer":
                    case "previous result":
                        // tts.speak("The previous answer was " + expression.getPreviousResult());
                        logger.log(Level.INFO, expression.getPreviousResult().toString());
                        break;
                    case "answer":
                    case "result":
                        // tts.speak("The answer was " + expression.getResult());
                        logger.log(Level.INFO, expression.getResult().toString());
                        break;
                    default:
                        // tts.speak("Get parameters not understood, please try again.");
                }
            }
            else if (commandMatches(speech, new String[]{"^(what is ).*"})) {
                processedCommand = processCommand(speech, new String[]{"^(what is )(the )?(current )?(value of )?"});

                if (processedCommand.equals("angle mode")) {
                    if (expression.isRad()) {
                        // tts.speak("The current angle mode is ray dee ins");
                        logger.log(Level.INFO, "Radians");
                    }
                    else {
                        // tts.speak("The current angle mode is degrees");
                        logger.log(Level.INFO, "Degrees");
                    }
                }
                else if (processedCommand.equals("calculator mode")) {
                    // tts.speak("The calculator is currently in normal mode.");
                }
                else {
                    try {  // Calculate result and return to user
                        expression.setAcousticRepresentation(processedCommand);
                        // tts.speak("The result of " + expression.getAcousticRepresentation() + " is equal to " +
                        //         expression.getResult());
                        logger.log(Level.INFO, expression.getResult().toString());
                    } catch (ArithmeticException ex) {  // Divide by zero error
                        // tts.speak("The result of " + expression.getAcousticRepresentation() + " is undefined.");
                        logger.log(Level.INFO, "undef");
                    } catch (RuntimeException ex) {
                        // tts.speak("Calculator expression not understood, please try again.");
                    }
                }
            }
            else if (commandMatches(speech, new String[]{"^(compute )(the value of )?.*",
                    "^(calculate )(the value of )?.*"})) {
                processedCommand = processCommand(speech, new String[]{"^(compute )(the value of )?",
                        "^(calculate )(the value of )?"});

                try {  // Calculate result and return to user
                    expression.setAcousticRepresentation(processedCommand);
                    // tts.speak("The result of " + expression.getAcousticRepresentation() + " is equal to " +
                    //         expression.getResult());
                    logger.log(Level.INFO, expression.getResult().toString());
                } catch (ArithmeticException ex) {  // Divide by zero error
                    // tts.speak("The result of " + expression.getAcousticRepresentation() + " is undefined.");
                        logger.log(Level.INFO, "undef");
                } catch (RuntimeException ex) {
                    // tts.speak("Calculator expression not understood, please try again.");
                }
            }
            else {
                // tts.speak("Input not understood, please try again.");
                logger.log(Level.INFO, "Bad input.");
            }
        } catch (RuntimeException ex) {
            // tts.speak("Input not understood, please try again.");
            logger.log(Level.INFO, "Bad input.");
        }
    }

    /** Express mode output */
    private void makeDecisionExpress(String speech) {
        String processedCommand;

        try {
            if (commandMatches(speech, new String[]{"^(set )(the )?.*", "^(change )(the )?.*"})) {
                processedCommand = processCommand(speech, new String[]{"^(set )(the )?", "^(change )(the )?"});

                switch (processedCommand) {  // todo: test switch statement
                    case "angle mode to degrees":
                        expression.setIsRadian(false);
                        // tts.speak("Mode set to degrees.");
                        break;
                    case "angle mode to ray dee ins":
                        expression.setIsRadian(true);
                        // tts.speak("Mode set to radians.");
                        break;
                    case "calculator mode to express":
                        // tts.speak("Mode is already express.");
                        break;
                    case "calculator mode to regular":
                    case "calculator mode to normal":
                        express = false;
                        // tts.speak("The calculator is now in normal mode.");
                        break;
                    default:
                        // tts.speak("Parameters not understood.");
                }
            }
            else if (commandMatches(speech, new String[]{"^(get )(the )?.*", "^(what was )(the )?.*"})) {
                processedCommand = processCommand(speech, new String[]{"^(get )(the )?", "^(what was )(the )?"});

                switch (processedCommand) {
                    case "previous answer":
                    case "previous result":
                        // tts.speak(expression.getPreviousResult().toPlainString());
                        logger.log(Level.INFO, expression.getPreviousResult().toString());
                        break;
                    case "answer":
                    case "result":
                        // tts.speak(expression.getResult().toPlainString());
                        logger.log(Level.INFO, expression.getResult().toString());
                        break;
                    default:
                        // tts.speak("Parameters not understood.");
                }
            }
            else if (commandMatches(speech, new String[]{"^(what is ).*"})) {
                processedCommand = processCommand(speech, new String[]{"^(what is )(the )?(current )?(value of )?"});

                if (processedCommand.equals("angle mode")) {
                    if (expression.isRad()) {
                        // tts.speak("Ray dee ins.");
                        logger.log(Level.INFO, "Radians");
                    } else {
                        // tts.speak("Degrees.");
                        logger.log(Level.INFO, "Degrees");
                    }
                }
                else if (processedCommand.equals("calculator mode")) {
                    // tts.speak("Express mode.");
                }
                else {
                    try {  // Calculate result and return to user
                        expression.setAcousticRepresentation(processedCommand);
                        // tts.speak(expression.getResult().toPlainString());
                        logger.log(Level.INFO, expression.getResult().toString());
                    } catch (ArithmeticException ex) {  // Divide by zero error
                        // tts.speak("Undefined.");
                        logger.log(Level.INFO, "undef");
                    } catch (RuntimeException ex) {
                        // tts.speak("Parameters not understood.");
                    }
                }
            }
            else if (commandMatches(speech, new String[]{"^(compute )(the value of )?.*",
                    "^(calculate )(the value of )?.*"})) {
                processedCommand = processCommand(speech, new String[]{"^(compute )(the value of )?",
                        "^(calculate )(the value of )?"});

                try {  // Calculate result and return to user
                    expression.setAcousticRepresentation(processedCommand);
                    // tts.speak(expression.getResult().toPlainString());
                    logger.log(Level.INFO, expression.getResult().toString());
                } catch (ArithmeticException ex) {  // Divide by zero error
                    // tts.speak("Undefined.");
                    logger.log(Level.INFO, "undef");
                } catch (RuntimeException ex) {
                    // tts.speak("Parameters not understood.");
                }
            }
            else {
                // tts.speak("Input not understood.");
                logger.log(Level.INFO, "Bad input.");
            }
        } catch (RuntimeException ex) {
            // tts.speak("Input not understood.");
            logger.log(Level.INFO, "Bad input.");
        }
    }

    /* ----------------------------------------- For processing commands/tts ---------------------------------------- */

    /** Checks if command matches */
    private boolean commandMatches(String command, String[] matches) {
        for (String match : matches) {
            if (command.matches(match)) return true;
        }
        return false;
    }

    /** Strip command with multiple matches and return stripped command or false */
    private String processCommand(String command, String[] matches) {
        for (String match : matches) {
            command = command.replaceAll(match, "");
        }
        return command;
    }

    /* -------------------------------------------------------------------------------------------------------------- */

    /** Main method */
    public static void main(String[] args) {
        new VoiceCalculator();
    }
}