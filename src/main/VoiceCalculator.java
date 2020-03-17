package main;

/* Code base:
 * https://github.com/goxr3plus/Java-Speech-Recognizer-Tutorial--Calculator/blob/master/Tutorial%201/src/model/SpeechRecognizerMain.java
 *
 * Copyright 2017 goxr3plus, Copyright 2019 dinitrogen-tetroxide
 *
 * Original code modified and used as a starting point of project (Specifically, the multithreading parts)
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

import calculatorassets.SpeechExpression;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import tts.TextToSpeech;

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
    private TextToSpeech tts = new TextToSpeech();
    private boolean express = false;

    private boolean listenForKeyword = true;  // Tells thread whether or not to listen for a keyword

    // Thread variables
    private boolean speechRecognizerThreadRunning = false;
    private ExecutorService eventsExecutorService = Executors.newFixedThreadPool(2);

    /** Constructor for class */
    private VoiceCalculator() {
        logger.log(Level.INFO, "Loading Voice Calculator...\n");  // Start logging

        /* Speech to Text Portion */

        // Defines new configuration object
        Configuration configuration = new Configuration();

        // Load models from jar and define grammar
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setGrammarPath("resources/grammarFiles");
        configuration.setGrammarName("commands");
        configuration.setUseGrammar(true);

        // Initializes LiveSpeechRecognizer, catching IOException
        try {
            recognizer = new LiveSpeechRecognizer(configuration);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        /* Text to Speech Portion */

        tts.setVoice("cmu-rms-hsmm");

        startSpeechRecognition();  //Start speech recognition thread
    }

    /** Starts the Speech Recognition Thread */
    private synchronized void startSpeechRecognition() {
        if (speechRecognizerThreadRunning)
            logger.log(Level.INFO, "Speech Recognition Thread already running.\n");  // Checks if thread is running
        else {
            // Calls submit method
            eventsExecutorService.submit(() -> {
                // Locks usage
                speechRecognizerThreadRunning = true;
                listenForKeyword = true;

                // Start Recognition
                recognizer.startRecognition(true);
                logger.log(Level.INFO, "Voice Calculator is ready.\n");
                tts.speak("Voice calculator is ready");

                try {
                    while (speechRecognizerThreadRunning) {
                        // Gets hypothesis from recognizer
                        SpeechResult speechResult = recognizer.getResult();

                        // Check if we ignore the speech recognition results
                        if (!listenForKeyword) {
                            // Check the result
                            if (speechResult == null)
                                logger.log(Level.INFO, "Speech not understood.\n");
                            else {
                                // Get the hypothesis
                                speechRecognitionResult = speechResult.getHypothesis();

                                // Print recognized phrase
                                System.out.println("Recognized phrase: [" + speechRecognitionResult + "]\n");

                                // Do something with command
                                if (express) makeDecisionExpress(speechRecognitionResult);
                                else makeDecision(speechRecognitionResult);
                            }

                            listenForKeyword = true;  // Continue listening for keyword
                        } else {
                            if (speechResult.getHypothesis().equals("voice calculator")) {
                                tts.speak("Yes?", 2.0f, false, true);
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
                        tts.speak("The angle mode is now in degrees.");
                        break;
                    case "angle mode to ray dee ins":
                        expression.setIsRadian(true);
                        tts.speak("The angle mode is now in ray dee ins.");
                        break;
                    case "calculator mode to express":
                        express = true;
                        tts.speak("Mode set to express.");
                        break;
                    case "calculator mode to regular":
                    case "calculator mode to normal":
                        tts.speak("The calculator is already in normal mode.");
                        break;
                    default:
                        tts.speak("Set parameters not understood, please try again.");
                }
            }  // Set command
            else if (commandMatches(speech, new String[]{"^(get )(the )?.*", "^(what was )(the )?.*"})) {
                processedCommand = processCommand(speech, new String[]{"^(get the )", "^(what was the )"});

                switch (processedCommand) {
                    case "previous answer":
                    case "previous result":
                        tts.speak("The previous answer was " + expression.getPreviousResult());
                        break;
                    case "answer":
                    case "result":
                        tts.speak("The answer was " + expression.getResult());
                        break;
                    default:
                        tts.speak("Get parameters not understood, please try again.");
                }
            }  // Get command
            else if (commandMatches(speech, new String[]{"^(what is ).*"})) {
                processedCommand = processCommand(speech, new String[]{"^(what is )(the )?(current )?(value of )?"});

                if (processedCommand.equals("angle mode")) {
                    if (expression.isRad()) tts.speak("The current angle mode is ray dee ins");
                    else tts.speak("The current angle mode is degrees");
                }
                else if (processedCommand.equals("calculator mode")) {
                    tts.speak("The calculator is currently in normal mode.");
                }
                else {
                    try {  // Calculate result and return to user
                        expression.setAcousticRepresentation(processedCommand);
                        tts.speak("The result of " + expression.getAcousticRepresentation() + " is equal to " +
                                expression.getResult());
                    } catch (ArithmeticException ex) {  // Divide by zero error
                        tts.speak("The result of " + expression.getAcousticRepresentation() + " is undefined.");
                    } catch (RuntimeException ex) {
                        tts.speak("Calculator expression not understood, please try again.");
                    }
                }
            }  // What is "ambiguous case" command
            else if (commandMatches(speech, new String[]{"^(compute )(the value of )?.*",
                    "^(calculate )(the value of )?.*"})) {
                processedCommand = processCommand(speech, new String[]{"^(compute )(the value of )?",
                        "^(calculate )(the value of )?"});

                try {  // Calculate result and return to user
                    expression.setAcousticRepresentation(processedCommand);
                    tts.speak("The result of " + expression.getAcousticRepresentation() + " is equal to " +
                            expression.getResult());
                } catch (ArithmeticException ex) {  // Divide by zero error
                    tts.speak("The result of " + expression.getAcousticRepresentation() + " is undefined.");
                } catch (RuntimeException ex) {
                    tts.speak("Calculator expression not understood, please try again.");
                }
            }  // Calculate command
            else {
                tts.speak("Input not understood, please try again.");
            }
        } catch (RuntimeException ex) {
            tts.speak("Input not understood, please try again.");
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
                        tts.speak("Mode set to degrees.");
                        break;
                    case "angle mode to ray dee ins":
                        expression.setIsRadian(true);
                        tts.speak("Mode set to radians.");
                        break;
                    case "calculator mode to express":
                        tts.speak("Mode is already express.");
                        break;
                    case "calculator mode to regular":
                    case "calculator mode to normal":
                        express = false;
                        tts.speak("The calculator is now in normal mode.");
                        break;
                    default:
                        tts.speak("Parameters not understood.");
                }
            }  // Set command
            else if (commandMatches(speech, new String[]{"^(get )(the )?.*", "^(what was )(the )?.*"})) {
                processedCommand = processCommand(speech, new String[]{"^(get )(the )?", "^(what was )(the )?"});

                switch (processedCommand) {
                    case "previous answer":
                    case "previous result":
                        tts.speak(expression.getPreviousResult().toPlainString());
                        break;
                    case "answer":
                    case "result":
                        tts.speak(expression.getResult().toPlainString());
                        break;
                    default:
                        tts.speak("Parameters not understood.");
                }
            }  // Get command
            else if (commandMatches(speech, new String[]{"^(what is ).*"})) {
                processedCommand = processCommand(speech, new String[]{"^(what is )(the )?(current )?(value of )?"});

                if (processedCommand.equals("angle mode")) {
                    if (expression.isRad()) tts.speak("Ray dee ins.");
                    else tts.speak("Degrees.");
                }
                else if (processedCommand.equals("calculator mode")) {
                    tts.speak("Express mode.");
                }
                else {
                    try {  // Calculate result and return to user
                        expression.setAcousticRepresentation(processedCommand);
                        tts.speak(expression.getResult().toPlainString());
                    } catch (ArithmeticException ex) {  // Divide by zero error
                        tts.speak("Undefined.");
                    } catch (RuntimeException ex) {
                        tts.speak("Parameters not understood.");
                    }
                }
            }  // What is "ambiguous case" command
            else if (commandMatches(speech, new String[]{"^(compute )(the value of )?.*",
                    "^(calculate )(the value of )?.*"})) {
                processedCommand = processCommand(speech, new String[]{"^(compute )(the value of )?",
                        "^(calculate )(the value of )?"});

                try {  // Calculate result and return to user
                    expression.setAcousticRepresentation(processedCommand);
                    tts.speak(expression.getResult().toPlainString());
                } catch (ArithmeticException ex) {  // Divide by zero error
                    tts.speak("Undefined.");
                } catch (RuntimeException ex) {
                    tts.speak("Parameters not understood.");
                }
            }  // Calculate command
            else {
                tts.speak("Input not understood.");
            }
        } catch (RuntimeException ex) {
            tts.speak("Input not understood.");
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
