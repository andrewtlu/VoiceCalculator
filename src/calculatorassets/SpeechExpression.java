package calculatorassets;

/* Copyright 2019 dinitrogen-tetroxide
 *
 * Complete list of functions:
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
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SpeechExpression {
    private static HashMap<String, Integer> numberMap = new HashMap<>();
    private static HashMap<String, String> wordMap = new HashMap<>();


    private String acousticRepresentation;
    private String convertedExpression;  // Converted numbers and answer variable
    private String[] splitExpression;  // For actual processing
    private BigDecimal result = new BigDecimal(0);
    private BigDecimal previousResult = new BigDecimal(0);

    // Variables for parsing
    private int currentIndex = 0;
    private String currentWord;
    private boolean isRad = true;  // true = radians, false = degrees

    // Regex for numbers
    private static String validNumbers = "((one|two|three|four|five|six|seven|eight|nine) ?)";
    private static String basePattern = "((" + validNumbers + "|" +
            "((ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen) ?)|" +
            "((twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety) ?" + validNumbers + "?))|((" + validNumbers +
            "hundred ?)(((twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety)? ?" + validNumbers +
            "?)|(ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen))) ?)";
    private static String structurePattern = "(negative )?(?=\\w)(((" + basePattern + "billion ?)?(" + basePattern +
            "million ?)?(" + basePattern + "thousand ?)?" + basePattern + "?)|((zero ?)|(oh ?)))?((point ?)(" + validNumbers +
            "|((zero ?)|(oh ?))){0,12})?";

    public SpeechExpression() {
        buildNumberMap();
        buildWordMap();
    }

    /** Construct a SpeechNumber object given a string */
    public SpeechExpression(String saidString) {
        buildNumberMap();
        buildWordMap();
        setAcousticRepresentation(saidString);
    }

    /* ---------------------------------------- For numbers and converting them ------------------------------------- */

    /** Build HashMap for word/value matching */
    private static void buildNumberMap() {
        String[] validNumbers = {"zero", "oh", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
                "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen",
                "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety", "hundred",
                "thousand", "million", "billion"};
        int[] values = {0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 10, 20, 30, 40, 50, 60, 70,
                80, 90, 100, 1000, 1000000, 1000000000};
        for (int i = 0; i < validNumbers.length; i++) {
            numberMap.put(validNumbers[i], values[i]);
        }
    }

    /** Convert numbers in word form to their literal representations */
    private String replaceWordNumbers(String str) {
        String[] splitString = str.split(" ");
        StringBuilder splitNumber = new StringBuilder();
        StringBuilder processedString = new StringBuilder();
        boolean runHasNumber = false;

        int i = 0;
        while (i < splitString.length) {
            if (numberMap.get(splitString[i]) != null || splitString[i].equals(("point"))) runHasNumber = true;

            // Marks number start and end indices
            while (i < splitString.length && (numberMap.get(splitString[i]) != null || splitString[i].equals(("point")))) {
                splitNumber.append(splitString[i++]).append(" ");
            }

            // If there was a number in this run append it to the string
            if (runHasNumber) {
                processedString.append(convertToNumberValue(splitNumber.toString())).append(" ");
                splitNumber.setLength(0);
                runHasNumber = false;
            }
            else {  // Else skip number
                processedString.append(splitString[i]).append(" ");
                splitNumber.setLength(0);
                i++;
            }
        }

        return processedString.toString();
    }

    /** Check validity of number, for public use with objects */
    private boolean numIsValid(String str) {
        return str.matches(structurePattern);
    }

    /** Convert word representation to number representation, basically setter for numberRepresentation */
    private String convertToNumberValue(String str) {
        if (numIsValid(str)) {
            long parsedIntegerPortion = 0, temporaryValue = 0;
            String[] splitString = str.split(" ");
            int decimalStart = -1;
            int i;
            boolean isNegative = false;
            String numberValue;

            if (splitString[0].equals("negative")) {
                i = 1;
                isNegative = true;
            } else i = 0;

            for (; i < splitString.length; i++) {
                if (!splitString[i].matches("thousand|million|billion|point")) {  // Not placeholder case
                    if (splitString[i].matches("(hundred)")) temporaryValue *= numberMap.get(splitString[i]);
                    else temporaryValue += numberMap.get(splitString[i]);
                } else if (!splitString[i].matches("point")) {  // Placeholder case
                    temporaryValue *= numberMap.get(splitString[i]);
                    parsedIntegerPortion += temporaryValue;
                    temporaryValue = 0;
                } else {  // Decimal case
                    parsedIntegerPortion += temporaryValue;
                    decimalStart = i + 1;
                    temporaryValue = 0;
                    break;
                }
            }

            parsedIntegerPortion += temporaryValue;

            if (decimalStart > -1) {  // If decimal is present parse
                StringBuilder parsedDecimalPortion = new StringBuilder();
                parsedDecimalPortion.append(parsedIntegerPortion).append(".");

                for (i = decimalStart; i < splitString.length; i++) {
                    parsedDecimalPortion.append(numberMap.get(splitString[i]));
                }

                numberValue = isNegative ? "-" + parsedDecimalPortion.toString() : parsedDecimalPortion.toString();
            } else {
                numberValue = isNegative ? "-" + parsedIntegerPortion : Long.toString(parsedIntegerPortion);
            }

            return numberValue;
        } else throw new RuntimeException("'" + str + "' is not a valid number");
    }

    /* -------------------------------------------------------------------------------------------------------------- */

    /** Build HashMap for word/function matching */
    private static void buildWordMap() {  // e not listed but directly implemented due to technical issues
        String[] validFunctions = {"left parentheses", "right parentheses", "plus", "minus", "(divided by)|(over)",
                "times", "(multiplied by)|(times)", "to the power( of)?", "(the )?(square )?root( of)?", "percent",
                "fact or eel", "(the )?sign( of)?", "(the )?co sign( of)?", "(the )?tangent( of)?",
                "(the )?co see can't( of)?", "(the )?see can't( of)?", "(the )?co tangent( of)?",
                "(the )?natural log( of)?", "(the )?log( of)?", "pi"};
        String[] functionSymbols = {"(", ")", "+", "-", "/", "*", "*", "^", "sqrt", "* 0.01",
                "!", "sin", "cos", "tan", "csc", "sec", "cot", "ln", "log", Double.toString(Math.PI)};
        for (int i = 0; i < validFunctions.length; i++) {
            wordMap.put(validFunctions[i], functionSymbols[i]);
        }
    }

    /** Get convertedExpression */
    public String getConvertedExpression() {
        return convertedExpression;
    }

    /** Convert acousticRepresentation */
    private void setConvertedExpression() {
        convertedExpression = acousticRepresentation;
        StringBuilder medium = new StringBuilder();

        // Replaces all "answer"s in equation, replacing with previousResult
        convertedExpression = replaceWordNumbers(convertedExpression.replaceAll("(answer)",
                previousResult.toPlainString()));

        // Replaces functions with their representative symbols (easier splitExpression attained this way)
        for (Map.Entry<String, String> entry: wordMap.entrySet()) {
            convertedExpression = convertedExpression.replaceAll(entry.getKey(), entry.getValue());
        }

        // Separates the words and stores in splitExpression
        splitExpression = convertedExpression.split(" ");

        // Replace all "e"s, because when replacing with other functions/variables literally replaces all char "e"s
        for (int i = 0; i < splitExpression.length; i++) {
            if (splitExpression[i].equals("e")) splitExpression[i] = Double.toString(Math.E);
        }

        // Rebuild and assign to convertedExpression
        for (String e: splitExpression) {
            medium.append(" ").append(e);
        }
        convertedExpression = medium.deleteCharAt(0).toString();
    }

    /** Get acousticRepresentation */
    public String getAcousticRepresentation() {
        return acousticRepresentation;
    }

    /** Set acousticRepresentation */
    public void setAcousticRepresentation(String saidString) {
        acousticRepresentation = saidString;

        setConvertedExpression();
    }

    /** Get previous result */
    public BigDecimal getPreviousResult() {
        String strippedResult;

        // Strip trailing decimal zeros and decimal point
        strippedResult = previousResult.setScale(12, BigDecimal.ROUND_HALF_UP).toPlainString();
        strippedResult = strippedResult.contains(".") ?
                strippedResult.replaceAll("0*$", "").replaceAll("\\.$", "") :
                strippedResult;

        return new BigDecimal(strippedResult);
    }

    /** Get result */
    public BigDecimal getResult() {
        String strippedResult;

        calculateResult();  // Calculates result in order to make sure result is not null

        // Strip trailing decimal zeros and decimal point if no decimal
        strippedResult = result.setScale(12, BigDecimal.ROUND_HALF_UP).toPlainString();
        strippedResult = strippedResult.contains(".") ?
                strippedResult.replaceAll("0*$", "").replaceAll("\\.$", "") :
                strippedResult;

        return new BigDecimal(strippedResult);
    }

    /** Check if expression is valid */
    private boolean isValid() {
        // Char type 0 = numbers, char type 1 = double operand operators, char type 2 = 1 operand prefix operators,
        // char type 3 = 1 operand postfix operators type 3 = left parenthesis, char type 4 = right parenthesis
        byte charType, previousCharType;
        int unclosedCount = 0;

        // Set first charType
        if (splitExpression[0].matches("^(-?\\d*\\.\\d*)$|^(-?\\d+)$") || splitExpression[0].equals("answer"))
            charType = 0;
        else if (splitExpression[0].equals("(")) {
            unclosedCount++;
            charType = 4;
        }
        else if (splitExpression[0].matches("sin|cos|tan|csc|sec|cot|sqrt|log|ln")) charType = 2;
        else return false;  // Cannot be valid if starts with operators and closing parenthesis


        if (splitExpression.length == 1 && (charType == 4 || charType == 2)) return false;  // Returns false if equation is only one left parenthesis/prefix operator
        else if (splitExpression.length == 1) return true;  // Returns true if it's just one number
        else {  // If longer than one carries out parsing
            for (int i = 1; i < splitExpression.length; i++) {
                previousCharType = charType;

                // First defines what the current charType is
                if (splitExpression[i].matches("^(-?\\d*\\.\\d*)$|^(-?\\d+)$") ||
                        splitExpression[0].equals("answer")) charType = 0;
                else if (splitExpression[i].matches("sin|cos|tan|csc|sec|cot|sqrt|log")) charType = 2;
                else if (splitExpression[i].equals("!")) charType = 3;
                else if (splitExpression[i].equals("(")) charType = 4;
                else if (splitExpression[i].equals(")")) charType = 5;
                else charType = 1;

                /* Checks for different cases
                 * 1. Numbers and two operand operators cannot repeat
                 * 2. Left parenthesis can't be preceded by a number or postfix operator (View note)
                 * 3. Right parenthesis can't be preceded by an operator that takes an operand from behind
                 * 4. Left parenthesis can't be followed by an operator that takes an operand from in front
                 * 5. Right parenthesis can't be followed by a number/operator (View note)
                 *
                 * Note: Calculator does not currently support implied multiplication (i.e. 2pi or 2(3 + 1))*/
                if (previousCharType == charType && previousCharType < 2) return false;                          // 1
                else if (charType == 4 && (previousCharType == 0 || previousCharType == 3 || previousCharType == 5))
                    return false;                                                                                // 2
                else if (charType == 5 && (previousCharType == 1 || previousCharType == 2)) return false;        // 3
                else if (previousCharType == 4 && (charType == 1 || charType == 3 || charType == 5))
                    return false;                                                                                // 4
                else if (previousCharType == 5 && (charType == 0 || charType == 2)) return false;                // 5

                unclosedCount = splitExpression[i].equals("(") ? ++unclosedCount : unclosedCount;
                unclosedCount = splitExpression[i].equals(")") ? --unclosedCount : unclosedCount;

                if (unclosedCount == -1) return false;  // You can't have )( which still has an unclosed count of 0
            }
            return unclosedCount == 0 && charType != 1;
        }
    }

    /** Switch Radian/Degrees */
    public void setIsRadian(boolean setMode) {
        isRad = setMode;
    }

    /** Get if equation angle mode */
    public boolean isRad() {
        return isRad;
    }

    /** Calculate result */
    public void calculateResult() {
        if (isValid()) {
            currentWord = splitExpression[0];
            currentIndex = 0;

            BigDecimal gottenResult = parseAS();  // Intermediate value to check if the previousResult needs to be updated

            if (result != null && !previousResult.equals(gottenResult)) {
                previousResult = result;
                result = gottenResult;
            } else if (!previousResult.equals(gottenResult)) previousResult = result = gottenResult; // Case where result == null
            else result = gottenResult;
        }
        else throw new RuntimeException("'" + convertedExpression + "' is not a valid expression.");
    }

    // Next few methods are for solving expressions, parsing code based off of code here (released to public domain):
    // https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form

    /** Check & increase index  */
    private boolean strIsEqual(String str) {
        if (currentWord.equals(str)) {
            currentWord = (++currentIndex < splitExpression.length) ? splitExpression[currentIndex] : ".-.-.-";
            return true;
        }
        return false;
    }

    /** Check for numbers */
    private boolean isValidNumber() {
        if (currentWord.matches("^(-?\\d*\\.\\d*)$|^(-?\\d+)$")) {
            currentWord = (++currentIndex < splitExpression.length) ? splitExpression[currentIndex] : ".-.-.-";
            return true;
        }
        return false;
    }

    private BigDecimal parseAS() {
        BigDecimal x = parseMD();
        while (true) {
            if (strIsEqual("+")) x = x.add(parseMD());
            else if (strIsEqual("-")) x = x.subtract(parseMD());
            else return x;
        }
    }

    private BigDecimal parseMD() {
        BigDecimal x = parseGroup();
        while (true) {
            if (strIsEqual("*")) x = x.multiply(parseGroup());
            else if (strIsEqual("/")) x = x.divide(parseGroup(), BigDecimal.ROUND_HALF_UP);
            else return x;
        }
    }

    private BigDecimal parseGroup() {
        BigDecimal x = null;
        if (strIsEqual("(")) {
            x = parseAS();
            strIsEqual(")");  // Parses past the closing parenthesis
        }
        else if (isValidNumber()) x = new BigDecimal(splitExpression[currentIndex - 1]);
        else {
            if (strIsEqual("sqrt")) x = new BigDecimal(Math.sqrt(parseGroup().doubleValue()));
            else if (strIsEqual("log")) x = new BigDecimal(Math.log10(parseGroup().doubleValue()));
            else if (strIsEqual("ln")) x = new BigDecimal(Math.log(parseGroup().doubleValue()));

            if (isRad) {  // Radians todo: add throwing arithmeticexception when doing undefined stuff
                if (strIsEqual("sin")) x = new BigDecimal(Math.sin(parseAS().doubleValue()));
                else if (strIsEqual("cos")) x = new BigDecimal(Math.cos(parseAS().doubleValue()));
                else if (strIsEqual("tan")) x = new BigDecimal(Math.tan(parseAS().doubleValue()));
                else if (strIsEqual("csc")) x = new BigDecimal(1/Math.sin(parseAS().doubleValue()));
                else if (strIsEqual("sec")) x = new BigDecimal(1/Math.cos(parseAS().doubleValue()));
                else if (strIsEqual("cot")) x = new BigDecimal(1/Math.tan(parseAS().doubleValue()));
            } else {  // Degrees
                if (strIsEqual("sin")) x = new BigDecimal(Math.sin(Math.toRadians(parseAS().doubleValue())));
                else if (strIsEqual("cos")) x = new BigDecimal(Math.cos(Math.toRadians(parseAS().doubleValue())));
                else if (strIsEqual("tan")) x = new BigDecimal(Math.tan(Math.toRadians(parseAS().doubleValue())));
                else if (strIsEqual("csc")) x = new BigDecimal(Math.toRadians(1/Math.sin(parseAS().doubleValue())));
                else if (strIsEqual("sec")) x = new BigDecimal(Math.toRadians(1/Math.cos(parseAS().doubleValue())));
                else if (strIsEqual("cot")) x = new BigDecimal(Math.toRadians(1/Math.tan(parseAS().doubleValue())));
            }
        }

        // Other operators
        if (strIsEqual("!")) x = new BigDecimal(factorial(x.longValue()));
        if (strIsEqual("^")) x = new BigDecimal(Math.pow(x.doubleValue(), parseGroup().doubleValue()));

        return x;
    }

    /** Factorial Function */
    private long factorial(long x) {
        if (x > 0) return x * factorial(x - 1);
        else if (x == 0) return 1;
        else throw new RuntimeException("Cannot have a negative factorial!");
    }
}
