# Compiler
CC = gcc
# Compiler flags
CFLAGS = -Wall -Wextra -Werror -g
# Source files
SRCS = memory.c chunk.c debug.c main.c
# Object files
OBJS = $(SRCS:.c=.o)
# Output executable name
TARGET = main 

# Default rule to compile everything
all: $(TARGET)

# Linking step
$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) $(OBJS) -o $(TARGET)

# Compile .c files into .o files
%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

# Clean up compiled files
clean:
	rm -f $(OBJS) $(TARGET)

# Run the program
run: $(TARGET)
	./$(TARGET)
