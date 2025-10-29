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
    echo "$SPRING_CONFIG_ADDITIONAL_FILE_CONTENT" > /app/config/app.yml
    echo "Spring config written to /app/config/app.yml"
    echo "Spring will load additional configuration from optional:file:/app/config/"
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
