@echo off
setlocal
for %%I in ("%~dp0..\..\..") do set "EDITOR_INSTANCE=%%~fI"
if not "%~1"=="" set "EDITOR_INSTANCE=%~1"
for %%I in ("%EDITOR_INSTANCE%\..\..") do set "EDITOR_PRISM=%%~fI"
set "EDITOR_JAVA=%EDITOR_PRISM%\java\java-runtime-gamma\bin\javaw.exe"
if not exist "%EDITOR_JAVA%" set "EDITOR_JAVA=javaw.exe"
start "" "%EDITOR_JAVA%" -Xmx1G -jar "%~dp0dist\avaritia-recipe-editor-1.0.0.jar" "%EDITOR_INSTANCE%"
endlocal
