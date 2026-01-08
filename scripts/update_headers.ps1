$licenseHeader = @'
/*
 * MIT License
 *
 * Copyright (c) 2026 Silvere Martin-Michiellot, Antigravity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
'@

$utf8NoBom = New-Object System.Text.UTF8Encoding $false
$javaFiles = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse

foreach ($file in $javaFiles) {
    Write-Host "Processing $($file.Name)..."
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    
    # Remove existing license headers (simple heuristic: generic comment block at start)
    if ($content -match "(?s)^\s*/\*.*?(Copyright|License).*?\*/\s*") {
         $content = $content -replace "(?s)^\s*/\*.*?(Copyright|License).*?\*/\s*", ""
    }
    
    # Prepend new license header
    $content = $licenseHeader + "`r`n" + $content
    
    # Correct Javadoc Authors/Since
    # We use string replacement for safety instead of complex regex groups which failed
    if ($content -match "(?s)(/\*\*.*?\*/)\s*(public|class|interface|enum|record|package)") {
        $fullMatch = $Matches[0]
        $javadoc = $Matches[1]
        
        $newJavadoc = $javadoc
        
        # Helper to append tag if missing
        function Append-Tag ($doc, $tagLine) {
            if ($doc -notmatch [Regex]::Escape($tagLine.Trim())) {
               return $doc.Replace("*/", "$tagLine`r`n */")
            }
            return $doc
        }
        
        $newJavadoc = Append-Tag $newJavadoc " * @author Silvere Martin-Michiellot"
        $newJavadoc = Append-Tag $newJavadoc " * @author Antigravity"
        $newJavadoc = Append-Tag $newJavadoc " * @since 1.0"
        
        $content = $content.Replace($fullMatch, $fullMatch.Replace($javadoc, $newJavadoc))
    } else {
         # Fallback for missing javadoc
         if ($content -match "(public\s+(class|interface|enum|record)\s+\w+)") {
             $classDecl = $Matches[1]
             $defaultJavadoc = "/**`r`n * @author Silvere Martin-Michiellot`r`n * @author Antigravity`r`n * @since 1.0`r`n */"
             $content = $content.Replace($classDecl, "$defaultJavadoc`r`n$classDecl")
         }
    }

    [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
}
