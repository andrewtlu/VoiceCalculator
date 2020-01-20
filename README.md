# VoiceCalculator

This is a basic audio (voice) input and output calculator written in Java 1.8.

## Calculator Info
The calculator is not a scientific calculator, and should not be used in a professional setting for the purpose of performing calculations (just yet).

- Supports input from the positive/negative hundred billions to 10e-12 (12 decimal places)
  - Does not support changing float as of now
- Can change between degrees and radians
- Can get previous answer and current answer
- Currently does not have multiple voices, however all this requires is a compatible voice.jar file and a
         makeDecision implementation for it
- Allows for change from a regular mode to express (Speech output shortened for time)

### Calculator Defaults:
- Angle mode is in radians
- Float 12
- Regular mode
- cmu-rms-hsmm voice
### Complete list of calculator functions:
- Addition
- Subtraction
- Multiplication
- Division
- Exponentiation
- Square roots
- Factorials
- Log (base 10)
- Natural Log
- Various Trig Functions (sin, cos, tan, csc, sec, cot in both radians and degrees)
  - Undefined trig functions return value close to infinity instead of throwing ArithmeticException

## Installing
Feel free to download the source code and the releases. The program is written in Java 1.8 using Intellij.

## Built With
* [CMU's Sphinx4 Library](https://github.com/cmusphinx/sphinx4) & rms-hsmm voice
* [MaryTTS](https://github.com/marytts/marytts)
* [Java Version 1.8.0.221](https://www.oracle.com/technetwork/java/javase/8u221-relnotes-5480116.html)

## Authors
* **Andrew Lu** ([rocketFuel](https://github.com/dinitrogen-tetroxide))
* See all the [contributors](https://github.com/dinitrogen-tetroxide/VoiceCalculator/graphs/contributors)

## Licensing
* Project is under Apache 2.0 license, see [LICENSE.md](LICENSE.md) for details

## Acknowledgements
* [Goxr3plus](https://github.com/goxr3plus) for the base code for multiple files
* [PurpleBooth](https://gist.github.com/PurpleBooth) for the README.md template
* All the people who helped and resources used to pull through with this project
