# CHALLENGES 24

## 1

Reading and writing the ip field is one of the most frequent operations inside the bytecode loop. Right now, we access it through a pointer to the current CallFrame. That requires a pointer indirection which may force the CPU to bypass the cache and hit main memory. That can be a real performance sink.

Ideally, we’d keep the ip in a native CPU register. C doesn’t let us require that without dropping into inline assembly, but we can structure the code to encourage the compiler to make that optimization. If we store the ip directly in a C local variable and mark it register, there’s a good chance the C compiler will accede to our polite request.

This does mean we need to be careful to load and store the local ip back into the correct CallFrame when starting and ending function calls. Implement this optimization. Write a couple of benchmarks and see how it affects the performance. Do you think the extra code complexity is worth it?

In vm.c

```c
#include "vm.h"
#include "chunk.h"
#include "common.h"
#include "debug.h"
#include "memory.h"
#include "object.h"
#include "table.h"
#include "value.h"
#include <stdarg.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

VM vm;

static Value clockNative(int argCount, Value *args) {
  (void)argCount;
  (void)args;
  return NUMBER_VAL((double)clock() / CLOCKS_PER_SEC);
}

static void resetStack() {
  vm.stackTop = vm.stack;
  vm.frameCount = 0;
};

static void runtimeError(const char *format, ...) {
  va_list args;
  va_start(args, format);
  vfprintf(stderr, format, args);
  va_end(args);
  fputs("\n", stderr);
  for (int i = vm.frameCount - 1; i >= 0; i--) {
    CallFrame *frame = &vm.frames[i];
    ObjFunction *function = frame->function;
    size_t instruction = frame->ip - function->chunk.code - 1;
    fprintf(stderr, "[line %d] in ", function->chunk.lines[instruction]);
    if (function->name == NULL) {
      fprintf(stderr, "script\n");
    } else {
      fprintf(stderr, "%s()\n", function->name->chars);
    }
  }
  resetStack();
}

static void defineNative(const char *name, NativeFn function) {
  push(OBJ_VAL(copyString(name, (int)strlen(name))));
  push(OBJ_VAL(newNative(function)));
  tableSet(&vm.globals, AS_STRING(vm.stack[0]), vm.stack[1]);
  pop();
  pop();
}

static Value peek(int distance) { return vm.stackTop[-1 - distance]; }

static bool call(ObjFunction *function, int argCount) {
  if (argCount != function->arity) {
    runtimeError("Expected %d arguments but got %d.", function->arity,
                 argCount);
    return false;
  }

  if (vm.frameCount == FRAMES_MAX) {
    runtimeError("Stack overflow.");
    return false;
  }

  CallFrame *frame = &vm.frames[vm.frameCount++];
  frame->function = function;
  frame->slots = vm.stackTop - argCount - 1;
  return true;
}

static bool callValue(Value callee, int argCount) {
  if (IS_OBJ(callee)) {
    switch (OBJ_TYPE(callee)) {
    case OBJ_FUNCTION:
      return call(AS_FUNCTION(callee), argCount);
    case OBJ_NATIVE: {
      NativeFn native = AS_NATIVE(callee);
      Value result = native(argCount, vm.stackTop - argCount);
      vm.stackTop -= argCount + 1;
      push(result);
      return true;
    }
    default:
      break;
    }
  }
  runtimeError("Can only call functinos and classes");
  return false;
}

static bool isFalsey(Value value) {
  return IS_NIL(value) || (IS_BOOL(value) && !AS_BOOL(value));
}

static void concatenate() {
  ObjString *b = AS_STRING(pop());
  ObjString *a = AS_STRING(pop());

  int length = a->length + b->length;
  char *chars = ALLOCATE(char, length + 1);
  memcpy(chars, a->chars, a->length);
  memcpy(chars + a->length, b->chars, b->length);
  chars[length] = '\0';
  ObjString *result = takeString(chars, length);
  push(OBJ_VAL(result));
};

void push(Value value) {
  *vm.stackTop = value;
  vm.stackTop++;
};

Value pop() {
  vm.stackTop--;
  return *vm.stackTop;
}

void initVM() {
  resetStack();
  vm.objects = NULL;
  initTable(&vm.strings);
  initTable(&vm.globals);
  defineNative("clock", clockNative);
};

void freeVM() {
  freeObjects();
  freeTable(&vm.strings);
  freeTable(&vm.globals);
};

static InterpretResult run() {
  CallFrame *frame = &vm.frames[vm.frameCount - 1];
  register uint8_t *ip = frame->ip;

#define READ_BYTE() (*(ip++))
#define READ_SHORT() (ip += 2, (uint16_t)((ip[-2] << 8) | ip[-1]))
#define READ_CONSTANT() (frame->function->chunk.constants.values[READ_BYTE()])
#define READ_STRING() AS_STRING(READ_CONSTANT())
#define BINARY_OP(valueType, op)                                               \
  do {                                                                         \
    if (!IS_NUMBER(peek(0)) || !IS_NUMBER(peek(1))) {                          \
      frame->ip = ip;                                                          \
      runtimeError("Operands must be numbers.");                               \
      return INTERPRET_RUNTIME_ERROR;                                          \
    }                                                                          \
    double b = AS_NUMBER(pop());                                               \
    double a = AS_NUMBER(pop());                                               \
    push(valueType(a op b));                                                   \
  } while (false)

  for (;;) {
#ifdef DEBUG_TRACE_EXECUTION
    printf("        ");
    for (Value *slot = vm.stack; slot < vm.stackTop; slot++) {
      printf("[ ");
      printValue(*slot);
      printf(" ]");
    }
    printf("\n");
    dissassembleInstruction(&frame->function->chunk,
                            (int)(ip - frame->function->chunk.code));
#endif
    uint8_t instruction;
    switch (instruction = READ_BYTE()) {
    case OP_CONSTANT: {
      Value constant = READ_CONSTANT();
      push(constant);
      break;
    }
    case OP_NIL:
      push(NIL_VAL);
      break;
    case OP_TRUE:
      push(BOOL_VAL(true));
      break;
    case OP_FALSE:
      push(BOOL_VAL(false));
      break;
    case OP_POP:
      pop();
      break;
    case OP_GET_LOCAL: {
      uint8_t slot = READ_BYTE();
      push(frame->slots[slot]);
      break;
    }
    case OP_GET_GLOBAL: {
      ObjString *name = READ_STRING();
      Value value;
      if (!tableGet(&vm.globals, name, &value)) {
        frame->ip = ip;
        runtimeError("Undefined variable '%s'.", name->chars);
        return INTERPRET_RUNTIME_ERROR;
      }
      push(value);
      break;
    }
    case OP_DEFINE_GLOBAL: {
      ObjString *name = READ_STRING();
      tableSet(&vm.globals, name, peek(0));
      pop();
      break;
    }
    case OP_SET_LOCAL: {
      uint8_t slot = READ_BYTE();
      frame->slots[slot] = peek(0);
      break;
    }
    case OP_SET_GLOBAL: {
      ObjString *name = READ_STRING();
      if (tableSet(&vm.globals, name, peek(0))) {
        tableDelete(&vm.globals, name);
        frame->ip = ip;
        runtimeError("Undefined variable '%s'.", name->chars);
        return INTERPRET_RUNTIME_ERROR;
      }
      break;
    }
    case OP_EQUAL: {
      Value b = pop();
      Value a = pop();
      push(BOOL_VAL(valuesEqual(a, b)));
      break;
    }
    case OP_LESS: {
      BINARY_OP(BOOL_VAL, <);
      break;
    }
    case OP_GREATER: {
      BINARY_OP(BOOL_VAL, >);
      break;
    }
    case OP_SUBTRACT: {
      BINARY_OP(NUMBER_VAL, -);
      break;
    }
    case OP_ADD: {
      if (IS_STRING(peek(0)) && IS_STRING(peek(1))) {
        concatenate();
      } else if (IS_NUMBER(peek(0)) && IS_NUMBER(peek(1))) {
        double b = AS_NUMBER(pop());
        double a = AS_NUMBER(pop());
        push(NUMBER_VAL(a + b));
      } else {
        frame->ip = ip;
        runtimeError("Operands must be two numbers or two strings.");
        return INTERPRET_RUNTIME_ERROR;
      }
      break;
    }
    case OP_MULTIPLY: {
      BINARY_OP(NUMBER_VAL, *);
      break;
    }
    case OP_DIVIDE: {
      BINARY_OP(NUMBER_VAL, /);
      break;
    }
    case OP_NOT: {
      push(BOOL_VAL(isFalsey(pop())));
      break;
    }
    case OP_NEGATE: {
      if (!IS_NUMBER(peek(0))) {
        frame->ip = ip;
        runtimeError("Operand must be a number");
        return INTERPRET_RUNTIME_ERROR;
      }
      push(NUMBER_VAL(-AS_NUMBER(pop())));
      break;
    }
    case OP_PRINT: {
      printValue(pop());
      printf("\n");
      break;
    }
    case OP_JUMP: {
      uint16_t offset = READ_SHORT();
      ip += offset;
      break;
    }
    case OP_JUMP_IF_FALSE: {
      uint16_t offset = READ_SHORT();
      if (isFalsey(peek(0))) {
        ip += offset;
      }
      break;
    }
    case OP_LOOP: {
      uint16_t offset = READ_SHORT();
      ip -= offset;
      break;
    }
    case OP_CALL: {
      int argCount = READ_BYTE();
      frame->ip = ip;
      Value callee = peek(argCount);
      bool isFunction = IS_FUNCTION(callee);
      if (!callValue(callee, argCount)) {
        return INTERPRET_RUNTIME_ERROR;
      }
      frame = &vm.frames[vm.frameCount - 1];
      if (isFunction) {
        ip = frame->function->chunk.code;
      }
      break;
    }
    case OP_RETURN: {
      Value result = pop();
      vm.frameCount--;
      if (vm.frameCount == 0) {
        pop();
        return INTERPRET_OK;
      }
      vm.stackTop = frame->slots;
      push(result);
      frame = &vm.frames[vm.frameCount - 1];
      ip = frame->ip;
      break;
    }
    }
  }
#undef READ_BYTE
#undef READ_SHORT
#undef READ_CONSTANT
#undef READ_STRING
#undef BINARY_OP
}

InterpretResult interpret(const char *source) {
  ObjFunction *function = compile(source);
  if (function == NULL)
    return INTERPRET_COMPILE_ERROR;
  push(OBJ_VAL(function));
  CallFrame *frame = &vm.frames[vm.frameCount];
  call(function, 0);
  frame->ip = function->chunk.code;
  return run();
}
```

