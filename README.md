# VoiceCalculator v2.0-alpha

### TODOs:
- Core:
   - [x] Update Sphinx4
   - [x] Update MaryTTS
   - [x] Ensure maven builds
   - [ ] Rewrite to fit in maven project
- Enhancements
   - [ ] More efficient equation processing
   - [ ] Fix up regex/grammar files
   - [ ] Custom BigInt/Float classes
   - [ ] Allow easier input (pauses, etc)
   - [ ] Adapt model to have better accuracy
   - [ ] Fix up ReadMe


### Calculator Info

- Supports input from the positive/negative hundred billions to 10e-12 (12 decimal places)
  - Does not support changing float as of now
- Can change between degrees and radians
- Can get previous answer and current answer
- Currently does not have multiple voices, however all this requires is a compatible voice.jar file and a makeDecision implementation for it
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

## Built With
* [CMU's Sphinx4 Library](https://github.com/cmusphinx/sphinx4) & rms-hsmm voice
* [MaryTTS](https://github.com/marytts/marytts)

## Metadata
* Made with love by [Andrew Lu](https://github.com/andrewtlu)
* Project is under Apache 2.0 license, see [LICENSE.md](LICENSE.md) for details
* Acknowledgements:
  * [Goxr3plus](https://github.com/goxr3plus) for the base code for multiple files
