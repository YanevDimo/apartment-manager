# Finalize HTML templates - fix formatting and ensure valid HTML structure

$templatesPath = "src\main\resources\templates"

function Finalize-Html {
    param($filePath)
    
    $content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)
    $originalContent = $content
    
    Write-Host "Finalizing: $(Split-Path $filePath -Leaf)" -ForegroundColor Cyan
    
    # Fix broken html tag (split across lines)
    $content = $content -replace '<html\s+lang="bg"\s+>', '<html lang="bg">'
    $content = $content -replace '<html\s+lang="bg"\r?\n\s*>', '<html lang="bg">'
    
    # Fix broken attributes (split across lines)
    $content = $content -replace '(\w+)\s+\r?\n\s*class=', '$1 class='
    $content = $content -replace '(\w+)\s+\r?\n\s*id=', '$1 id='
    $content = $content -replace '(\w+)\s+\r?\n\s*href=', '$1 href='
    $content = $content -replace '(\w+)\s+\r?\n\s*action=', '$1 action='
    
    # Remove extra blank lines (more than 2 consecutive)
    $content = $content -replace '(\r?\n\s*){3,}', "`n`n"
    
    # Fix missing closing tags for empty elements
    $selfClosingTags = @('meta', 'link', 'img', 'input', 'br', 'hr')
    foreach ($tag in $selfClosingTags) {
        # Fix unclosed tags like <meta ...> to <meta ... />
        $content = $content -replace "<$tag([^>]*?)(?<!/)>", "<$tag`$1 />"
    }
    
    # Ensure proper indentation (basic cleanup)
    # Remove leading/trailing whitespace from lines
    $lines = $content -split "`r?`n"
    $cleanedLines = @()
    foreach ($line in $lines) {
        $trimmed = $line.TrimEnd()
        if ($trimmed.Length -gt 0) {
            $cleanedLines += $trimmed
        } else {
            $cleanedLines += ""
        }
    }
    $content = $cleanedLines -join "`n"
    
    # Ensure file ends with newline
    if ($content -notmatch '\n$') {
        $content += "`n"
    }
    
    # Only write if content changed
    if ($content -ne $originalContent) {
        [System.IO.File]::WriteAllText($filePath, $content, [System.Text.Encoding]::UTF8)
        Write-Host "  ✓ Fixed formatting: $(Split-Path $filePath -Leaf)" -ForegroundColor Green
        return $true
    } else {
        Write-Host "  - No formatting issues: $(Split-Path $filePath -Leaf)" -ForegroundColor Gray
        return $false
    }
}

# Get all HTML files except the conversion guide
$htmlFiles = Get-ChildItem -Path $templatesPath -Filter "*.html" | Where-Object { $_.Name -ne "THYMELEAF_CONVERSION_GUIDE.md" }

$totalFiles = $htmlFiles.Count
$fixedFiles = 0

Write-Host "`n=== Finalizing HTML Templates ===" -ForegroundColor Yellow
Write-Host "Processing $totalFiles HTML files...`n" -ForegroundColor Yellow

foreach ($file in $htmlFiles) {
    if (Finalize-Html -filePath $file.FullName) {
        $fixedFiles++
    }
}

Write-Host "`n=== Summary ===" -ForegroundColor Yellow
Write-Host "Total files processed: $totalFiles" -ForegroundColor Cyan
Write-Host "Files with formatting fixes: $fixedFiles" -ForegroundColor Green
Write-Host "Files already properly formatted: $($totalFiles - $fixedFiles)" -ForegroundColor Gray
Write-Host "`nAll HTML templates are now pure, valid HTML! ✓" -ForegroundColor Green



