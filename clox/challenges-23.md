# CHALLENGES 23

## 1

In addition to if statements, most C-family languages have a multi-way switch statement. Add one to clox. The grammar is:

switchStmt     → "switch" "(" expression ")"
                 "{" switchCase* defaultCase? "}" ;
switchCase     → "case" expression ":" statement* ;
defaultCase    → "default" ":" statement* ;
To execute a switch statement, first evaluate the parenthesized switch value expression. Then walk the cases. For each case, evaluate its value expression. If the case value is equal to the switch value, execute the statements under the case and then exit the switch statement. Otherwise, try the next case. If no case matches and there is a default clause, execute its statements.

To keep things simpler, we’re omitting fallthrough and break statements. Each case automatically jumps to the end of the switch statement after its statements are done.

in scanner.h

```c
typedef enum {
  // Single-character tokens.
  TOKEN_LEFT_PAREN,
  TOKEN_RIGHT_PAREN,
  TOKEN_LEFT_BRACE,
  TOKEN_RIGHT_BRACE,
  TOKEN_COMMA,
  TOKEN_DOT,
  TOKEN_MINUS,
  TOKEN_PLUS,
  TOKEN_COLON,
  TOKEN_SEMICOLON,
  TOKEN_SLASH,
  TOKEN_STAR,
  // One or two character tokens.
  TOKEN_BANG,
  TOKEN_BANG_EQUAL,
  TOKEN_EQUAL,
  TOKEN_EQUAL_EQUAL,
  TOKEN_GREATER,
  TOKEN_GREATER_EQUAL,
  TOKEN_LESS,
  TOKEN_LESS_EQUAL,
  // Literals.
  TOKEN_IDENTIFIER,
  TOKEN_STRING,
  TOKEN_NUMBER,
  // Keywords.
  TOKEN_AND,
  TOKEN_CLASS,
  TOKEN_ELSE,
  TOKEN_FALSE,
  TOKEN_FOR,
  TOKEN_FUN,
  TOKEN_IF,
  TOKEN_NIL,
  TOKEN_OR,
  TOKEN_PRINT,
  TOKEN_RETURN,
  TOKEN_SUPER,
  TOKEN_THIS,
  TOKEN_TRUE,
  TOKEN_VAR,
  TOKEN_WHILE,
  TOKEN_SWITCH,
  TOKEN_CASE,
  TOKEN_DEFAULT,

  TOKEN_ERROR,
  TOKEN_EOF
} TokenType;

```

In scanner.c


```c

// scanToken
  case ':':
    return makeToken(TOKEN_COLON);

// identifierType

  case 'c':
    if (scanner.current - scanner.start > 1) {
      switch (scanner.start[1]) {
      case 'l':
        return checkKeyword(2, 3, "ass", TOKEN_CLASS);
      case 'a':
        return checkKeyword(2, 2, "se", TOKEN_CASE);
      }
    }
    break;
case 'd':
    return checkKeyword(1, 6, "efault", TOKEN_DEFAULT);

case 's':
    if (scanner.current - scanner.start > 1) {
      switch (scanner.start[1]) {
      case 'u':
        return checkKeyword(2, 3, "per", TOKEN_SUPER);
      case 'w':
        return checkKeyword(2, 4, "itch", TOKEN_SWITCH);
      }
    }
    break;
 
```
```
```

In compiler.c