I choose this fib function for the benchmarks:

```lox
fun fib(n){
    if(n < 2) return 1;
    return fib(n - 1) + fib(n - 2);
  }

var start = clock();
print fib(20);
print clock() - start;
```



Without the optimization it takes  3.77343 seconds on my machine.

With the opitmization it takes 3.74629 on my machine.

So the optimization does not seems to worth it (the code is quite messy)

## 2

Native function calls are fast in part because we don’t validate that the call passes as many arguments as the function expects. We really should, or an incorrect call to a native function without enough arguments could cause the function to read uninitialized memory. Add arity checking.

I kept it dead simple.

In object.h

```c

#define AS_NATIVE(value) ((ObjNative *)AS_OBJ(value))

typedef struct {
  Obj obj;
  NativeFn function;
  int arity;
} ObjNative;


ObjNative *newNative(NativeFn function, int arity);

```


In object.c

```c
ObjNative *newNative(NativeFn function, int arity) {
  ObjNative *native = ALLOCATE_OBJ(ObjNative, OBJ_NATIVE);
  native->function = function;
  native->arity = arity;
  return native;
}

```

In vm.c

```c
static void defineNative(const char *name, NativeFn function, int arity) {
  push(OBJ_VAL(copyString(name, (int)strlen(name))));
  push(OBJ_VAL(newNative(function, arity)));
  tableSet(&vm.globals, AS_STRING(vm.stack[0]), vm.stack[1]);
  pop();
  pop();
}
// in initVM
  defineNative("clock", clockNative, 0);

// in callValue
case OBJ_NATIVE: {
      ObjNative *nativeFn = AS_NATIVE(callee);
      NativeFn native = nativeFn->function;
      if (nativeFn->arity != argCount) {
        runtimeError("Expected %d arguments but got %d.", nativeFn->arity,
                     argCount);
        return false;
      }
      Value result = native(argCount, vm.stackTop - argCount);
      vm.stackTop -= argCount + 1;
      push(result);
      return true;
    }

```

