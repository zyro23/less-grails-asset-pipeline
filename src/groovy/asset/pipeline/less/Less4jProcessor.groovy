package asset.pipeline.less
import asset.pipeline.AssetHelper

import org.springframework.core.io.ClassPathResource
import groovy.util.logging.Log4j
import asset.pipeline.CacheManager
import com.github.sommeri.less4j.*
import com.github.sommeri.less4j.core.*

@Log4j
class Less4jProcessor {
  
  def precompilerMode

  Less4jProcessor(precompiler=false){
    this.precompilerMode = precompiler
  }



  def process(input, assetFile) {
      try { 
        def assetRelativePath = relativePath(assetFile.file)


        def paths = AssetHelper.getAssetPaths()
        def relativePaths = paths.collect { [it,assetRelativePath].join(AssetHelper.DIRECTIVE_FILE_SEPARATOR)}
        paths = relativePaths + paths


        def lessSource = new AssetPipelineLessSource(assetFile.file,input,[paths: paths, baseFile: assetFile])

        LessCompiler compiler = new ThreadUnsafeLessCompiler();
        def compilationResult = compiler.compile(lessSource);

        def result = compilationResult.getCss()
        return result
      } catch (Exception e) {
        if(precompilerMode) {
          def errorDetails = "LESS Engine Compiler Failed - ${assetFile.file.name}.\n"
          errorDetails += "**Did you mean to compile this file individually (check docs on exclusion)?**\n"
          log.error(errorDetails,e)
        } else {
          throw e
        }
      }
  }


  def relativePath(file, includeFileName=false) {
    def path
    if(includeFileName) {
      path = file.class.name == 'java.io.File' ? file.getCanonicalPath().split(AssetHelper.QUOTED_FILE_SEPARATOR) : file.file.getCanonicalPath().split(AssetHelper.QUOTED_FILE_SEPARATOR)
    } else {
      path = file.getParent().split(AssetHelper.QUOTED_FILE_SEPARATOR)
    }

    def startPosition = path.findLastIndexOf{ it == "grails-app" }
    if(startPosition == -1) {
      startPosition = path.findLastIndexOf{ it == 'web-app' }
      if(startPosition+2 >= path.length) {
        return ""
      }
      path = path[(startPosition+2)..-1]
    }
    else {
      if(startPosition+3 >= path.length) {
        return ""
      }
      path = path[(startPosition+3)..-1]
    }

    return path.join(AssetHelper.DIRECTIVE_FILE_SEPARATOR)
  }
}
