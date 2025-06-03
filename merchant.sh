#!/bin/bash
export LANG=UTF-8

# Run the Java program with UTF-8 encoding
java -Dfile.encoding=UTF-8 frontend.Merchant

# Pause to keep the terminal open (helpful if double-clicked in GUI)
read -p "按 Enter 鍵關閉視窗..."
