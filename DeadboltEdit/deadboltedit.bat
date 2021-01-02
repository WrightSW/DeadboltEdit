@ECHO OFF

REM Start-up script for DeadboltEdit
REM
REM  When passing file names containing spaces to this script,
REM  add escaped quotes around them, e.g.
REM  "\"C:/My Directory/My File.txt\""

REM The quoting is to permit directory names with embedded quotes.

SET "APP_ROOT=%~dp0"

java -jar "%APP_ROOT%\DeadboltEdit.jar %*"
