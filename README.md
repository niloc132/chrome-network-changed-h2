# Test sample for Chromium network changed http2 bug

This app starts on three ports:
 * 8080 for plaintext http/1.1
 * 8081 for http/1.1 over tls
 * 8082 for h2 (over tls, browsers can't use h2 any other way)

To run the second two, an ssl cert is required in pkcs12 format, either provide your own or use 
[the generated one](cert.md). These can be passed via system properties to the server to indicate
which cert to use - `h1certpath`/`h2certpath` and `h1secret`/`h2secret`, with defaults of
`selfsigned.p12` and `secret` respectively.

To run the server, invoke 
```
./mvnw exec:java -Dexec.mainClass=com.example.ServerMain
```
or using docker, 
```
docker-compose up --build
```

Open any of the following (each has links to the others): http://localhost:8080, https://localhost:8081, https://localhost:8082.

Can also load these directly to observe the http streams as they load as `text/plain`, but these tend to not display
content in the browser until running for several minutes. They can still be used to watch the "Network" or "Console"
tabs in browser dev tools.
https://localhost:8080/stream
https://localhost:8081/stream
https://localhost:8082/stream

Once contents are streaming, to reproduce the issue, modify your system's network settings in some trivial way:
 * On MacOS, opening network settings and clicking "Apply" with no chances will cause this, as will enabling/disabling unrelated network interfaces
 * On Linux, changing network interfaces will cause this, such as creating/removing docker networks, or starting/stopping other containers
 * 