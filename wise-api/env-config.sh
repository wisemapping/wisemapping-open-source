#!/bin/sh

# Check if SPRING_APP_CONFIG_VALUE is empty
if [ -z "$SPRING_APP_CONFIG_VALUE" ]; then
    echo "ERROR: SPRING_APP_CONFIG_VALUE environment variable is empty or not set. Using default local configuration." >&2
fi

# Write it to a file
echo "$SPRING_APP_CONFIG_VALUE" > /app/app.yml