## 3
Right now, there’s no way for a native function to signal a runtime error. In a real implementation, this is something we’d need to support because native functions live in the statically typed world of C but are called from dynamically typed Lox land. If a user, say, tries to pass a string to sqrt(), that native function needs to report a runtime error.

Extend the native function system to support that. How does this capability affect the performance of native calls?

I kept it simple and used a hacky solution.

In value.h

```c

#define IS_VALUE_TYPE(value, expectedType) ((value).type == (expectedType))
```

In object.h

```c

#define IS_OBJ_TYPE(value, expectedType) isObjTypeLoose(value, (expectedType))

typedef struct {
  ValueType valueType;
  ObjType objType;
} ArgumentType;

#define ARGUMENT_TYPE_MAX 1

typedef struct {
  Obj obj;
  NativeFn function;
  int arity;
  ArgumentType argumentTypes[ARGUMENT_TYPE_MAX];
} ObjNative;

static inline bool isObjTypeLoose(Value value, ObjType type) {
  return isObjType(value, type) ||
         (IS_OBJ(value) &&
          ((AS_OBJ(value)->type == OBJ_FUNCTION && type == OBJ_NATIVE) ||
           (AS_OBJ(value)->type == OBJ_NATIVE && type == OBJ_FUNCTION)));
}
```

