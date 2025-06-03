#!/bin/bash
# Set encoding to UTF-8 (usually default in modern Linux terminals)
export LANG=UTF-8

echo "如果沒有錯誤訊息，即可關閉"

# Compile Java files in the frontend, backend, and share directories
javac frontend/*.java backend/*.java share/*.java

# Optional: wait for user input before closing (only useful if run from GUI double-click)
read -p "Press enter to continue..."
