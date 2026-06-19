param(
    [string] $Java8Home = $env:JAVA8_HOME
)

$ErrorActionPreference = 'Stop'

function Test-JavaHome {
    param([string] $Path)
    return $Path -and (Test-Path -LiteralPath (Join-Path $Path 'bin\java.exe'))
}

if (-not (Test-JavaHome $Java8Home) -and (Test-JavaHome $env:JAVA_HOME)) {
    $Java8Home = $env:JAVA_HOME
}

if (-not (Test-JavaHome $Java8Home)) {
    throw 'JDK 8 not found. Set JAVA8_HOME to a JDK 8 installation directory, or run this script with JAVA_HOME already pointing to JDK 8.'
}

$env:JAVA_HOME = $Java8Home
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
mvn -version
mvn clean verify
