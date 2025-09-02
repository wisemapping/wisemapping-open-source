#!/bin/sh

# Check if SPRING_CONFIG_ADDITIONAL_FILE_CONTENT is empty
if [ -z "SPRING_CONFIG_ADDITIONAL_FILE_CONTENT" ]; then
    echo "ERROR: SPRING_CONFIG_ADDITIONAL_FILE_CONTENT environment variable is empty or not set. Using default local configuration." >&2
fi

# Write it to a file
echo "$SPRING_CONFIG_ADDITIONAL_FILE_CONTENT" > /app/app.yml