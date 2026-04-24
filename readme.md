[//]: # (запуск)
$out = "out"
New-Item -ItemType Directory -Force $out | Out-Null
javac -encoding UTF-8 -d $out (Get-ChildItem -Recurse .\src -Filter *.java).FullName
java -cp $out Main