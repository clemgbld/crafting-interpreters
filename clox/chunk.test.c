#include "munit/munit.h"

// Sample test function
static MunitResult test_example(const MunitParameter params[],
                                void *user_data) {
  (void)params;
  (void)user_data;
  munit_assert(1 + 1 == 2);
  return MUNIT_OK;
}

// Define test suite
static MunitTest tests[] = {
    {"/example", test_example, NULL, NULL, MUNIT_TEST_OPTION_NONE, NULL},
    {NULL, NULL, NULL, NULL, MUNIT_TEST_OPTION_NONE, NULL} // Sentinel
};

// Define MUnit suite
static const MunitSuite suite = {"/test_suite", tests, NULL, 1,
                                 MUNIT_SUITE_OPTION_NONE};

// Main function
int main(int argc, char *argv[]) {
  return munit_suite_main(&suite, NULL, argc, argv);
}
