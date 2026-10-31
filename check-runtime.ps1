param([switch]$GameTest)
$ErrorActionPreference='Stop'
$project=$PSScriptRoot
$instance=Split-Path $project -Parent
$runtime=Join-Path $project $(if($GameTest){'build\runtime-check-1.4.4-world'}else{'build\runtime-check-1.4.4'})
New-Item -ItemType Directory -Force -Path "$runtime\mods" | Out-Null
$mods=Get-ChildItem "$instance\minecraft\mods" -Filter '*.jar' | Where-Object { $_.Name -match '^(Botania-|appliedenergistics2-|curios-|Patchouli-|guideme-|Re-Avaritia-)' }
foreach($mod in $mods){ Copy-Item -LiteralPath $mod.FullName -Destination "$runtime\mods" -Force }
Copy-Item -LiteralPath "$project\dist\loli-labs-1.20.1-1.4.4.jar" -Destination "$runtime\mods" -Force
Copy-Item -LiteralPath "$project/dependencies/ic2ceu-modern-1.20.1-0.19.2.jar" -Destination "$runtime/mods" -Force
$libraryRoot='D:\Prism\libraries'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip=[IO.Compression.ZipFile]::OpenRead("$libraryRoot\net\minecraftforge\forge\1.20.1-47.4.10\forge-1.20.1-47.4.10-installer.jar")
$reader=[IO.StreamReader]::new($zip.GetEntry('version.json').Open())
$version=$reader.ReadToEnd() | ConvertFrom-Json
$reader.Dispose()
$zip.Dispose()
$jvm=foreach($arg in $version.arguments.jvm){ $arg.Replace('${library_directory}',$libraryRoot.Replace('\','/')).Replace('${classpath_separator}',';').Replace('${version_name}','1.20.1-forge-47.4.10') }
$jars=foreach($metaPath in @('D:\Prism\meta\net.minecraftforge\47.4.10.json','D:\Prism\meta\net.minecraft\1.20.1.json','D:\Prism\meta\org.lwjgl3\3.3.1.json')) {
 $meta=Get-Content -Raw -LiteralPath $metaPath | ConvertFrom-Json
 foreach($library in $meta.libraries){
  $parts=$library.name.Split(':')
  $relative=$parts[0].Replace('.','/')+'/'+$parts[1]+'/'+$parts[2]+'/'+$parts[1]+'-'+$parts[2]
  if($parts.Length -gt 3){$relative+='-'+$parts[3]}
  $file=Join-Path $libraryRoot ($relative+'.jar')
  if(Test-Path -LiteralPath $file){$file}
  if($library.natives.windows){
   $native=$library.natives.windows.Replace('${arch}','64')
   $nativeFile=Join-Path $libraryRoot ($relative+'-'+$native+'.jar')
   if(Test-Path -LiteralPath $nativeFile){$nativeFile}
  }
 }
}
$jars+=Get-ChildItem "$libraryRoot\net\minecraftforge" -Recurse -Filter '*1.20.1-47.4.10.jar' | Where-Object {$_.Directory.Parent.Name -in @('fmlcore','javafmllanguage','lowcodelanguage','mclanguage')} | Select-Object -ExpandProperty FullName
$jars+="$libraryRoot\net\minecraftforge\forge\1.20.1-47.4.10\forge-1.20.1-47.4.10-client.jar"
$jars+="$libraryRoot\net\minecraftforge\forge\1.20.1-47.4.10\forge-1.20.1-47.4.10-universal.jar"
$jars+="$libraryRoot\net\minecraft\client\1.20.1-20230612.114412\client-1.20.1-20230612.114412-extra.jar"
$checkClasses=Join-Path $runtime 'launcher-classes'
New-Item -ItemType Directory -Force -Path $checkClasses | Out-Null
& 'D:\Prism\java\java-runtime-gamma\bin\javac.exe' --release 17 -proc:none -classpath ($jars -join ';') -d $checkClasses "$project\checks\CheckLaunchHandler.java" "$project\checks\GameTestLaunchHandler.java"
if($LASTEXITCODE -ne 0){throw 'Verification launcher compilation failed'}
$checkJar=Join-Path $runtime 'lab-check-launcher.jar'
& 'D:\Prism\java\java-runtime-gamma\bin\jar.exe' --create --file $checkJar -C $checkClasses . -C "$project\checks" META-INF
$jars+=$checkJar
$args=@('-Xmx3G','-Dloli_labs.verify=true','-Dfml.earlyprogresswindow=false','-Dforge.logging.console.level=warn')+$jvm+@('-cp',($jars -join ';'),$version.mainClass,'--launchTarget','lolilabsdata','--fml.forgeVersion','47.4.10','--fml.mcVersion','1.20.1','--fml.forgeGroup','net.minecraftforge','--fml.mcpVersion','20230612.114412','--gameDir',$runtime,'--mod','loli_labs','--all','--output',"$runtime\generated")
# Java argument files preserve long classpaths and keep all generated files in this workspace.
if($GameTest){
    New-Item -ItemType Directory -Force -Path "$runtime/config" | Out-Null
    Set-Content "$runtime/config/ic2modern.yaml" "gt6StylePipesCables: true
disableEnergyTiers: false
debug: false" -Encoding utf8NoBOM
    $args=@('-Xmx3G','-Dforge.gameTestServer=true','-Dforge.enabledGameTestNamespaces=loli_labs','-Dfml.earlyprogresswindow=false','-Dforge.logging.console.level=info')+$jvm+@('-cp',($jars -join ';'),$version.mainClass,'--launchTarget','lolilabsgametest','--fml.forgeVersion','47.4.10','--fml.mcVersion','1.20.1','--fml.forgeGroup','net.minecraftforge','--fml.mcpVersion','20230612.114412','--nogui')
}
$argumentFile=Join-Path $runtime 'launch.args'
$quoted=$args | ForEach-Object { '"'+$_.Replace('\','/')+'"' }
Set-Content -LiteralPath $argumentFile -Value $quoted -Encoding utf8NoBOM
Push-Location $runtime
try { & 'D:\Prism\java\java-runtime-gamma\bin\java.exe' ('@'+$argumentFile) 2>&1 | Tee-Object -FilePath "$runtime\check.log"; if($LASTEXITCODE -ne 0){throw 'Forge runtime checks failed'} }
finally { Pop-Location }
