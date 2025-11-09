#include "vm.h"
#include "chunk.h"
#include "common.h"
#include "debug.h"
#include "value.h"
#include <stdio.h>

VM vm;

void initVM() { initValueArray(&vm.stack); };

void freeVM() { freeValueArray(&vm.stack); };

static InterpretResult run() {
#define READ_BYTE() (*vm.ip++)
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
#define BINARY_OP(op)                                                          \
  do {                                                                         \
    double b = *(vm.stack.stackTop - 1);                                       \
    double a = *(vm.stack.stackTop - 2);                                       \
    *(vm.stack.stackTop - 2) = (a op b);                                       \
    vm.stack.stackTop--;                                                       \
  } while (false);

  for (;;) {
#ifdef DEBUG_TRACE_EXECUTION
    printf("        ");
    for (Value *slot = vm.stack.values; slot < vm.stack.stackTop; slot++) {
      printf("[ ");
      printValue(*slot);
      printf(" ]");
    }
    printf("\n");
    dissassembleInstruction(vm.chunk, (int)(vm.ip - vm.chunk->code));
#endif
    uint8_t instruction;
    switch (instruction = READ_BYTE()) {
    case OP_CONSTANT: {
      Value constant = READ_CONSTANT();
      pushStack(&vm.stack, constant);
      break;
    }
    case OP_SUBSTRACT: {
      BINARY_OP(-);
      break;
    }
    case OP_ADD: {
      BINARY_OP(+);
      break;
    }
    case OP_MULTIPLY: {
      BINARY_OP(*);
      break;
    }
    case OP_DIVIDE: {
      BINARY_OP(/);
      break;
    }
    case OP_NEGATE: {
      *(vm.stack.stackTop - 1) = -*(vm.stack.stackTop - 1);
      break;
    }
    case OP_RETURN: {
      printValue(popStack(&vm.stack));
      printf("\n");
      return INTERPRET_OK;
    }
    }
  }
#undef READ_BYTE
#undef READ_CONSTANT
#undef BINARY_OP
}

InterpretResult interpret(Chunk *chunk) {
  vm.chunk = chunk;
  vm.ip = vm.chunk->code;
  return run();
}
