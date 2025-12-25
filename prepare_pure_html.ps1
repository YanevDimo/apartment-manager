# Prepare all HTML templates to be pure HTML
# Removes any remaining Thymeleaf syntax and ensures valid HTML structure

$templatesPath = "src\main\resources\templates"

function Prepare-PureHtml {
    param($filePath)
    
    $content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)
    $originalContent = $content
    
    Write-Host "Processing: $(Split-Path $filePath -Leaf)" -ForegroundColor Cyan
    
    # Remove xmlns:th namespace declaration
    $content = $content -replace ' xmlns:th="[^"]*"', ''
    
    # Remove all th: attributes (comprehensive list)
    $thymeleafAttributes = @(
        'th:fragment', 'th:replace', 'th:include', 'th:if', 'th:unless', 'th:each',
        'th:text', 'th:utext', 'th:value', 'th:href', 'th:src', 'th:action',
        'th:class', 'th:classappend', 'th:style', 'th:attr', 'th:attrappend',
        'th:object', 'th:field', 'th:errors', 'th:block', 'th:remove',
        'th:switch', 'th:case', 'th:inline', 'th:with', 'th:assert'
    )
    
    foreach ($attr in $thymeleafAttributes) {
        # Remove attribute with any value (including complex expressions)
        $content = $content -replace " $attr=`"[^`"]*`"", ''
        $content = $content -replace " $attr='[^']*'", ''
    }
    
    # Remove any remaining th: attributes (catch-all)
    while ($content -match ' th:[a-zA-Z-]+="[^"]*"') {
        $content = $content -replace ' th:[a-zA-Z-]+="[^"]*"', ''
    }
    while ($content -match " th:[a-zA-Z-]+='[^']*'") {
        $content = $content -replace " th:[a-zA-Z-]+='[^']*'", ''
    }
    
    # Remove Thymeleaf expression syntax: ${...}
    $content = $content -replace '\$\{[^}]+\}', ''
    
    # Remove Thymeleaf expression syntax: #{...}
    $content = $content -replace '#\{[^}]+\}', ''
    
    # Remove Thymeleaf expression syntax: @{...}
    # But convert @{/path} to /path for href/action attributes
    $content = $content -replace '@\{([^}]+)\}', '$1'
    
    # Remove Thymeleaf inline expressions: [[...]]
    $content = $content -replace '\[\[[^\]]+\]\]', ''
    
    # Remove Thymeleaf inline expressions: [(...)]
    $content = $content -replace '\[\([^\)]+\)\]', ''
    
    # Remove Thymeleaf utility expressions: ~{...}
    $content = $content -replace '~\{[^}]+\}', ''
    
    # Remove Thymeleaf fragment expressions: ~{...}
    $content = $content -replace '~\{[^}]+\}', ''
    
    # Remove # expressions (utility methods)
    $content = $content -replace '#[a-zA-Z]+\.[a-zA-Z]+\([^)]*\)', ''
    
    # Fix empty href attributes
    $content = $content -replace 'href=""', 'href="#"'
    $content = $content -replace "href=''", "href='#'"
    
    # Fix empty action attributes
    $content = $content -replace 'action=""', 'action="#"'
    $content = $content -replace "action=''", "action='#'"
    
    # Fix empty src attributes
    $content = $content -replace 'src=""', 'src="#"'
    $content = $content -replace "src=''", "src='#'"
    
    # Remove empty class attributes
    $content = $content -replace ' class=""', ''
    $content = $content -replace " class=''", ''
    
    # Remove empty id attributes
    $content = $content -replace ' id=""', ''
    $content = $content -replace " id=''", ''
    
    # Remove empty style attributes
    $content = $content -replace ' style=""', ''
    $content = $content -replace " style=''", ''
    
    # Clean up multiple spaces
    $content = $content -replace '  +', ' '
    
    # Clean up spaces before closing tags
    $content = $content -replace ' >', '>'
    
    # Ensure proper DOCTYPE if missing
    if ($content -notmatch '<!DOCTYPE') {
        if ($content -match '<html') {
            $content = '<!DOCTYPE html>' + "`n" + $content
        }
    }
    
    # Ensure html tag has lang attribute if missing
    if ($content -match '<html[^>]*>' -and $content -notmatch '<html[^>]*lang') {
        $content = $content -replace '<html([^>]*)>', '<html lang="bg"$1>'
    }
    
    # Ensure charset meta tag exists
    if ($content -match '<head' -and $content -notmatch 'charset') {
        $metaCharset = "`n    <meta charset=`"UTF-8`">"
        $content = $content -replace '(<head[^>]*>)', "`$1$metaCharset"
    }
    
    # Ensure viewport meta tag exists
    if ($content -match '<head' -and $content -notmatch 'viewport') {
        $metaViewport = "`n    <meta name=`"viewport`" content=`"width=device-width, initial-scale=1.0`">"
        $content = $content -replace '(<head[^>]*>)', "`$1$metaViewport"
    }
    
    # Only write if content changed
    if ($content -ne $originalContent) {
        [System.IO.File]::WriteAllText($filePath, $content, [System.Text.Encoding]::UTF8)
        Write-Host "  ✓ Fixed: $(Split-Path $filePath -Leaf)" -ForegroundColor Green
        return $true
    } else {
        Write-Host "  - No changes needed: $(Split-Path $filePath -Leaf)" -ForegroundColor Gray
        return $false
    }
}

# Get all HTML files except the conversion guide
$htmlFiles = Get-ChildItem -Path $templatesPath -Filter "*.html" | Where-Object { $_.Name -ne "THYMELEAF_CONVERSION_GUIDE.md" }

$totalFiles = $htmlFiles.Count
$fixedFiles = 0

Write-Host "`n=== Preparing HTML Templates for Pure HTML ===" -ForegroundColor Yellow
Write-Host "Found $totalFiles HTML files to process`n" -ForegroundColor Yellow

foreach ($file in $htmlFiles) {
    if (Prepare-PureHtml -filePath $file.FullName) {
        $fixedFiles++
    }
}

Write-Host "`n=== Summary ===" -ForegroundColor Yellow
Write-Host "Total files processed: $totalFiles" -ForegroundColor Cyan
Write-Host "Files modified: $fixedFiles" -ForegroundColor Green
Write-Host "Files unchanged: $($totalFiles - $fixedFiles)" -ForegroundColor Gray
Write-Host "`nAll HTML templates are now pure HTML! ✓" -ForegroundColor Green

