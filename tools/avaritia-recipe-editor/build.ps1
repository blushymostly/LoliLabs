param([string]$JavaBin='D:/Prism/java/java-runtime-gamma/bin')
$ErrorActionPreference='Stop'
$project=$PSScriptRoot
New-Item -ItemType Directory -Force -Path "$project/build/classes","$project/dist/lib" | Out-Null
Copy-Item -LiteralPath 'D:/Prism/libraries/com/google/code/gson/gson/2.10/gson-2.10.jar' -Destination "$project/dist/lib/gson.jar" -Force
$sources=Get-ChildItem "$project/src" -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName
$params=@('--release','17','-encoding','UTF-8','-cp',('"'+"$project/dist/lib/gson.jar"+'"'),'-d',('"'+"$project/build/classes"+'"'))+($sources | ForEach-Object {'"'+$_.Replace('\','/')+'"'})
$params=$params | ForEach-Object {$_.Replace('\','/')}
Set-Content "$project/build/compile.args" $params -Encoding utf8NoBOM
& "$JavaBin/javac.exe" ('@'+"$project/build/compile.args")
if($LASTEXITCODE){throw 'Editor compilation failed'}
Set-Content "$project/build/MANIFEST.MF" "Manifest-Version: 1.0`nMain-Class: tmr.editor.Editor`nClass-Path: lib/gson.jar`n" -Encoding ascii
& "$JavaBin/jar.exe" --create --file "$project/dist/avaritia-recipe-editor-1.0.0.jar" --manifest "$project/build/MANIFEST.MF" -C "$project/build/classes" .
if($LASTEXITCODE){throw 'Editor packaging failed'}
Write-Output "Built $project/dist/avaritia-recipe-editor-1.0.0.jar"

