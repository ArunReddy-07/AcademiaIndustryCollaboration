$environmentNames = @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET', 'JWT_EXPIRATION_MS', 'SPRING_PROFILES_ACTIVE')
$previousEnvironment = @{}
foreach ($name in $environmentNames) {
    $previousEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
}

$securePassword = $null
$passwordPointer = [IntPtr]::Zero
$locationPushed = $false
$mavenExitCode = 0

try {
    if (-not $env:DB_URL) {
        $databaseName = Read-Host 'Existing PostgreSQL database name [academia_industry_collaboration_portal]'
        if ([string]::IsNullOrWhiteSpace($databaseName)) {
            $databaseName = 'academia_industry_collaboration_portal'
        }
        if ($databaseName -notmatch '^[A-Za-z0-9_]+$') {
            throw 'Database name may contain only letters, numbers, and underscores.'
        }
        $env:DB_URL = "jdbc:postgresql://localhost:5432/$databaseName"
    }

    if (-not $env:DB_USERNAME) {
        $databaseUser = Read-Host 'PostgreSQL username [postgres]'
        $env:DB_USERNAME = if ([string]::IsNullOrWhiteSpace($databaseUser)) { 'postgres' } else { $databaseUser }
    }

    if (-not $env:DB_PASSWORD) {
        $securePassword = Read-Host 'PostgreSQL password' -AsSecureString
        $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    }

    if (-not $env:JWT_SECRET) {
        $random = [Security.Cryptography.RandomNumberGenerator]::Create()
        try {
            $secretBytes = New-Object byte[] 32
            $random.GetBytes($secretBytes)
            $env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
        } finally {
            $random.Dispose()
        }
    }

    if ($env:JWT_SECRET.Length -lt 32) {
        throw 'JWT_SECRET must contain at least 32 characters.'
    }

    if (-not $env:JWT_EXPIRATION_MS) { $env:JWT_EXPIRATION_MS = '3600000' }
    $env:SPRING_PROFILES_ACTIVE = 'dev'

    Push-Location $PSScriptRoot
    $locationPushed = $true
    mvn spring-boot:run
    $mavenExitCode = $LASTEXITCODE
} finally {
    if ($locationPushed) { Pop-Location }
    if ($passwordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    if ($securePassword) { $securePassword.Dispose() }
    foreach ($name in $environmentNames) {
        $value = $previousEnvironment[$name]
        if ($null -eq $value) { Remove-Item "Env:$name" -ErrorAction SilentlyContinue }
        else { Set-Item "Env:$name" $value }
    }
}

if ($mavenExitCode -ne 0) { throw "Backend stopped with exit code $mavenExitCode." }