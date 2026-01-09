# CHALLENGES 27

## 1

Trying to access a non-existent field on an object immediately aborts the entire VM.
The user has no way to recover from this runtime error, nor is there any way to see if a field exists before trying to access it. It’s up to the user to ensure on their own that only valid fields are read.

How do other dynamically typed languages handle missing fields? What do you think Lox should do? Implement your solution.

I would say that the simplest thing to do would be to do like Javascript if a field does not exist in an object it is undefined and it that way to verify if a field exist you check if it is undefined or not.
in Lox a way to achieve this would be to just return nil by default if there is no value in the field.

We can implement it like that:

In vm.c

```c
case OP_GET_PROPERTY: {
      if (!IS_INSTANCE(peek(0))) {
        runtimeError("Only instances have ");
        return INTERPRET_RUNTIME_ERROR;
      }
      ObjInstance *instance = AS_INSTANCE(peek(0));
      ObjString *name = READ_STRING();
      Value value;
      pop();
      if (tableGet(&instance->fields, name, &value)) {
        push(value);
      } else {
        push(NIL_VAL);
      }
      break;
    }
```

## 2

Fields are accessed at runtime by their string name. But that name must always appear directly in the source code as an identifier token. A user program cannot imperatively build a string value and then use that as the name of a field. Do you think they should be able to? 

Yes i think they should be able to, because this type of feature is exactly the point of having dynamic language in the first place.


Devise a language feature that enables that and implement it.

I decided to implement it the javascript way.

```lox
class Pair {}

var pair = Pair();
pair["fi" + "rst" ] = 1;
pair["se" + "ond"] = 2;
print pair["fi" + "rst" ] + pair["se" + "ond"]; // 3
```



In scanner.h

```c
TOKEN_LEFT_SQUARE,
TOKEN_RIGHT_SQUARE,
```

In scanner.c

```c
Token scanToken() {
  skipWhitespace();
  scanner.start = scanner.current;
  if (isAtEnd())
    return makeToken(TOKEN_EOF);

  char c = advance();

  if (isAlpha(c)) {
    return identifier();
  }

  if (isDigit(c))
    return number();

  switch (c) {
  case '[':
    return makeToken(TOKEN_LEFT_SQUARE);
  case ']':
    return makeToken(TOKEN_RIGHT_SQUARE);
```

In compiler.c

```c
static void squareBracket(bool canAssign) {
  expression();
  consume(TOKEN_RIGHT_SQUARE, "Expect ']' after expression.");
  if (canAssign && match(TOKEN_EQUAL)) {
    expression();
    emitByte(OP_SET_COMPUTED_PROPERTY);
  } else {
    emitByte(OP_GET_COMPUTED_PROPERTY);
  }
}

ParseRule rules[] = {
    [TOKEN_LEFT_SQUARE] = {NULL, squareBracket, PREC_CALL},
    [TOKEN_RIGHT_SQUARE] = {NULL, NULL, PREC_NONE},
```

In chunk.h

```c
  OP_GET_COMPUTED_PROPERTY,
  OP_SET_COMPUTED_PROPERTY,
```

In debug.c

```c
  case OP_GET_COMPUTED_PROPERTY:
    return simpleInstruction("OP_GET_COMPUTED_PROPERTY", offset);
  case OP_SET_COMPUTED_PROPERTY:
    return simpleInstruction("OP_SET_COMPUTED_PROPERTY", offset);

```

In vm.c

```c
case OP_GET_COMPUTED_PROPERTY: {
      if (!IS_INSTANCE(peek(1))) {
        runtimeError("Only instances have ");
        return INTERPRET_RUNTIME_ERROR;
      }
      if (!IS_STRING(peek(0))) {
        runtimeError("Only string can be a field ");
        return INTERPRET_RUNTIME_ERROR;
      }

      ObjInstance *instance = AS_INSTANCE(peek(1));
      ObjString *name = AS_STRING(peek(0));
      Value value;
      if (tableGet(&instance->fields, name, &value)) {
        pop();
        pop();
        push(value);
        break;
      }
      runtimeError("Undefined property '%s'", name->chars);
      return INTERPRET_RUNTIME_ERROR;
    }

    case OP_SET_COMPUTED_PROPERTY: {
      if (!IS_INSTANCE(peek(2))) {
        runtimeError("Only instances have ");
        return INTERPRET_RUNTIME_ERROR;
      }
      if (!IS_STRING(peek(1))) {
        runtimeError("Only string can be a field ");
        return INTERPRET_RUNTIME_ERROR;
      }
      ObjInstance *instance = AS_INSTANCE(peek(2));
      ObjString *name = AS_STRING(peek(1));
      tableSet(&instance->fields, name, peek(0));
      Value value = pop();
      pop();
      pop();
      push(value);
      break;
    }
```
