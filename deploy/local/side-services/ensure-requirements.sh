set -e
cd $(dirname $0)

projectPomFile=../../../implementation/pom.xml

echo "Cleaning up..."
mvn -DskipTests -B clean --file $projectPomFile

echo "Building..."
mvn -DskipTests -B package --file $projectPomFile