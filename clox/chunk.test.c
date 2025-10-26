#include "chunk.h"
#include "munit/munit.h"
#include <stdio.h>

/* getLine */
static MunitResult can_get_the_line_of_chunk() {
  Chunk chunk;
  initChunk(&chunk);
  writeChunk(&chunk, OP_RETURN, 123);
  munit_assert_int(getLine(&chunk, 0), ==, 123);
  freeChunk(&chunk);
  return MUNIT_OK;
}

static MunitResult can_get_any_line_of_chunk() {
  Chunk chunk;
  initChunk(&chunk);
  writeChunk(&chunk, OP_RETURN, 124);
  munit_assert_int(getLine(&chunk, 0), ==, 124);
  freeChunk(&chunk);
  return MUNIT_OK;
}

static MunitResult can_get_any_line_of_chunk_at_any_index() {
  Chunk chunk;
  initChunk(&chunk);
  writeChunk(&chunk, OP_RETURN, 124);
  writeChunk(&chunk, OP_RETURN, 200);
  writeChunk(&chunk, OP_RETURN, 200);

  munit_assert_int(getLine(&chunk, 0), ==, 124);
  munit_assert_int(getLine(&chunk, 1), ==, 200);
  munit_assert_int(getLine(&chunk, 2), ==, 200);
  freeChunk(&chunk);
  return MUNIT_OK;
}

/* writeConstant */
static MunitResult can_write_simple_constant() {
  Chunk chunk;
  initChunk(&chunk);

  writeConstant(&chunk, 1.2, 123);
  munit_assert_double(chunk.constants.values[chunk.code[chunk.count - 1]], ==,
                      1.2);
  return MUNIT_OK;
}

static MunitResult can_write_long_constant() {
  Chunk chunk;
  initChunk(&chunk);
  for (int i = 0; i <= 255; i++) {
    writeConstant(&chunk, 1.2, i + 1);
  }
  writeConstant(&chunk, 55.5, 257);
  int reconstructed = (chunk.code[chunk.count - 3] << 16) |
                      (chunk.code[chunk.count - 2] << 8) |
                      chunk.code[chunk.count - 1];
  munit_assert_double(chunk.constants.values[reconstructed], ==, 55.5);
  return MUNIT_OK;
}

// Define test suite
static MunitTest tests[] = {
    {"/can get the line of a chunk", can_get_the_line_of_chunk, NULL, NULL,
     MUNIT_TEST_OPTION_NONE, NULL},
    {"/can get any line of a chunk", can_get_any_line_of_chunk, NULL, NULL,
     MUNIT_TEST_OPTION_NONE, NULL},
    {"/can get any line of a chunk at any index",
     can_get_any_line_of_chunk_at_any_index, NULL, NULL, MUNIT_TEST_OPTION_NONE,
     NULL},
    {"/can write simple constant", can_write_simple_constant, NULL, NULL,
     MUNIT_TEST_OPTION_NONE, NULL},
    {"/can write long constant", can_write_long_constant, NULL, NULL,
     MUNIT_TEST_OPTION_NONE, NULL},
    {NULL, NULL, NULL, NULL, MUNIT_TEST_OPTION_NONE, NULL} // Sentinel
};

// Define MUnit suite
static const MunitSuite suite = {"/get_line_test_suite", tests, NULL, 1,
                                 MUNIT_SUITE_OPTION_NONE};

// Main function
int main(int argc, char *argv[]) {
  return munit_suite_main(&suite, NULL, argc, argv);
}
