param([switch]$SyntaxOnly)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force out | Out-Null
    & javac -encoding UTF-8 -d out -sourcepath src src/Main.java
    if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
    $javaArgs = @('-Dfile.encoding=UTF-8', '-cp', 'out;lib/*', 'Main')
    if ($SyntaxOnly) { $javaArgs += '--syntax' }
    & java @javaArgs
    if ($LASTEXITCODE -ne 0) { throw 'Java execution failed. Check Docker, schema and JDBC settings.' }
} finally {
    Pop-Location
}
