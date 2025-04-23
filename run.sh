#!/usr/bin/env bash
#
# Usage: ./run.sh [optional args passed straight to BankingApp]
#

# Clean previous results
echo "Cleaning previous results..."
rm -rf bin
mkdir -p bin

# Compile packages
echo "Compiling Java resources..."
javac -cp "src:lib/*" -d bin src/banking/*.java src/tests/*.java
if [[ $? -ne 0 ]]; then
    echo "Compilation failed."
    exit 1
fi
echo "Compilation successful."

# Run all tests with JUnit 5
echo "Running tests..."
java -jar lib/junit-platform-console-standalone-1.12.0.jar --class-path bin --scan-classpath
if [[ $? -ne 0 ]]; then
    echo "Tests failed."
    exit 1
fi
echo "Tests ran successfully."

# Run the application, forwarding any arguments supplied to this script
echo "Running the program with autoApproveLoan on and default admin account..."
java -cp "bin:lib/*" banking.BankingApp "$@"