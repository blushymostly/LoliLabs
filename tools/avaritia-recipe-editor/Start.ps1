param([string]$Instance='')
$ErrorActionPreference='Stop'
$tool=$PSScriptRoot
if(!$Instance){$Instance=[IO.Path]::GetFullPath((Join-Path $tool '../../..'))}
$prism=[IO.Path]::GetFullPath((Join-Path $Instance '../..'))
$java=Join-Path $prism 'java/java-runtime-gamma/bin/javaw.exe'
if(!(Test-Path -LiteralPath $java)){$java=(Get-Command javaw.exe -ErrorAction Stop).Source}
$jar=Join-Path $tool 'dist/avaritia-recipe-editor-1.0.0.jar'
if(!(Test-Path -LiteralPath $jar)){throw 'Editor is not built. Run build.ps1 first.'}
Start-Process -FilePath $java -ArgumentList @('-Xmx1G','-jar',('"'+$jar+'"'),('"'+$Instance+'"')) -WorkingDirectory $tool -WindowStyle Hidden
