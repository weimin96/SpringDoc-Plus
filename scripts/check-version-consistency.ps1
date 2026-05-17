param(
  [string] $ExpectedVersion = ''
)

$ErrorActionPreference = 'Stop'

function Stop-WithMessage([string] $Message) {
  throw $Message
}

function Read-Utf8File([string] $RelativePath) {
  $path = Join-Path $ProjectRoot $RelativePath
  if (-not (Test-Path -LiteralPath $path)) {
    Stop-WithMessage "缺少文件：$RelativePath"
  }
  return Get-Content -LiteralPath $path -Raw -Encoding UTF8
}

function Assert-ReadmeDependencyVersion([string] $RelativePath, [string] $Version) {
  $content = Read-Utf8File $RelativePath
  $pattern = '<artifactId>(springdoc-plus-(?:openapi3|gateway)-spring-boot-starter)</artifactId>\s*<version>([^<]+)</version>'
  $matches = [regex]::Matches($content, $pattern)
  if ($matches.Count -eq 0) {
    Stop-WithMessage "$RelativePath 未找到 SpringDoc-Plus starter 依赖示例。"
  }

  foreach ($match in $matches) {
    $artifactId = $match.Groups[1].Value
    $actualVersion = $match.Groups[2].Value
    if ($actualVersion -ne $Version) {
      Stop-WithMessage "$RelativePath 中 $artifactId 版本为 $actualVersion，期望 $Version。"
    }
  }
}

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$rootPomPath = Join-Path $ProjectRoot 'pom.xml'
[xml] $rootPom = Get-Content -LiteralPath $rootPomPath -Raw -Encoding UTF8
$revision = [string] $rootPom.project.properties.revision

if ([string]::IsNullOrWhiteSpace($revision)) {
  Stop-WithMessage '根 pom.xml 缺少 revision 属性。'
}

if ([string]::IsNullOrWhiteSpace($ExpectedVersion)) {
  $tagName = $env:GITHUB_REF_NAME
  $ExpectedVersion = if ($tagName) { $tagName.TrimStart('v') } else { $revision }
}

if ($revision -ne $ExpectedVersion) {
  Stop-WithMessage "根 pom.xml revision 为 $revision，期望 $ExpectedVersion。"
}

Assert-ReadmeDependencyVersion 'README.md' $ExpectedVersion
Assert-ReadmeDependencyVersion 'README.en.md' $ExpectedVersion

$changelog = Read-Utf8File 'CHANGELOG.md'
if ($changelog -notmatch "(?m)^## \[$([regex]::Escape($ExpectedVersion))\]") {
  Stop-WithMessage "CHANGELOG.md 缺少 $ExpectedVersion 版本记录。"
}

$pomFiles = Get-ChildItem -LiteralPath $ProjectRoot -Recurse -Filter 'pom.xml' |
  Where-Object { $_.FullName -ne $rootPomPath }

foreach ($pomFile in $pomFiles) {
  $content = Get-Content -LiteralPath $pomFile.FullName -Raw -Encoding UTF8
  if ($content -match '<version>0\.1\.\d+(?:-SNAPSHOT)?</version>') {
    Stop-WithMessage "$($pomFile.FullName) 存在硬编码项目版本。"
  }
}

Write-Host "版本一致性检查通过：$ExpectedVersion"
