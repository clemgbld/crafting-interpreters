#include "chunk.h"
#include "common.h"
#include "debug.h"

int main() {
  Chunk chunk;
  initChunk(&chunk);
  writeChunk(&chunk, OP_RETURN, 123);
  for (int i = 0; i <= 255; i++) {
    writeConstant(&chunk, 1.2, i + 1);
  }
  writeConstant(&chunk, 55.5, 257);
  disassembleChunk(&chunk, "test chunk");

  freeChunk(&chunk);
  return 0;
}
