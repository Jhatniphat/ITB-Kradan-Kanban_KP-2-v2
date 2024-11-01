*** Settings ***
Library           SeleniumLibrary
Resource       ../resource/login.resource
Resource       ../resource/common.resource
Test Teardown    Close All Browsers
Task Tags    permanent_testing

*** Test Cases ***
normal_login
    [Documentation]    This is a test case for normal login
    Open Website
    Login User    ${USER_1}
    Wait Until Location Contains    board    5s

no_password_login
    [Documentation]    login without password -> Button should be disabled
    Open Website
    Login User    ${USER_EMPTY_PASSWORD}    BUTTON_SHOULD_BE_ENABLED=FALSE

no_username_login
    [Documentation]    login without username -> Button should be disabled
    Open Website
    Login User    ${USER_EMPTY_USERNAME}    BUTTON_SHOULD_BE_ENABLED=FALSE

user_not_registered
    [Documentation]    This is a test case for login with user not registered
    Open Website
    Login User    ${USER_NOT_REGISTERED}
    Login Error Message Should Be Contain    error_message=Username or Password is incorrect