In object.c

```c
ObjNative *newNative(NativeFn function, int arity,
                     ArgumentType argumentTypes[]) {
  ObjNative *native = ALLOCATE_OBJ(ObjNative, OBJ_NATIVE);
  native->function = function;
  native->arity = arity;
  for (int i = 0; i < native->arity; i++) {
    native->argumentTypes[i] = argumentTypes[i];
  }
  return native;
}
```

Where most of the work happen.
In vm.c

```c
static void defineNative(const char *name, NativeFn function, int arity,
                         ArgumentType argumentTypes[]) {
  push(OBJ_VAL(copyString(name, (int)strlen(name))));
  push(OBJ_VAL(newNative(function, arity, argumentTypes)));
  tableSet(&vm.globals, AS_STRING(vm.stack[0]), vm.stack[1]);
  pop();
  pop();
}

static const char *objTypeToString(ObjType type) {
  switch (type) {
  case OBJ_FUNCTION:
    return "function";
  case OBJ_NATIVE:
    return "function";
  case OBJ_STRING:
    return "string";
  }
}

static const char *typeToString(ValueType valuetype, ObjType objType) {
  switch (valuetype) {
  case VAL_BOOL:
    return "boolean";
  case VAL_NUMBER:
    return "number";
  case VAL_NIL:
    return "nil";
  case VAL_OBJ:
    return objTypeToString(objType);
  }
}

static bool callValue(Value callee, int argCount) {
  if (IS_OBJ(callee)) {
    switch (OBJ_TYPE(callee)) {
    case OBJ_FUNCTION:
      return call(AS_FUNCTION(callee), argCount);
    case OBJ_NATIVE: {
      ObjNative *nativeFn = AS_NATIVE(callee);
      NativeFn native = nativeFn->function;
      if (nativeFn->arity != argCount) {
        runtimeError("Expected %d arguments but got %d.", nativeFn->arity,
                     argCount);
        return false;
      }
      Value *args = vm.stackTop - argCount;
      for (int i = 0; i < argCount; i++) {
        ValueType valueType = nativeFn->argumentTypes[i].valueType;
        if (!IS_VALUE_TYPE(args[i], valueType)) {
          runtimeError("wrong agrument type for argument %d expected %s got %s",
                       i + 1,
                       // dummy OBJ_STRING
                       typeToString(valueType, OBJ_STRING),
                       typeToString(args[i].type, OBJ_STRING));
          return false;
        }

        ObjType objType = nativeFn->argumentTypes[i].objType;
        if (IS_OBJ(args[i]) && !IS_OBJ_TYPE(args[i], objType)) {
          runtimeError("wrong agrument type for argument %d expected %s got %s",
                       i + 1, typeToString(valueType, objType),
                       typeToString(args[i].type, AS_OBJ(args[i])->type));

          return false;
        }
      }

      Value result = native(argCount, vm.stackTop - argCount);
      vm.stackTop -= argCount + 1;
      push(result);
      return true;
    }

    default:
      break;
    }
  }
  runtimeError("Can only call functinos and classes");
  return false;
}

void initVM() {
  resetStack();
  vm.objects = NULL;
  initTable(&vm.strings);
  initTable(&vm.globals);
  defineNative("clock", clockNative, 0, (ArgumentType[]){});
  // OBJ_STRING put as dummy
  defineNative("sqrt", sqrtNative, 1,
               (ArgumentType[]){{VAL_NUMBER, OBJ_STRING}});
  defineNative("readFile", readFileNative, 1,
               (ArgumentType[]){{VAL_OBJ, OBJ_STRING}});
};
```

