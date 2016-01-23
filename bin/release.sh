#!/usr/bin/bash

cp -r resources/public/css release
python bin/generate_select.py -n 1000 -js reselect.min.js > release/example.html
