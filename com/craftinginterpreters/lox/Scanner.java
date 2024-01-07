package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*; 

class Scanner {
  //store the raw souce code as string
  private final String source;
  //store the converted token in arrayList
  private final List<Token> tokens = new ArrayList<>();
  //use to keep track of where the scanner at
  private int start = 0;
  private int current = 0;
  private int line = 1;

  // Define set of reserve words in a map
  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }

  Scanner(String source) {
    this.source = source;
  }

  //keep adding token until EOF
  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    //take in a char and only consider length 1 lexeme for now
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break; 
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/':
        if (match('/')) {
          // a comment will have 2 slash, and we consume until the end
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(SLASH);
        }
        break;
      //ignoreed char for now
      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;

      case '\n':
        line++;
        break;

      case '"': string(); break;

      default: 
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          //anything start with a letter or underscore is an identifier
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }
  private void identifier() {
    //the scanner keep advancing while the characters are alphanumeric
    while (isAlphaNumeric(peek())) advance();
    
    // after scanning an identifier, check if it match any reserved words
    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    // Java Map return a null if key not found? wow
    // If not found, then it is just a user-defined identifier instead of reserve word
    if (type == null) type = IDENTIFIER;
    addToken(type);
  }

  //method that process number lexeme 
  private void number() {
    while (isDigit(peek())) advance();

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek())) advance();
    }

    //lox's runtime numeric value is double
    addToken(NUMBER,
        Double.parseDouble(source.substring(start, current)));
  }


  private void string() {
    //consume current character until it hits the end
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // The closing ".
    advance();

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  //match the next character, just like conditional advance
  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  //lookahead, but do not consume the current char, for comment case
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  // peek and return the next character
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  } 

  //identify if current char is letter or underscore
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }

  // Identify if current char is alphaNumeric
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  //identify if current char is a digit
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  } 

  //help us to know if we have consumed all the characters in a file
  private boolean isAtEnd() {
    return current >= source.length();
  }

  //advance one character in 'source' 
  private char advance() {
    return source.charAt(current++);
  }

  //overloading addToken, this is not literal
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  //this is for literal
  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    //add token type, text form ie string, literal object, and which line it is in?
    tokens.add(new Token(type, text, literal, line));
  }
}
