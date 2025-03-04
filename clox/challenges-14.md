# Challenges 14

## 1 

Our encoding of line information is hilariously wasteful of memory. 
Given that a series of instructions often correspond to the same source line, a natural solution is something akin to run-length encoding of the line numbers.

Devise an encoding that compresses the line information for a series of instructions on the same line. 
Change writeChunk() to write this compressed form, and implement a getLine() function that, given the index of an instruction, determines the line where the instruction occurs.

Hint: It’s not necessary for getLine() to be particularly efficient. 
Since it is called only when a runtime error occurs, it is well off the critical path where performance matters.
