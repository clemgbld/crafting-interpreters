#include "chunk.h"
#include "munit/munit.h"

// Sample test function
static MunitResult can_get_the_line_of_chunk() {
  Chunk chunk;
  initChunk(&chunk);
  writeChunk(&chunk, OP_RETURN, 123);
  munit_assert(getLine(&chunk, 0) == 123);
  freeChunk(&chunk);
  return MUNIT_OK;
}

// Define test suite
static MunitTest tests[] = {
    {"/can get teh line of a chunk", can_get_the_line_of_chunk, NULL, NULL,
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
