#!/bin/bash
git pull && ./gradlew build && screen -X -S bingo quit && screen -dmS bingo java -jar build/libs/th-bingo-1.0.0.jar && screen -ls