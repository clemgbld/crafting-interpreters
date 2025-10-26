#include "chunk.h"
#include "memory.h"
#include "value.h"
#include <stdio.h>
#include <stdlib.h>

void initLineArray(LineArray *lineArray) {
  lineArray->capacity = 0;
  lineArray->count = 0;
  lineArray->lines = NULL;
};

void initLine(Line *line, int number) {
  line->count = 1;
  line->number = number;
}

void writeLineArray(LineArray *lineArray, int line) {
  if (lineArray->count > 0 &&
      lineArray->lines[lineArray->count].number == line) {
    lineArray->lines[lineArray->count].count++;
    return;
  }
  if (lineArray->capacity < lineArray->count + 1) {
    int oldCapacity = lineArray->capacity;
    lineArray->capacity = GROW_CAPACITY(oldCapacity);
    lineArray->lines =
        GROW_ARRAY(Line, lineArray->lines, oldCapacity, lineArray->capacity);
  }
  Line lineObj;
  initLine(&lineObj, line);
  lineArray->lines[lineArray->count] = lineObj;
  lineArray->count++;
}

void freeLineArray(LineArray *lineArray) {
  FREE_ARRAY(uint8_t, lineArray->lines, lineArray->capacity);
  initLineArray(lineArray);
};

void initChunk(Chunk *chunk) {
  chunk->capacity = 0;
  chunk->count = 0;
  chunk->code = NULL;
  initValueArray(&chunk->constants);
  initLineArray(&chunk->lines);
}

int getLine(Chunk *chunk, int index) {
  int lineLength = 0;
  for (int i = 0; i < chunk->lines.count; i++) {
    Line currentLine = chunk->lines.lines[i];
    lineLength += currentLine.count;
    if (index <= lineLength - 1)
      return currentLine.number;
  }
  exit(1);
};

void writeChunk(Chunk *chunk, uint8_t byte, int line) {
  if (chunk->capacity < chunk->count + 1) {
    int oldCapacity = chunk->capacity;
    chunk->capacity = GROW_CAPACITY(oldCapacity);
    chunk->code =
        GROW_ARRAY(uint8_t, chunk->code, oldCapacity, chunk->capacity);
  }
  chunk->code[chunk->count] = byte;
  writeLineArray(&chunk->lines, line);
  chunk->count++;
}

void freeChunk(Chunk *chunk) {
  FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
  freeLineArray(&chunk->lines);
  freeValueArray(&chunk->constants);
  initChunk(chunk);
}

void writeConstant(Chunk *chunk, Value value, int line) {
  int constant = addConstant(chunk, value);
  if (constant > 255) {
    writeChunk(chunk, OP_CONSTANT_LONG, line);
    writeChunk(chunk, (constant >> 16), line);
    writeChunk(chunk, (constant >> 8), line);
    writeChunk(chunk, (constant & 255), line);
  } else {
    writeChunk(chunk, OP_CONSTANT, line);
    writeChunk(chunk, constant, line);
  }
}

int addConstant(Chunk *chunk, Value value) {
  writeValueArray(&chunk->constants, value);
  return chunk->constants.count - 1;
}
