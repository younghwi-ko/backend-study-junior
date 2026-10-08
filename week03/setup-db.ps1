$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
        throw 'Set DB_PASSWORD before creating PostgreSQL. Use the same value when running Java.'
    }
    & docker info *> $null
    if ($LASTEXITCODE -ne 0) { throw 'Start Docker Desktop first.' }
    & docker container inspect postgres18 *> $null
    if ($LASTEXITCODE -eq 0) {
        throw 'postgres18 already exists. See README for restart/schema commands; existing data was left intact.'
    }
    New-Item -ItemType Directory -Force data | Out-Null
    # PPT 7페이지와 같은 PostgreSQL 18 및 저장 경로.
    $savedPostgresPassword = $env:POSTGRES_PASSWORD
    try {
        $env:POSTGRES_PASSWORD = $env:DB_PASSWORD
        # docker가 프로세스 환경변수에서 읽도록 값 없이 이름만 전달한다.
        & docker run -d --name postgres18 -e POSTGRES_PASSWORD -p 127.0.0.1:5432:5432 -v "${PWD}/data:/var/lib/postgresql" postgres:18
        if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL container creation failed.' }
    } finally {
        $env:POSTGRES_PASSWORD = $savedPostgresPassword
    }
    $ready = $false
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        & docker exec postgres18 pg_isready -U postgres -d postgres *> $null
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { throw 'PostgreSQL was not ready within 60 seconds. Run: docker logs postgres18' }
    Get-Content -Raw sql/schema.sql | & docker exec -i postgres18 psql -U postgres -d postgres -v ON_ERROR_STOP=1
    if ($LASTEXITCODE -ne 0) { throw 'DDL failed. Check docker logs postgres18 and schema.sql.' }
    Write-Host 'PostgreSQL and both tables are ready.'
} finally {
    Pop-Location
}
