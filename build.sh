#!/bin/bash
ant clean build
ant -Dprofile=quality clean build
ant -Dprofile=prod clean build