```c
#define MAX_CASES 256

static void switchStatement() {
  consume(TOKEN_LEFT_PAREN, "Expect '(' after switch.");
  expression();
  consume(TOKEN_RIGHT_PAREN, "Expect ')' after condition.");
  consume(TOKEN_LEFT_BRACE, "Expect '{' after before switch cases.");

  int state = 0; // 0: brefore all cases, 1: before default, 2: after default
  int caseEnds[MAX_CASES];
  int caseCount = 0;
  int previousCaseSkip = -1;

  while (!match(TOKEN_RIGHT_BRACE) && !check(TOKEN_EOF)) {
    if (match(TOKEN_CASE) || match(TOKEN_DEFAULT)) {
      TokenType caseType = parser.previous.type;

      if (state == 2) {
        error("Can't have another case or default after the default case.");
      }

      if (state == 1) {
        // At the end of the previous case, jump over the others
        caseEnds[caseCount++] = emitJump(OP_JUMP);

        // Patch its condition to jump to the next case (this one)
        patchJump(previousCaseSkip);
        emitByte(OP_POP);
      }

      if (caseType == TOKEN_CASE) {
        state = 1;

        // See if the case is equal to the value
        emitByte(OP_DUP);
        expression();
        consume(TOKEN_COLON, "Expect ':' after case value.");
        emitByte(OP_EQUAL);

        previousCaseSkip = emitJump(OP_JUMP_IF_FALSE);

        // Pop the comparison result.
        emitByte(OP_POP);
      } else {
        state = 2;
        consume(TOKEN_COLON, "Expect ':' after default");
        previousCaseSkip = -1;
      }
    } else {
      // Otherwise, it's a statement inside the current case.
      if (state == 0) {
        error("Can't have statements");
      }
      statement();
    }
  }

  // If we ended without a default case, path its condition jump.
  if (state == 1) {
    patchJump(previousCaseSkip);
    emitByte(OP_POP);
  }

  // Patch all the case jumps to the end.
  for (int i = 0; i < caseCount; i++) {
    patchJump(caseEnds[i]);
  }

  emitByte(OP_POP); // the switch value.
}

// in statement

else if (match(TOKEN_SWITCH)) {
    switchStatement();
  }

// in rules

[TOKEN_COLON] = {NULL, NULL, PREC_NONE},
[TOKEN_SWITCH] = {NULL, NULL, PREC_NONE},
[TOKEN_CASE] = {NULL, NULL, PREC_NONE},
[TOKEN_DEFAULT] = {NULL, NULL, PREC_NONE},
```

In vm.c

```c
// in run
case OP_DUP:
      push(peek(0));
      break;

```

In debug.c

```c
case OP_DUP:
    return simpleInstruction("OP_DUP", offset);
```

## 2

In jlox, we had a challenge to add support for break statements. This time, let’s do continue:

continueStmt   → "continue" ";" ;
A continue statement jumps directly to the top of the nearest enclosing loop, skipping the rest of the loop body. Inside a for loop, a continue jumps to the increment clause, if there is one. It’s a compile-time error to have a continue statement not enclosed in a loop.

Make sure to think about scope. 
What should happen to local variables declared inside the body of the loop or in blocks nested inside the loop when a continue is executed?

We should clear the local variables who are greater than the depth of the inner most loop scope depth.

In scanner.h

```c
TOKEN_CONTINUE,
```

In scanner.c

```c
// in identifierType
if (scanner.current - scanner.start > 1) {
      switch (scanner.start[1]) {
case 'l':
        return checkKeyword(2, 3, "ass", TOKEN_CLASS);
case 'o':
        return checkKeyword(2, 6, "ntinue", TOKEN_CONTINUE);
      }
}
break;
```

In compiler.c

