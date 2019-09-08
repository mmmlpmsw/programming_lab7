    
#!/usr/bin/env bash
shopt -s expand_aliases

sh clear.sh;

mkdir build;
mkdir jars;

if [[ $1 -eq "18" ]]; then
  alias javac=javac18;
fi

(
  echo "Компиляция клиента...";
  javac -cp lib/javax.mail.jar -sourcepath src -d build src/ifmo/programming/lab7/client/Client.java -encoding UTF-8
) && (
  echo "Сборка клиентского jar...";
  jar cfm jars/ClientApp.jar manifests/CLIENT_MANIFEST.MF -C build .
) && (
  echo "Компиляция сервера...";
  javac -cp lib/javax.mail.jar -sourcepath src -d build src/ifmo/programming/lab7/server/Server.java -encoding UTF-8
) && (
  echo "Сборка серверного jar...";
  jar cfm jars/ServerApp.jar manifests/SERVER_MANIFEST.MF -C build .
) && (
  echo "Готово!";
)

if [[ $1 = "18" ]]; then
  unalias javac;
fi