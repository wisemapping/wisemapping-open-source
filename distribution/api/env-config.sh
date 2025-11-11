#!/bin/sh

#
#    Copyright [2007-2025] [wisemapping]
#
#   Licensed under WiseMapping Public License, Version 1.0 (the "License").
#   It is basically the Apache License, Version 2.0 (the "License") plus the
#   "powered by wisemapping" text requirement on every single page;
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the license at
#
#       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

# Ensure config directory exists
mkdir -p /app/config

# Write Spring config file only if content is not empty
if [ -n "$SPRING_CONFIG_ADDITIONAL_FILE_CONTENT" ]; then
    echo "$SPRING_CONFIG_ADDITIONAL_FILE_CONTENT" > /app/config/application.yml
    echo "Spring config written to /app/config/application.yml"
    echo "Spring will load additional configuration from optional:file:/app/config/"
else
    echo "SPRING_CONFIG_ADDITIONAL_FILE_CONTENT is empty, skipping application.yml creation"
fi

# Write New Relic config file only if content is not empty
if [ -n "$NEW_RELIC_CONFIG_FILE_CONTENT" ]; then
    echo "$NEW_RELIC_CONFIG_FILE_CONTENT" > /app/newrelic.yml
    echo "New Relic config written to /app/newrelic.yml"
else
    echo "NEW_RELIC_CONFIG_FILE_CONTENT is empty, skipping newrelic.yml creation"
fi

# Check if custom instrumentation XML exists (copied during Docker build)
# New Relic looks for newrelic-extensions.xml in /app/extensions/ directory
# If XML config exists, assume New Relic should be enabled
NEW_RELIC_XML_FOUND=false
if [ -f /app/extensions/newrelic-extensions.xml ]; then
    NEW_RELIC_XML_FOUND=true
    echo "Custom instrumentation file found at /app/extensions/newrelic-extensions.xml"
fi

# Automatically set NEW_RELIC_OPTS if newrelic.jar exists and config is present
# If XML config file was found, assume New Relic should be enabled (even if config file is missing)
# This allows the agent to be loaded without manually setting NEW_RELIC_OPTS
if [ -f /app/newrelic.jar ]; then
    if [ -f /app/newrelic.yml ]; then
        # Both jar and config file exist - enable agent
        if [ -z "$NEW_RELIC_OPTS" ]; then
            export NEW_RELIC_OPTS="-javaagent:/app/newrelic.jar"
            echo "New Relic agent enabled: NEW_RELIC_OPTS set to -javaagent:/app/newrelic.jar"
        else
            echo "New Relic agent options already set: $NEW_RELIC_OPTS"
        fi
    elif [ "$NEW_RELIC_XML_FOUND" = "true" ]; then
        # XML config found but newrelic.yml missing - enable agent anyway (assume enabled if XML exists)
        if [ -z "$NEW_RELIC_OPTS" ]; then
            export NEW_RELIC_OPTS="-javaagent:/app/newrelic.jar"
            echo "New Relic agent enabled (XML config found): NEW_RELIC_OPTS set to -javaagent:/app/newrelic.jar"
            echo "Warning: newrelic.yml not found, but custom instrumentation XML exists. Agent will use default config."
        else
            echo "New Relic agent options already set: $NEW_RELIC_OPTS"
        fi
    else
        echo "Warning: newrelic.jar found but newrelic.yml is missing and no custom instrumentation XML found."
    fi
elif [ -f /app/newrelic.yml ] || [ "$NEW_RELIC_XML_FOUND" = "true" ]; then
    echo "Warning: New Relic configuration found but newrelic.jar is missing. New Relic agent will not be loaded."
fi
