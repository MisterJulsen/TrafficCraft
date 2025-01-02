@echo off
setlocal

rem Sucht alle Verzeichnisse mit der Endung .gradle im aktuellen Ordner und allen Unterordnern
for /d /r %%G in (*.gradle) do (
    echo Lösche Ordner: %%G
    rd /s /q "%%G"
)

echo Fertig!
endlocal
