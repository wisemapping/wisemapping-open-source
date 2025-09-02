#!/bin/sh

# Write Spring config file only if content is not empty
if [ -n "$SPRING_CONFIG_ADDITIONAL_FILE_CONTENT" ]; then
    echo "$SPRING_CONFIG_ADDITIONAL_FILE_CONTENT" > /app/app.yml
    echo "Spring config written to /app/app.yml"
else
    echo "SPRING_CONFIG_ADDITIONAL_FILE_CONTENT is empty, skipping app.yml creation"
fi

# Write New Relic config file only if content is not empty
if [ -n "$NEW_RELIC_CONFIG_FILE_CONTENT" ]; then
    echo "$NEW_RELIC_CONFIG_FILE_CONTENT" > /app/newrelic.yml
    echo "New Relic config written to /app/newrelic.yml"
else
    echo "NEW_RELIC_CONFIG_FILE_CONTENT is empty, skipping newrelic.yml creation"
fi
