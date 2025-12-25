# Add CSS links to HTML templates that don't have them

$templatesPath = "src\main\resources\templates"
$cssLink = '    <link href="/static/css/app.css" rel="stylesheet" />'

function Add-CssLink {
    param($filePath)
    
    $content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)
    $originalContent = $content
    
    # Skip if already has app.css link
    if ($content -match '/static/css/app\.css') {
        Write-Host "  - Already has app.css: $(Split-Path $filePath -Leaf)" -ForegroundColor Gray
        return $false
    }
    
    # Remove duplicate CSS links if any
    $content = $content -replace '(?s)<link[^>]*app\.css[^>]*>\s*', ''
    
    # Find the head tag and add CSS link after meta tags
    if ($content -match '<head[^>]*>') {
        # Find the last meta tag or title tag to insert after
        if ($content -match '(<meta[^>]*>\s*)(?!.*<meta)') {
            # Add CSS link after the last meta tag
            $content = $content -replace '((<meta[^>]*>\s*)(?!.*<meta))', "`$1$cssLink`n    "
        } elseif ($content -match '<title') {
            # Add CSS link before title tag
            $content = $content -replace '(<title)', "$cssLink`n    `$1"
        } else {
            # Add CSS link right after head tag
            $content = $content -replace '(<head[^>]*>)', "`$1`n    $cssLink"
        }
        
        [System.IO.File]::WriteAllText($filePath, $content, [System.Text.Encoding]::UTF8)
        Write-Host "  ✓ Added CSS link: $(Split-Path $filePath -Leaf)" -ForegroundColor Green
        return $true
    }
    
    Write-Host "  ⚠ No head tag found: $(Split-Path $filePath -Leaf)" -ForegroundColor Yellow
    return $false
}

# Get all HTML files except the conversion guide
$htmlFiles = Get-ChildItem -Path $templatesPath -Filter "*.html" | Where-Object { $_.Name -ne "THYMELEAF_CONVERSION_GUIDE.md" }

$totalFiles = $htmlFiles.Count
$updatedFiles = 0

Write-Host "`n=== Adding CSS Links to Templates ===" -ForegroundColor Yellow
Write-Host "Processing $totalFiles HTML files...`n" -ForegroundColor Yellow

foreach ($file in $htmlFiles) {
    if (Add-CssLink -filePath $file.FullName) {
        $updatedFiles++
    }
}

Write-Host "`n=== Summary ===" -ForegroundColor Yellow
Write-Host "Total files processed: $totalFiles" -ForegroundColor Cyan
Write-Host "Files updated: $updatedFiles" -ForegroundColor Green
Write-Host "Files already had CSS: $($totalFiles - $updatedFiles)" -ForegroundColor Gray
Write-Host "`nCSS links added successfully! ✓" -ForegroundColor Green

