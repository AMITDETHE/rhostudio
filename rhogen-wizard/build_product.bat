java ^
-jar "C:\Android\installer\eclipse/plugins/org.eclipse.equinox.launcher_1.2.0.v20110502.jar" ^
-application org.eclipse.ant.core.antRunner ^
-buildfile "C:\Android\installer\eclipse/plugins/org.eclipse.pde.build_3.7.0.v20111116-2009/scripts/productBuild/productBuild.xml" ^
-Dbuilder="C:\Android\rhostudio\rhogen-wizard\build-setting" ^
-Dbase=c: ^
-DbaseLocation=C:/Android/installer/eclipse ^
-DpluginPath=C:\Android\installer\eclipse\delta-pack\plugins;C:\Android\installer\eclipse\delta-pack\features;C:\Android\installer\eclipse\plugins; ^
-Dproduct=C:/Android/rhostudio/rhogen-wizard/rhostudio.product ^
-Dconfigs="win32, win32, x86 ^& win32, win32, x86_64" ^
-Dp2.gathering=true