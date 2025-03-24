#!/bin/bash

# Clean previous results

echo "Cleaning previous results..."
rm -rf bin
mkdir -p bin

# Compile Banking package and tests
echo "Compiling Java files..."
javac -d bin src/Banking/*.java src/tests/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful."
else
    echo "Compilation failed."
    exit 1
fi

# Run the Menu class
echo "Running the program..."
java -cp bin Banking.Menu

