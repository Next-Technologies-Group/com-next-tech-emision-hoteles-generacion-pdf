FROM amazoncorretto:11.0.19
EXPOSE 8080
ENV TZ=America/Mexico_City
COPY target/generacion-pdf-*.jar  generacion-pdf.jar
CMD java -jar generacion-pdf.jar