#ifndef clox_chunk_h

#define clox_chunk_h

#include "common.h"
#include "value.h"

typedef enum { OP_RETURN, OP_CONSTANT, OP_CONSTANT_LONG } OpCode;

typedef struct {
  int count;
  int number;
} Line;

typedef struct {
  int count;
  int capacity;
  Line *lines;
} LineArray;

typedef struct {
  int count;
  int capacity;
  uint8_t *code;
  ValueArray constants;
  LineArray lines;
} Chunk;

void initChunk(Chunk *chunk);

void writeChunk(Chunk *chunk, uint8_t byte, int line);

void freeChunk(Chunk *chunk);

int addConstant(Chunk *chunk, Value value);

void initLineArray(LineArray *lineArray);

void freeLineArray(LineArray *lineArray);

void writeLineArray(LineArray *lineArray, int line);

void initLine(Line *line, int number);

int getLine(Chunk *chunk, int index);

void writeConstant(Chunk *chunk, Value value, int line);

#endif
