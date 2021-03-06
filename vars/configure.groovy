def call(body) {
  // evaluate the body block, and collect configuration into the object
  def map = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = map
  body()

  def isUnix = isUnix()

  // defaults
  if (!map.containsKey('arch'))
    map['arch'] = 'x86_64'
  if (!map.containsKey('config'))
    map['config'] = 'Release'
  if (!map.containsKey('dir'))
    map['dir'] = 'build'
  if (!map.containsKey('sourceDir'))
    map['sourceDir'] = '.'
  if (!map.containsKey('keepDir'))
    map['keepDir'] = false

  if (!map.containsKey('generator')) {
    map['generator'] = 'Ninja'
  }

  def script = ""

  if (!isUnix) {
    // Win-specific
    vcvarsllDir = "%vs${env.MSVC_NUMBER}0comntools%..\\..\\VC"
    if ((env.MSVC_NUMBER as Integer) >= 15)
      vcvarsllDir = "C:\\Program Files (x86)\\Microsoft Visual Studio\\${env.MSVC_VERSION}\\Community\\VC\\Auxiliary\\Build"

    vcvarsallParam = "amd64"
    if (map.arch == "x86")
      vcvarsallParam = "x86"
    script += ":: set path=%path:\"=%\n"
    script += "call \"${vcvarsllDir}\\vcvarsall.bat\" ${vcvarsallParam}\n"
  }

  if (map.env != null)
    script += ". ${map.sourceDir}/scripts/env/${map.env}\n"
  if (map.keepDir == false) {
    if (isUnix)
      script += "rm -rf ${map.dir} && mkdir ${map.dir}\n"
    else
      script += "rd /S /Q ${map.dir} 2>nul & mkdir ${map.dir}\n"
  }

  script += "(cd ${map.dir} && cmake ../${map.sourceDir} -G \"${map.generator}\" -DCMAKE_BUILD_TYPE=${map.config} ${map.cmakeOptions})\n"

  if (isUnix)
    sh "${script}"
  else
    bat "${script}"
}
