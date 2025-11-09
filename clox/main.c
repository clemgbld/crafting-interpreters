#include "chunk.h"
#include "vm.h"
#include <stdio.h>
#include <time.h>

int main() {
  double start = clock();
  initVM();
  Chunk chunk;
  initChunk(&chunk);

  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, addConstant(&chunk, 4), 123);
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, addConstant(&chunk, 3), 123);
  writeChunk(&chunk, OP_NEGATE, 123);
  writeChunk(&chunk, OP_ADD, 123);
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, addConstant(&chunk, 2), 123);
  writeChunk(&chunk, OP_NEGATE, 123);
  writeChunk(&chunk, OP_MULTIPLY, 123);
  writeChunk(&chunk, OP_RETURN, 123);

  interpret(&chunk);
  freeVM();
  freeChunk(&chunk);
  double end = clock();

  printf("time elapsed %f\n", end - start);

  return 0;
}
