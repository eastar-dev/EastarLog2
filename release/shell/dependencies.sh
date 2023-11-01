#export JAVA_HOME='/Applications/Android Studio.app/Contents/jre/Contents/Home'
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
cd ../../
sh ./gradlew :app:dependencies > dependencies.log

