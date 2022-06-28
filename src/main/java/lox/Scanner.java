package lox;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 0;

    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isEndAt()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isEndAt(){
        return current >=source.length();
    }


    private void scanToken(){
        char c = nextToken();
        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '*' -> addToken(TokenType.STAR);
        }
        return;
    }

    private char nextToken(){
       return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }


    private void addToken(TokenType type,Object literal){
        String text = source.substring(start,current);
        tokens.add(new Token(type,text,literal,line));
    }



}