This affects the performance at runtime because now before calling any native function we had the check for the arguments that which is O(n) where n is native function arity.

And it also affects the memory used because each ObjNative must now carry an array of struct of that contains the ValueType and the ObjType.
the array hajs a max of the native function with the max arguments which is 1 for now. so for each enum we add (number of arguments of the native function with which has the most arguments * (sizeOf(ValueType) + sizeOf(ObjType))).

In order to be less hacky i could have introduce an object type "OBJ_NONE" or something like that.
I studied afterwards how Lua does it and all the functions have they own type checking, the error checking is not centralized like mine so you have a bit more work to do each time you declare your native function but you save memory compared to my solution so it is probably the best of both world.

## 4

Add some more native functions to do things you find useful. Write some programs using those. What did you add? How do they affect the feel of the language and how practical it is?

I added some native functions in order to test my implementation of the third challenge.

```c
static Value sqrtNative(int argCount, Value *args) {
  (void)argCount;
  return NUMBER_VAL(sqrt(AS_NUMBER(*args)));
}

static Value readFileNative(int argCount, Value *args) {
  (void)argCount;
  char *path = AS_CSTRING(*args);
  FILE *file = fopen(path, "rb");
  if (file == NULL) {
    fprintf(stderr, "Could not open file \"%s\".\n", path);
    return NIL_VAL;
  }
  fseek(file, 0L, SEEK_END);
  size_t fileSize = ftell(file);
  rewind(file);
  char *buffer = (char *)malloc(fileSize + 1);
  if (buffer == NULL) {
    fprintf(stderr, "Not enough memory to read the file \"%s\".\n", path);
    return NIL_VAL;
  }
  size_t bytesRead = fread(buffer, sizeof(char), fileSize, file);
  if (bytesRead < fileSize) {
    fprintf(stderr, "Could not open file \"%s\".\n", path);
    return NIL_VAL;
  }
  buffer[bytesRead] = '\0';
  fclose(file);
  ObjString *result = copyString(buffer, fileSize);
  FREE(char, buffer);
  return OBJ_VAL(result);
}
```

It's pretty nice you can build good abstraction for example the readFileNative expose you a clean API to read a file without caring about the details of opening the file, figure out the size, closing the file and so on.
