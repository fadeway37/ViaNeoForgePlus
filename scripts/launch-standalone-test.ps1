# Requires PowerShell 7 and an official NeoForge client installation.
# Uses an offline test identity; never reads launcher accounts.
[CmdletBinding()]
param(
    [string]$InstalledClient = (Join-Path $PSScriptRoot '../../vianeoforgeplus-test-26.2-26.2.0.82/standalone'),
    [string]$JavaHome = $env:JAVA_HOME,
    [ValidateSet('core', 'bedrock')][string]$Profile = 'core',
    [string]$CoreJar = (Join-Path $PSScriptRoot '../build/libs/ViaNeoForgePlus-5.0.1.jar'),
    [string]$BedrockJar = (Join-Path $PSScriptRoot '../../vianeoforgeplus-bedrock/build/libs/vianeoforgeplus-bedrock-1.0.0.jar'),
    [string]$AssetCache = '',
    [string]$LibraryCache = '',
    [string]$Server = '',
    [switch]$PrepareOnly
)
$ErrorActionPreference = 'Stop'
if (![Environment]::Is64BitProcess -or $PSVersionTable.PSVersion.Major -lt 7 -or !$IsWindows) {
    throw 'Use 64-bit PowerShell 7 on Windows.'
}
if (!$JavaHome -or !(Test-Path -LiteralPath (Join-Path $JavaHome 'bin/java.exe'))) {
    throw 'Set JAVA_HOME or pass -JavaHome pointing to Java 25.'
}
if ($Server -and $Server -notmatch '^127\.0\.0\.1:\d{1,5}$') {
    throw 'This offline regression launcher only accepts a loopback test server.'
}
$clientRoot = (Resolve-Path -LiteralPath $InstalledClient).Path
$vanilla = Get-Content -LiteralPath (Join-Path $clientRoot 'versions/26.2/26.2.json') -Raw | ConvertFrom-Json -AsHashtable
$neo = Get-Content -LiteralPath (Join-Path $clientRoot 'versions/neoforge-26.2.0.82/neoforge-26.2.0.82.json') -Raw | ConvertFrom-Json -AsHashtable
if ($vanilla.id -ne '26.2' -or $neo.id -ne 'neoforge-26.2.0.82' -or $neo.inheritsFrom -ne $vanilla.id) {
    throw 'Expected the official Minecraft 26.2 / NeoForge 26.2.0.82 installation.'
}
$gameDir = Join-Path $clientRoot "instances/$Profile"
$assetsDir = Join-Path $clientRoot 'assets'
$libraryDir = Join-Path $clientRoot 'libraries'
$nativeDir = Join-Path $gameDir 'natives'
foreach ($directory in @($gameDir, $assetsDir, $libraryDir, $nativeDir, (Join-Path $gameDir 'mods'))) {
    New-Item -ItemType Directory -Path $directory -Force | Out-Null
}

function Receive-VerifiedFile([string]$Url, [string]$Destination, [string]$Sha1, [string]$Cached = '') {
    if ((Test-Path -LiteralPath $Destination) -and (Get-FileHash -LiteralPath $Destination -Algorithm SHA1).Hash -eq $Sha1) { return }
    New-Item -ItemType Directory -Path (Split-Path -Parent $Destination) -Force | Out-Null
    if ($Cached -and (Test-Path -LiteralPath $Cached) -and (Get-FileHash -LiteralPath $Cached -Algorithm SHA1).Hash -eq $Sha1) {
        Copy-Item -LiteralPath $Cached -Destination $Destination
    } else {
        Write-Host "Downloading $([IO.Path]::GetFileName($Destination))"
        Invoke-WebRequest -Uri $Url -OutFile $Destination -MaximumRetryCount 3 -RetryIntervalSec 2 -TimeoutSec 30
    }
    if ((Get-FileHash -LiteralPath $Destination -Algorithm SHA1).Hash -ne $Sha1) {
        throw "Checksum mismatch: $Destination"
    }
}

function Test-ManifestRules($Entry) {
    if (!$Entry.rules) { return $true }
    $allowed = $false
    foreach ($rule in $Entry.rules) {
        $matches = $true
        if ($rule.os) {
            if ($rule.os.name -and $rule.os.name -ne 'windows') { $matches = $false }
            if ($rule.os.arch -and $rule.os.arch -notin @('x86_64', 'amd64')) { $matches = $false }
            if ($rule.os.version -and [Environment]::OSVersion.Version.ToString() -notmatch $rule.os.version) { $matches = $false }
            if ($rule.os.versionRange.min -and [Environment]::OSVersion.Version -lt [version]$rule.os.versionRange.min) { $matches = $false }
            if ($rule.os.versionRange.max -and [Environment]::OSVersion.Version -ge [version]$rule.os.versionRange.max) { $matches = $false }
        }
        if ($rule.features) {
            foreach ($feature in $rule.features.Keys) {
                if ($rule.features[$feature] -ne $false) { $matches = $false }
            }
        }
        if ($matches) { $allowed = $rule.action -eq 'allow' }
    }
    return $allowed
}

