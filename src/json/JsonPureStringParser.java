/*******************************************************************************
 * Copyright (c) 2015 Stefan Marr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package json;

import asr.AsrString;

public final class JsonPureStringParser {

  private static final AsrString L_EMPTY = AsrString.fromLiteral("");
  private static final AsrString L_ERROR = AsrString.fromLiteral("Unexpected character");
  private static final AsrString L_VALUE = AsrString.fromLiteral("value");

  private static final AsrString L_N = AsrString.fromLiteral("n");
  private static final AsrString L_T = AsrString.fromLiteral("t");
  private static final AsrString L_F = AsrString.fromLiteral("f");
  private static final AsrString L_U = AsrString.fromLiteral("u");
  private static final AsrString L_L = AsrString.fromLiteral("l");
  private static final AsrString L_R = AsrString.fromLiteral("r");
  private static final AsrString L_A = AsrString.fromLiteral("a");
  private static final AsrString L_S = AsrString.fromLiteral("s");
  private static final AsrString L_B = AsrString.fromLiteral("b");

  private static final AsrString L_SMALL_E = AsrString.fromLiteral("e");
  private static final AsrString L_BIG_E = AsrString.fromLiteral("E");

  private static final AsrString L_QUOTE = AsrString.fromLiteral("\"");
  private static final AsrString L_SQ_BR_OPEN = AsrString.fromLiteral("[");
  private static final AsrString L_G_BR_OPEN = AsrString.fromLiteral("{");
  private static final AsrString L_MIN = AsrString.fromLiteral("-");
  private static final AsrString L_0 = AsrString.fromLiteral("0");
  private static final AsrString L_1 = AsrString.fromLiteral("1");
  private static final AsrString L_2 = AsrString.fromLiteral("2");
  private static final AsrString L_3 = AsrString.fromLiteral("3");
  private static final AsrString L_4 = AsrString.fromLiteral("4");
  private static final AsrString L_5 = AsrString.fromLiteral("5");
  private static final AsrString L_6 = AsrString.fromLiteral("6");
  private static final AsrString L_7 = AsrString.fromLiteral("7");
  private static final AsrString L_8 = AsrString.fromLiteral("8");
  private static final AsrString L_9 = AsrString.fromLiteral("9");

  private static final AsrString L_SQ_BR_CLOSE = AsrString.fromLiteral("]");
  private static final AsrString L_COMMA = AsrString.fromLiteral(",");
  private static final AsrString L_COL = AsrString.fromLiteral(":");
  private static final AsrString L_G_BR_CLOSE = AsrString.fromLiteral("}");

  private static final AsrString L_ESCAPE = AsrString.fromLiteral("\\");
  private static final AsrString L_SLASH = AsrString.fromLiteral("/");
  private static final AsrString L_DOT = AsrString.fromLiteral(".");

  private static final AsrString L_PLUS = AsrString.fromLiteral("+");
  private static final AsrString L_DIGIT = AsrString.fromLiteral("digit");

  private static final AsrString L_SPACE = AsrString.fromLiteral(" ");
  private static final AsrString L_ESCAPE_N = AsrString.fromLiteral("\n");
  private static final AsrString L_ESCAPE_T = AsrString.fromLiteral("\t");
  private static final AsrString L_ESCAPE_R = AsrString.fromLiteral("\r");
  private static final AsrString L_ESCAPE_B = AsrString.fromLiteral("\b");
  private static final AsrString L_ESCAPE_F = AsrString.fromLiteral("\f");

  private static final AsrString L_EXP_ERROR = AsrString.fromLiteral("',' or ']'");
  private static final AsrString L_NAME = AsrString.fromLiteral("name");


  private final AsrString input;
  private int index;
  private int line;
  private int column;
  private AsrString current;
  private AsrString captureBuffer;
  private int captureStart;

  public JsonPureStringParser(final AsrString string) {
    this.input = string;
    index = -1;
    line = 1;
    captureStart = -1;
    column = 0;
    current = null;
    captureBuffer = L_EMPTY;
  }

  public JsonValue parse() {
    read();
    skipWhiteSpace();
    JsonValue result = readValue();
    skipWhiteSpace();
    if (!isEndOfText()) {
      throw error(L_ERROR);
    }
    return result;
  }

  private JsonValue readValue() {
    if (current.equals(L_N)) {
      return readNull();
    } else if (current.equals(L_T)) {
      return readTrue();
    } else if (current.equals(L_F)) {
      return readFalse();
    } else if (current.equals(L_QUOTE)) {
      return readString();
    } else if (current.equals(L_SQ_BR_OPEN)) {
      return readArray();
    } else if (current.equals(L_G_BR_OPEN)) {
      return readObject();
    } else if (current.equals(L_MIN) ||
              current.equals(L_0) || 
              current.equals(L_1) ||
              current.equals(L_2) ||
              current.equals(L_3) ||
              current.equals(L_4) ||
              current.equals(L_5) ||
              current.equals(L_6) ||
              current.equals(L_7) ||
              current.equals(L_8) ||
              current.equals(L_9)) {
      return readNumber();
    } else {
      throw expected(L_VALUE); 
    }
  }

  private JsonArray readArray() {
    read();
    JsonArray array = new JsonArray();
    skipWhiteSpace();
    if (readChar(L_SQ_BR_CLOSE)) {
      return array;
    }
    do {
      skipWhiteSpace();
      array.add(readValue());
      skipWhiteSpace();
    } while (readChar(L_COMMA));
    if (!readChar(L_SQ_BR_CLOSE)) {
      throw expected(L_EXP_ERROR);
    }
    return array;
  }

  private JsonObject readObject() {
    read();
    JsonObject object = new JsonObject();
    skipWhiteSpace();
    if (readChar(L_G_BR_CLOSE)) {
      return object;
    }
    do {
      skipWhiteSpace();
      AsrString name = readName();
      skipWhiteSpace();
      if (!readChar(L_COL)) {
        throw expected(L_COL);
      }
      skipWhiteSpace();
      object.add(name, readValue());
      skipWhiteSpace();
    } while (readChar(L_COMMA));

    if (!readChar(L_G_BR_CLOSE)) {
      throw expected(L_EXP_ERROR);
    }
    return object;
  }

  private AsrString readName() {
    if (!current.equals(L_QUOTE)) {
      throw expected(L_NAME);
    }
    return readStringInternal();
  }

  private JsonValue readNull() {
    read();
    readRequiredChar(L_U);
    readRequiredChar(L_L);
    readRequiredChar(L_L);
    return JsonLiteral.NULL;
  }

  private JsonValue readTrue() {
    read();
    readRequiredChar(L_R);
    readRequiredChar(L_U);
    readRequiredChar(L_SMALL_E);
    return JsonLiteral.TRUE;
  }

  private JsonValue readFalse() {
    read();
    readRequiredChar(L_A);
    readRequiredChar(L_L);
    readRequiredChar(L_S);
    readRequiredChar(L_SMALL_E);
    return JsonLiteral.FALSE;
  }

  private void readRequiredChar(final AsrString ch) {
    if (!readChar(ch)) {
      throw expected(L_EXP_ERROR);
    }
  }

  private JsonValue readString() {
    return new JsonString(readStringInternal());
  }

  private AsrString readStringInternal() {
    read();
    startCapture();
    while (!current.equals(L_QUOTE)) {
      if (current.equals(L_ESCAPE)) {
        pauseCapture();
        readEscape();
        startCapture();
      } else {
        read();
      }
    }
    AsrString string = endCapture();
    read();
    return string;
  }

  private void readEscape() {
    read();
    if (current.equals(L_QUOTE) || current.equals(L_SLASH) || current.equals(L_ESCAPE)) {
      captureBuffer.addConcat(current);
    } else if (current.equals(L_B)) {
      captureBuffer.addConcat(L_ESCAPE_B); // += "\b";
    } else if (current.equals(L_F)) {
      captureBuffer.addConcat(L_ESCAPE_F); // += "\f";
    } else if (current.equals(L_N)) {
      captureBuffer.addConcat(L_ESCAPE_N); // += "\n";
    } else if (current.equals(L_R)) {
      captureBuffer.addConcat(L_ESCAPE_R); // += "\r";
    } else if (current.equals(L_T)) {
      captureBuffer.addConcat(L_ESCAPE_T); // += "\t";
    } else {
      throw expected(L_EXP_ERROR);
    }
    read();
  }

  private JsonValue readNumber() {
    startCapture();
    readChar(L_MIN);
    AsrString firstDigit = current;
    if (!readDigit()) {
      throw expected(L_DIGIT);
    }
    if (!firstDigit.equals(L_0)) {
      // Checkstyle: stop
      while (readDigit()) { }
     // Checkstyle: resume
    }
    readFraction();
    readExponent();
    return new JsonNumber(endCapture());
  }

  private boolean readFraction() {
    if (!readChar(L_DOT)) {
      return false;
    }
    if (!readDigit()) {
      throw expected(L_DIGIT);
    }
    // Checkstyle: stop
    while (readDigit()) { }
    // Checkstyle: resume
    return true;
  }

  private boolean readExponent() {
    if (!readChar(L_SMALL_E) && !readChar(L_BIG_E)) {
      return false;
    }
    if (!readChar(L_PLUS)) {
      readChar(L_MIN);
    }
    if (!readDigit()) {
      throw expected(L_DIGIT);
    }

    // Checkstyle: stop
    while (readDigit()) { }
    // Checkstyle: resume
    return true;
  }

  private boolean readChar(final AsrString ch) {
    if (!current.equals(ch)) {
      return false;
    }
    read();
    return true;
  }

  private boolean readDigit() {
    if (!isDigit()) {
      return false;
    }
    read();
    return true;
  }

  private void skipWhiteSpace() {
    while (isWhiteSpace()) {
      read();
    }
  }

  private void read() {
    if (L_ESCAPE_N.equals(current)) {
      line++;
      column = 0;
    }
    index++;
    if (index < input.length()) {
      current = input.substring(index, index + 1);
    } else {
      current = null;
    }
  }

  private void startCapture() {
    captureStart = index;
  }

  private void pauseCapture() {
    int end = current == null ? index : index - 1;
    captureBuffer.addConcat(input.substring(captureStart, end + 1));
    captureStart = -1;
  }

  private AsrString endCapture() {
    int end = current == null ? index : index - 1;
    AsrString captured;
    if (L_EMPTY.equals(captureBuffer)) {
      captured = input.substring(captureStart, end + 1);
    } else {
      captureBuffer.addConcat(input.substring(captureStart, end + 1));
      captured = captureBuffer;
      captureBuffer = L_EMPTY;
    }
    captureStart = -1;
    return captured;
  }

  private ParseException expected(final AsrString expected) {
    if (isEndOfText()) {
      return error(L_ERROR);
    }
    return error(L_ERROR);
  }

  private ParseException error(final AsrString message) {
    return new ParseException(message.toJavaString(), index, line, column - 1);
  }

  private boolean isWhiteSpace() {
    return L_SPACE.equals(current) || L_ESCAPE_T.equals(current) || L_ESCAPE_N.equals(current) || L_ESCAPE_R.equals(current);
  }

  private boolean isDigit() {
    return L_0.equals(current) ||
        L_1.equals(current) ||
        L_2.equals(current) ||
        L_3.equals(current) ||
        L_4.equals(current) ||
        L_5.equals(current) ||
        L_6.equals(current) ||
        L_7.equals(current) ||
        L_8.equals(current) ||
        L_9.equals(current);
  }

  private boolean isEndOfText() {
    return current == null;
  }
}
