# Compiler and flags
CC = gcc
CFLAGS = -Wall -Wextra -Werror -g -I./include
# For test files: add -I. so that "#include "munit/munit.h"" works (munit folder is in the root)
TEST_CFLAGS = -Wall -Wextra -Werror -g -I./include -I.
LDFLAGS = 

# Main source files
SRCS = memory.c value.c chunk.c debug.c main.c
OBJS = $(SRCS:.c=.o)
TARGET = main

# Test files: all files ending with .test.c
TEST_SRCS = $(wildcard *.test.c)
# Convert *.test.c into *.test.o
TEST_OBJS = $(patsubst %.test.c,%.test.o,$(TEST_SRCS))
TEST_EXEC = test_runner

# MUnit submodule is assumed to be in the root folder (i.e. there's a "munit" folder)

# Compile munit.c before test files
MUNIT_OBJ = munit/munit.o

# Default rule: build main program
all: deps $(TARGET)

# Ensure submodules are initialized
deps:
	git submodule init
	git submodule update

# Link main executable
$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) $(OBJS) -o $(TARGET) $(LDFLAGS)

# Rule to compile main source files (non-test)
%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

# Compile munit.c into an object file
munit/munit.o: munit/munit.c
	$(CC) $(CFLAGS) -Imunit -c $< -o $@

# Rule to compile test files: convert a .test.c file into a .test.o file
%.test.o: %.test.c
	$(CC) $(TEST_CFLAGS) -c $< -o $@

# Link all object files (including munit.o) into the test runner
$(TEST_EXEC): $(TEST_OBJS) $(MUNIT_OBJ)
	$(CC) $(TEST_CFLAGS) $(TEST_OBJS) $(MUNIT_OBJ) -o $(TEST_EXEC) $(LDFLAGS)

# Run tests
test: deps $(TEST_EXEC)
	./$(TEST_EXEC)

# Run the main program
run: $(TARGET)
	./$(TARGET)

# Clean up build artifacts
clean:
	rm -f $(OBJS) $(TARGET) $(TEST_OBJS) $(MUNIT_OBJ) $(TEST_EXEC)

