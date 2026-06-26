Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead('e:\UPN\2026-1\DESARROLLO DE APLICATIVOS MOVILES\SEMANA 13 (T3)\MyOyichan\Projects\Informe_MyOyichan_T2.docx')
$entry = $zip.Entries | Where-Object { $_.FullName -eq 'word/document.xml' }
if ($entry) {
    $stream = $entry.Open()
    $reader = New-Object System.IO.StreamReader($stream)
    $xml = $reader.ReadToEnd()
    $reader.Close()
    $zip.Dispose()
    # Simple regex to remove xml tags
    $text = $xml -replace '<[^>]+>', ' '
    # Remove extra spaces
    $text = $text -replace '\s+', ' '
    Write-Output $text
} else {
    Write-Output "document.xml not found"
}
