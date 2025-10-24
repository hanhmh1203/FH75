#!/bin/bash

# AndroidX Migration Script
echo "Starting AndroidX migration..."

# Define the workspace path
WORKSPACE_PATH="/Users/william/Documents/f75/FH-75 handheld reader/SDK/二合一（带保存）android studio版/FH75Demo"

# Find all Java files and replace android.support imports with androidx
find "$WORKSPACE_PATH/app/src" -name "*.java" -type f -exec sed -i '' \
    -e 's/import android\.support\.v4\.app\.Fragment;/import androidx.fragment.app.Fragment;/g' \
    -e 's/import android\.support\.v4\.app\.FragmentActivity;/import androidx.fragment.app.FragmentActivity;/g' \
    -e 's/import android\.support\.v4\.app\.FragmentTabHost;/import androidx.fragment.app.FragmentTabHost;/g' \
    -e 's/import android\.support\.v4\.app\.DialogFragment;/import androidx.fragment.app.DialogFragment;/g' \
    -e 's/import android\.support\.v4\.app\.FragmentManager;/import androidx.fragment.app.FragmentManager;/g' \
    {} \;

echo "AndroidX migration completed!"
echo "Updated imports in Java files to use androidx packages."