```c
// as global variables
int innermostLoopStart = -1;
int innermostLoopScopeDepth = 0;

static void forStatement() {
  beginScope();

  consume(TOKEN_LEFT_PAREN, "Expect '(' after 'for'.");
  if (match(TOKEN_VAR)) {
    varDeclaration();
  } else if (match(TOKEN_SEMICOLON)) {
    // No initializer.
  } else {
    expressionStatement();
  }

  int surroundingLoopStart = innermostLoopStart;
  int surroundingLoopScopeDepth = innermostLoopScopeDepth;
  innermostLoopStart = currentChunk()->count;
  innermostLoopScopeDepth = current->scopeDepth;

  int exitJump = -1;
  if (!match(TOKEN_SEMICOLON)) {
    expression();
    consume(TOKEN_SEMICOLON, "Expect ';' after loop condition.");

    // Jump out of the loop if the condition is false.
    exitJump = emitJump(OP_JUMP_IF_FALSE);
    emitByte(OP_POP); // Condition.
  }

  if (!match(TOKEN_RIGHT_PAREN)) {
    int bodyJump = emitJump(OP_JUMP);

    int incrementStart = currentChunk()->count;
    expression();
    emitByte(OP_POP);
    consume(TOKEN_RIGHT_PAREN, "Expect ')' after for clauses.");

    emitLoop(innermostLoopStart);
    innermostLoopStart = incrementStart;
    patchJump(bodyJump);
  }

  statement();

  emitLoop(innermostLoopStart);

  if (exitJump != -1) {
    patchJump(exitJump);
    emitByte(OP_POP); // Condition.
  }

  innermostLoopStart = surroundingLoopStart;
  innermostLoopScopeDepth = surroundingLoopScopeDepth;

  endScope();
}

static void whileStatement() {
  int surroundingLoopStart = innermostLoopStart;
  int surroundingLoopScopeDepth = innermostLoopScopeDepth;
  innermostLoopStart = currentChunk()->count;
  innermostLoopScopeDepth = current->scopeDepth;
  consume(TOKEN_LEFT_PAREN, "Expect '(' after 'while'.");
  expression();
  consume(TOKEN_RIGHT_PAREN, "Expect ')' after while clauses.");

  int exitJump = emitJump(OP_JUMP_IF_FALSE);
  emitByte(OP_POP);
  statement();
  emitLoop(innermostLoopStart);

  patchJump(exitJump);
  emitByte(OP_POP);

  innermostLoopStart = surroundingLoopStart;
  innermostLoopScopeDepth = surroundingLoopScopeDepth;
}

static void continueStatement() {
  if (innermostLoopStart == -1) {
    error("Can't use 'continue' outside a loop");
  }

  consume(TOKEN_SEMICOLON, "Expect ';' after 'continue'");

  // Discard any locals created inside the loop
  for (int i = current->localCount - 1;
       i >= 0 && current->locals[i].depth > innermostLoopScopeDepth; i--) {
    emitByte(OP_POP);
  }

  emitLoop(innermostLoopStart);
}

// in statement
  } else if (match(TOKEN_CONTINUE)) {
    continueStatement();
}

static void continueStatement() {
  if (innermostLoopStart == -1) {
    error("Can't use 'continue' outside of a loop.");
  }

  consume(TOKEN_SEMICOLON, "Expect ';' after 'continue'.");

  // Discard any locals created inside the loop.
  for (int i = current->localCount - 1;
       i >= 0 && current->locals[i].depth > innermostLoopScopeDepth;
       i--) {
    emitByte(OP_POP);
  }

  // Jump to top of current innermost loop.
  emitLoop(innermostLoopStart);
}

/// in rules
[TOKEN_CONTINUE] = {NULL, NULL, PREC_NONE},
```


## 3
Control flow constructs have been mostly unchanged since Algol 68. Language evolution since then has focused on making code more declarative and high level, so imperative control flow hasn’t gotten much attention.

For fun, try to invent a useful novel control flow feature for Lox. It can be a refinement of an existing form or something entirely new. In practice, it’s hard to come up with something useful enough at this low expressiveness level to outweigh the cost of forcing a user to learn an unfamiliar notation and behavior, but it’s a good chance to practice your design skills.

like in the design note it is sometime more elegant to breakout of the loop with a goto

In c without goto
```c

bool found = false;
for (int x = 0; x < xSize; x++) {
  for (int y = 0; y < ySize; y++) {
    for (int z = 0; z < zSize; z++) {
      if (matrix[x][y][z] == 0) {
        printf("found");
        found = true;
        break;
      }
    }
    if (found) break;
  }
  if (found) break;
}
```

In c with goto

```c
for (int x = 0; x < xSize; x++) {
  for (int y = 0; y < ySize; y++) {
    for (int z = 0; z < zSize; z++) {
      if (matrix[x][y][z] == 0) {
        printf("found");
        goto done;
      }
    }
  }
}
done:
```

A 'breakAll' keyword would be nice neat

```lox
for (int x = 0; x < xSize; x++) {
  for (int y = 0; y < ySize; y++) {
    for (int z = 0; z < zSize; z++) {
      if (x == 10 and y == 10 and z == 10) {
        printf("found");
        breakAll;
      }
    }
  }
}
```

And maybe adding the number of loop you want to break as an optional expression next to break would be a nice feature as well.

Even though checking that the number is correct can be a chore.
(After writing that answer i found out that php really implemented this)

```lox
for (int x = 0; x < xSize; x++) {
  for (int y = 0; y < ySize; y++) {
    for (int z = 0; z < zSize; z++) {
      if (x == 10 and y == 10 and z == 10) {
        printf("found");
        break 3;
      }
    }
  }
}
```
