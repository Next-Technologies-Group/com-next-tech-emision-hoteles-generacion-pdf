# cfdi-4.0-pdf-generador

Generación de PDF para Comprobantes Fiscales Digitales por Internet (CFDI 4.0)
del SAT México, a partir de XML, utilizando iTextPDF.

## Tecnología

* [Java](https://www.oracle.com/java/) 11
* [Spring Boot](https://spring.io/projects/spring-boot) 2.6.3
* [Maven](https://maven.apache.org/) 3.x
* [iTextPDF](https://itextpdf.com/) 5.5.0 (AGPL v3)

## Instalación

```bash
git clone https://github.com/Next-Technologies-Group/com-next-tech-emision-hoteles-generacion-pdf.git
cd com-next-tech-emision-hoteles-generacion-pdf
mvn clean package           # build público (sin Elastic APM agent)
mvn -Papm-internal package  # build con Elastic APM agent embebido
```

Ejecución:

```bash
mvn spring-boot:run
# o
java -jar target/generacion-pdf-*.jar
```

Configuración: todas las propiedades se externalizan vía variables de entorno
(`${VAR:default}`). Consulta `src/main/resources/application.properties` para
la lista completa.

## Versionado

Usamos [SemVer](http://semver.org/). Las versiones disponibles aparecen en los
[tags del repositorio en GitHub](https://github.com/Next-Technologies-Group/com-next-tech-emision-hoteles-generacion-pdf/tags).

## Licencia

Este proyecto se distribuye bajo la **GNU Affero General Public License v3.0
o superior (AGPL-3.0-or-later)**. Esta licencia se eligió para cumplir con
los términos de [iTextPDF 5.5.0](https://itextpdf.com/), una de las
dependencias principales, distribuida también bajo AGPL v3.

* Texto íntegro de la licencia: [LICENSE](LICENSE)
* Atribuciones de terceros: [NOTICE](NOTICE)

### Cumplimiento del Artículo 13 de la AGPL v3

La AGPL v3 §13 exige que cualquier usuario que interactúe con el software a
través de la red pueda obtener el código fuente correspondiente. Esta
aplicación expone el endpoint:

```
GET ${prefix_server_path}/source
```

que devuelve un HTTP `302 Found` redirigiendo a la URL del repositorio
público con el código fuente correspondiente a la versión desplegada
(configurable vía la variable de entorno `APP_SOURCE_URL`).

## Contribuciones

Las contribuciones se aceptan bajo los términos de la AGPL v3. Al enviar un
pull request, declaras que tu aportación puede ser distribuida bajo dicha
licencia.
