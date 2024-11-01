*** Settings ***
Documentation     This is a common resource file , which contains common keywords
Library           SeleniumLibrary
Variables         ../variables/common_variables.py

*** Keywords ***
Open Website
    Open Browser    ${FRONT_URL}    browser=${BROWSER}    
    Maximize Browser Window