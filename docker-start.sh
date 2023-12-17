#!/bin/bash

gradle clean bootJar
docker build -t piotrhosa/tui-git .  --platform=linux/amd64
docker run -p 8080:8080 piotrhosa/tui-git
