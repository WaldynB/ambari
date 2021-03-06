rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.


@echo on
echo Copying resources
pushd ..
copy /y "%cd%\ambari-scom-server\target\ambari-scom-*.*" "%cd%\msi\src\AmbariPackages\ambari-winpkg\resources\" || exit /b 1
copy /y "%cd%\metrics-sink\target\metrics-sink-*.jar" "%cd%\msi\src\AmbariPackages\ambari-winpkg\resources\" || exit /b 1
copy /y "%cd%\metrics-sink\db\Hadoop-Metrics-SQLServer-CREATE.ddl" "%cd%\msi\src\AmbariPackages\ambari-winpkg\resources\" || exit /b 1
popd

echo Compressing resources
powershell.exe -NoProfile -InputFormat none -ExecutionPolicy unrestricted -command "%cd%\build\zip.ps1" "%cd%\src\AmbariPackages\ambari-winpkg" "%cd%\src\AmbariPackages\ambari-winpkg.zip" || exit /b 1

echo Building GUI
set msBuildDir=%WINDIR%\Microsoft.NET\Framework\v4.0.30319
call %msBuildDir%\msbuild.exe "%cd%\src\GUI_Ambari\GUI_Ambari.csproj"  || exit /b 1
copy /y "%cd%\src\GUI_Ambari\bin\Debug\GUI_Ambari.exe" "%cd%\src\bin\GUI_Ambari.exe" || exit /b 1
mkdir "%cd%\src\bin\"
copy /y "%cd%\src\Result\bin\Debug\Ambari_Result.exe" "%cd%\src\bin\Ambari_Result.exe" || exit /b 1

echo Building MSI
pushd "%cd%\src" || exit /b 1
candle "%cd%\ambari-scom.wxs"  2>&1 || exit /b 1
light "%cd%\ambari-scom.wixobj" -ext WixUtilExtension.dll 2>&1 || exit /b 1
if not exist "%cd%\ambari-scom.msi" (
echo MSI build failed
exit /b 1
)
popd || exit /b 1
copy /y "%cd%\src\ambari-scom.msi" "%cd%\ambari-scom.msi" || exit /b 1
copy /y "..\management-pack\Hadoop_MP\Installer\bin\Debug\en-us\AmbariSCOMManagementPack.msi" "%cd%\AmbariSCOMManagementPack.msi" || exit /b 1

echo Cleaning 
del /f /q "%cd%\src\ambari-scom.wixobj"  || exit /b 1
del /f /q "%cd%\src\ambari-scom.wixpdb"  || exit /b 1
del /f /q "%cd%\src\ambari-scom.msi"  || exit /b 1
attrib -r -s -h "%cd%\src\GUI_Ambari.v11.suo" || exit /b 1
del /f /q "%cd%\src\GUI_Ambari.v11.suo"  || exit /b 1
del /f /q "%cd%\src\bin\GUI_Ambari.exe"  || exit /b 1
del /f /q "%cd%\src\bin\Ambari_Result.exe"  || exit /b 1
del /f /q "%cd%\src\AmbariPackages\ambari-winpkg.zip" || exit /b 1
del /f /q "%cd%\src\AmbariPackages\ambari-winpkg\resources\*.jar"  || exit /b 1
del /f /q "%cd%\src\AmbariPackages\ambari-winpkg\resources\*.zip"  || exit /b 1
del /f /q "%cd%\src\AmbariPackages\ambari-winpkg\resources\Hadoop-Metrics-SQLServer-CREATE.ddl"  || exit /b 1
rd /s /q "%cd%\src\GUI_Ambari\bin"  || exit /b 1
rd /s /q "%cd%\src\GUI_Ambari\obj"  || exit /b 1
echo Done
