param([string]$LoliProject='D:/Prism/instances/TMR-dev/loli-labs',[string]$Fixtures="$PSScriptRoot/build/native-fixtures/fixtures")
$ErrorActionPreference='Stop'
$java='D:/Prism/java/java-runtime-gamma/bin'
if(!(Test-Path -LiteralPath $Fixtures)){
    & "$java/java.exe" '-Djava.awt.headless=true' -jar "$PSScriptRoot/dist/avaritia-recipe-editor-1.0.0.jar" --self-test (Split-Path $Fixtures -Parent)
    if($LASTEXITCODE){throw 'Fixture generation failed'}
}
$runtime=Join-Path $PSScriptRoot 'build/native-check'
New-Item -ItemType Directory -Force -Path "$runtime/mods","$runtime/check-classes" | Out-Null
# Reuse the already verified isolated Forge launcher and dependencies, without changing installed JARs.
$previous=Join-Path $LoliProject 'build/runtime-check-1.4.4'
Copy-Item "$previous/mods/*.jar" "$runtime/mods" -Force
$cpFile=Join-Path $LoliProject 'build/loli-labs-1.4.4/javac.args'
$compileArgs=Get-Content -Raw -LiteralPath $cpFile
$match=[regex]::Match($compileArgs,'(?s)-classpath\s+"([^"]+)"')
if(!$match.Success){throw 'Cannot find local Forge classpath'}
& "$java/javac.exe" --release 17 -proc:none -classpath $match.Groups[1].Value -d "$runtime/check-classes" "$PSScriptRoot/checks/EditorRecipeChecks.java"
if($LASTEXITCODE){throw 'Native check compilation failed'}
& "$java/jar.exe" --update --file "$runtime/mods/loli-labs-1.20.1-1.4.4.jar" -C "$runtime/check-classes" .
if($LASTEXITCODE){throw 'Native check injection failed'}
$argsFile=Join-Path $runtime 'launch.args'
$lines=Get-Content -LiteralPath "$previous/launch.args"
$lines=$lines | ForEach-Object {$_.Replace($previous.Replace('\','/'),$runtime.Replace('\','/')).Replace($previous,$runtime)}
# Keep the existing launcher service JAR on its original read-only classpath.
$lines=$lines | ForEach-Object {$_.Replace((Join-Path $runtime 'lab-check-launcher.jar').Replace('\','/'),(Join-Path $previous 'lab-check-launcher.jar').Replace('\','/'))}
$lines=@('"-Davaritia.editor.fixtures='+$Fixtures.Replace('\','/')+'"')+$lines
Set-Content -LiteralPath $argsFile -Value $lines -Encoding utf8NoBOM
Push-Location $runtime
try{& "$java/java.exe" ('@'+$argsFile) *> "$runtime/check.log";if($LASTEXITCODE){throw "Native validation failed: $runtime/check.log"}}finally{Pop-Location}
Get-Content "$runtime/check.log" | Select-String 'AVARITIA_EDITOR_NATIVE_SERIALIZERS_OK|LOLI_LABS_RUNTIME_CHECKS_PASSED'
