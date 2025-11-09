#include "value.h"
#include "munit/munit.h"
#include <stdio.h>

static MunitResult can_push_and_pop_from_stack() {
  ValueArray valueArray;
  initValueArray(&valueArray);
  pushStack(&valueArray, 123.0);
  munit_assert_int(popStack(&valueArray), ==, 123.0);
  freeValueArray(&valueArray);
  return MUNIT_OK;
}

static MunitResult can_push_and_pop_multiple_values_and_pop_from_stack() {
  ValueArray valueArray;
  initValueArray(&valueArray);
  pushStack(&valueArray, 1.0);
  pushStack(&valueArray, 2.0);
  pushStack(&valueArray, 3.0);
  pushStack(&valueArray, 4.0);
  pushStack(&valueArray, 5.0);
  pushStack(&valueArray, 6.0);
  pushStack(&valueArray, 7.0);
  pushStack(&valueArray, 8.0);
  pushStack(&valueArray, 9.0);
  pushStack(&valueArray, 10.0);

  munit_assert_int(popStack(&valueArray), ==, 10.0);
  munit_assert_int(popStack(&valueArray), ==, 9.0);
  munit_assert_int(popStack(&valueArray), ==, 8.0);
  munit_assert_int(popStack(&valueArray), ==, 7.0);
  munit_assert_int(popStack(&valueArray), ==, 6.0);
  munit_assert_int(popStack(&valueArray), ==, 5.0);
  munit_assert_int(popStack(&valueArray), ==, 4.0);
  munit_assert_int(popStack(&valueArray), ==, 3.0);
  munit_assert_int(popStack(&valueArray), ==, 2.0);
  munit_assert_int(popStack(&valueArray), ==, 1.0);
  freeValueArray(&valueArray);
  return MUNIT_OK;
}

// Define test suite
static MunitTest tests[] = {
    {"/can push and pop from stack", can_push_and_pop_from_stack, NULL, NULL,
     MUNIT_TEST_OPTION_NONE, NULL},
    {"/can push and pop mutiple values from stack",
     can_push_and_pop_multiple_values_and_pop_from_stack, NULL, NULL,
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
