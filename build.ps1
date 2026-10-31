param(
    [string]$PrismRoot = 'D:\Prism',
    [string]$JavaBin = 'D:\Prism\java\java-runtime-gamma\bin'
)
$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$instance = Split-Path $project -Parent
$output = Join-Path $project 'build\loli-labs-1.4.4'
$classes = Join-Path $output 'classes'
New-Item -ItemType Directory -Force -Path $classes | Out-Null

# Compile directly against this installed Forge version's SRG runtime API.
# No Gradle downloads or changes to the Minecraft installation are needed.
$gameJars = @(
    "$PrismRoot\libraries\net\minecraftforge\javafmllanguage\1.20.1-47.4.10\javafmllanguage-1.20.1-47.4.10.jar",
    "$PrismRoot\libraries\net\minecraftforge\fmlcore\1.20.1-47.4.10\fmlcore-1.20.1-47.4.10.jar",
    "$PrismRoot\libraries\net\minecraftforge\fmlloader\1.20.1-47.4.10\fmlloader-1.20.1-47.4.10.jar",
    "$PrismRoot\libraries\net\minecraftforge\forge\1.20.1-47.4.10\forge-1.20.1-47.4.10-client.jar",
    "$PrismRoot\libraries\net\minecraftforge\forge\1.20.1-47.4.10\forge-1.20.1-47.4.10-universal.jar",
    "$PrismRoot\libraries\net\minecraft\client\1.20.1-20230612.114412\client-1.20.1-20230612.114412-srg.jar"
)
$libraries = foreach ($metadataPath in @(
    "$PrismRoot\meta\net.minecraftforge\47.4.10.json",
    "$PrismRoot\meta\net.minecraft\1.20.1.json"
)) {
    $metadata = Get-Content -Raw -LiteralPath $metadataPath | ConvertFrom-Json
    foreach ($library in $metadata.libraries) {
        $parts = $library.name.Split(':')
        $relativePath = $parts[0].Replace('.', '/') + '/' + $parts[1] + '/' + $parts[2] + '/' + $parts[1] + '-' + $parts[2]
        if ($parts.Length -gt 3) { $relativePath += '-' + $parts[3] }
        $path = Join-Path "$PrismRoot\libraries" ($relativePath + '.jar')
        if (Test-Path -LiteralPath $path) { $path }
    }
}
$mods = Get-ChildItem "$instance\minecraft\mods" -Filter '*.jar' | Where-Object {
    $_.Name -match '^(Botania-|appliedenergistics2-|Re-Avaritia-)'
} | Select-Object -ExpandProperty FullName
$apis=Get-ChildItem "$project/dependencies" -Filter '*.jar' | Where-Object Name -ne 'ic2ceu-modern-1.20.1-0.19.1.jar' | Select-Object -ExpandProperty FullName
$classpath = ($gameJars + $mods + $libraries + $apis) -join ';'
$activeSources = @('RemovedItems.java', 'LegacyMaterials.java', 'LabConfigMigration.java', 'LabItems.java', 'LoliLabs.java', 'ReleaseRuntimeChecks.java', 'EUWorldChecks.java', 'MachineDefinitions.java', 'LabMachines.java', 'LabMachineBlock.java', 'LabMachineItem.java', 'LabMachineEntity.java', 'LabModularUI.java', 'LabPanelClient.java', 'LegacyMappings.java', 'LabMiningTool.java')
$sources = $activeSources | ForEach-Object { Join-Path "$project\src\tmr\lolilabs" $_ }
$arguments = @('--release', '17', '-proc:none', '-encoding', 'UTF-8', '-classpath', ('"' + $classpath.Replace('\', '/') + '"'), '-d', ('"' + $classes.Replace('\', '/') + '"'))
$arguments += $sources | ForEach-Object { '"' + $_.Replace('\', '/') + '"' }
$argumentFile = Join-Path $output 'javac.args'
Set-Content -LiteralPath $argumentFile -Value $arguments -Encoding utf8NoBOM
& "$JavaBin\javac.exe" ('@' + $argumentFile)
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
$dist = Join-Path $project 'dist'
New-Item -ItemType Directory -Force -Path $dist | Out-Null
$jarPath = Join-Path $dist 'loli-labs-1.20.1-1.4.4.jar'
& "$JavaBin\jar.exe" --create --file $jarPath -C $classes . -C "$project\resources-1.4.4" .
if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed.' }
Write-Output "Built $jarPath"

