package ogs.build;

def linux(buildDir, target = null, cmd = "make -j \$(nproc)") {
    if (target == null) {
        target = 'all'
        if (ogs.helper.isRelease())
            target = 'package'
    }
    sh "cd ${buildDir} && ${cmd} ${target}"
}

def win(buildDir, target = null) {
    targetString = ""
    if (target == null && ogs.helper.isRelease())
        targetString = "--target package"
    else if (target != null)
        targetString = "--target ${target}"

    vcvarsallParam = "amd64"
    if (buildDir.endsWith("32"))
        vcvarsallParam = "x86"

    bat("""set path=%path:\"=%
           call "%vs120comntools%..\\..\\VC\\vcvarsall.bat" ${vcvarsallParam}
           cd ${buildDir}
           cmake --build . --config Release ${targetString}""".stripIndent())
}