# Child version replaces matching parent coordinates; classifiers stay distinct.
$libraries = [ordered]@{}
foreach ($lib in @($vanilla.libraries) + @($neo.libraries)) {
    $parts = $lib.name.Split(':')
    $key = "$($parts[0]):$($parts[1])"
    if ($parts.Length -gt 3) { $key += ":$($parts[3])" }
    $libraries[$key] = $lib
}
$classPath = [Collections.Generic.List[string]]::new()
foreach ($lib in $libraries.Values) {
    if (!(Test-ManifestRules $lib)) { continue }
    $artifact = $lib.downloads.artifact
    if (!$artifact) { throw "Unsupported library manifest: $($lib.name)" }
    $destination = Join-Path $libraryDir $artifact.path
    $cached = ''
    if ($LibraryCache) {
        $parts = $lib.name.Split(':')
        $candidate = Join-Path $LibraryCache "$($parts[0])/$($parts[1])/$($parts[2])/$($artifact.sha1)/$([IO.Path]::GetFileName($artifact.path))"
        if (Test-Path -LiteralPath $candidate) { $cached = $candidate }
    }
    Receive-VerifiedFile $artifact.url $destination $artifact.sha1 $cached
    $classPath.Add($destination)
}
$clientJar = Join-Path $clientRoot 'versions/26.2/26.2.jar'
Receive-VerifiedFile $vanilla.downloads.client.url $clientJar $vanilla.downloads.client.sha1
$classPath.Add($clientJar)
$assetIndexFile = Join-Path $assetsDir "indexes/$($vanilla.assetIndex.id).json"
$cachedIndex = if ($AssetCache) { Join-Path $AssetCache "indexes/$($vanilla.assetIndex.id).json" } else { '' }
Receive-VerifiedFile $vanilla.assetIndex.url $assetIndexFile $vanilla.assetIndex.sha1 $cachedIndex
$assetIndex = Get-Content -LiteralPath $assetIndexFile -Raw | ConvertFrom-Json -AsHashtable
$objects = @($assetIndex.objects.Values | Sort-Object -Property hash -Unique)
foreach ($object in $objects) {
    $relative = "objects/$($object.hash.Substring(0,2))/$($object.hash)"
    $cachedObject = if ($AssetCache) { Join-Path $AssetCache $relative } else { '' }
    Receive-VerifiedFile "https://resources.download.minecraft.net/$($object.hash.Substring(0,2))/$($object.hash)" (Join-Path $assetsDir $relative) $object.hash $cachedObject
}
Write-Host "Verified $($classPath.Count) classpath artifacts and $($objects.Count) asset objects."

# Copy only the requested artifacts. Never sync/delete a user's mods.
Copy-Item -LiteralPath $CoreJar -Destination (Join-Path $gameDir 'mods/ViaNeoForgePlus-5.0.1.jar')
if ($Profile -eq 'bedrock') {
    Copy-Item -LiteralPath $BedrockJar -Destination (Join-Path $gameDir 'mods/vianeoforgeplus-bedrock-1.0.0.jar')
}
$substitutions = @{
    auth_player_name = 'VNFTest'; auth_uuid = 'a2095bd6e6a739f9af2c7b1747db98ce'; auth_access_token = '0'
    clientid = '0'; auth_xuid = '0'; version_name = $neo.id; version_type = 'release'
    game_directory = $gameDir; assets_root = $assetsDir; assets_index_name = $vanilla.assetIndex.id
    natives_directory = $nativeDir; library_directory = $libraryDir; classpath = ($classPath -join ';')
    launcher_name = 'ViaNeoForgePlus-Regression'; launcher_version = '5.0.1'
}
function Expand-Arguments($Entries) {
    foreach ($entry in $Entries) {
        $values = if ($entry -is [string]) { @($entry) } elseif (Test-ManifestRules $entry) { @($entry.value) } else { @() }
        foreach ($value in $values) {
            foreach ($key in $substitutions.Keys) { $value = $value.Replace('${' + $key + '}', [string]$substitutions[$key]) }
            if ($value -match '\$\{') { throw "Unresolved launch argument: $value" }
            $value
        }
    }
}
$launchArgs = @('-Xms512M', '-Xmx3G')
# Local JDK 25 workaround: an absent UDS directory causes TCP fallback.
$absentUdsDir = Join-Path $gameDir 'nonexistent-uds-fallback'
if (Test-Path -LiteralPath $absentUdsDir) { throw "UDS fallback path must not exist: $absentUdsDir" }
$launchArgs += "-Djdk.net.unixdomain.tmpdir=$absentUdsDir"
$launchArgs += @(Expand-Arguments (@($vanilla.arguments.jvm) + @($neo.arguments.jvm)))
$launchArgs += $neo.mainClass
$launchArgs += @(Expand-Arguments (@($vanilla.arguments.game) + @($neo.arguments.game)))
if ($Server) { $launchArgs += @('--quickPlayMultiplayer', $Server) }
# Java argument file avoids Windows command-line length limits; test credentials only.
$argFile = Join-Path $gameDir 'launch.args'
$escapedArgs = $launchArgs | ForEach-Object { '"' + $_.Replace('\', '\\').Replace('"', '\"') + '"' }
[IO.File]::WriteAllLines($argFile, $escapedArgs, [Text.UTF8Encoding]::new($false))
Write-Host "Ready: $gameDir (Minecraft 26.2 / NeoForge 26.2.0.82 / $Profile)"
if ($PrepareOnly) { return }
Push-Location $gameDir
try {
    & (Join-Path $JavaHome 'bin/java.exe') "@$argFile"
    if ($LASTEXITCODE -ne 0) { throw "Client exited with code $LASTEXITCODE. See $gameDir/logs." }
} finally { Pop-Location }
