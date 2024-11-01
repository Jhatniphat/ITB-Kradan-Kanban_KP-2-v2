*** Settings ***
Library           SeleniumLibrary
Variables         ../variables/user_variables.py
Variables         ../variables/common_variables.py

*** Keywords ***
Login User
    Go To           ${LOGIN_URL}
    [Arguments]    ${USER}=${USER_1}    ${BUTTON_SHOULD_BE_ENABLED}=${True}
    
    ${username}=    Set Variable    ${USER.username}
    ${password}=    Set Variable    ${USER.password}    

    Input Text      class:itbkk-username    ${username}
    Input Text      class:itbkk-password    ${password}
    IF    $BUTTON_SHOULD_BE_ENABLED == $True
        Element Should Be Enabled    class:itbkk-button-signin
        Click Button    class:itbkk-button-signin
    ELSE
        Element Should Be Disabled    class:itbkk-button-signin
    END

Login Error Message Should Be Contain
    [Arguments]    ${error_message}
    Wait Until Element Is Visible    class:itbkk-message
    Element Should Contain    class:itbkk-message    ${error_message}
