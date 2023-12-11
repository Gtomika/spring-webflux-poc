# Spring WebFlux and Caching PoC

This application shows the following:

- How to configure Spring WebFlux and make parallel HTTP requests, and aggregate the results.
- How to configure Spring Caching with Redis to reduce amount of outgoing requests.

Since this is a PoC, only local running is supported. All requests (both incoming and outgoing), and 
all caching operations are verbosely logged.

# Application overview

This diagram illustrates how the application works.

![Architecture diagram](./images/spring-webflux-poc.drawio.png)

An artificial delay is added to WireMock responses, so that the effectiveness of caching can be seen. Dummy 
authentication is used in the form of a bearer token, which WireMock checks.

# Aggregation and error handling

If one of the external API requests returns an error, or times out, this value 
will be replaced by *null* in the aggregated response.

For example, if user API and reservations API are successful, but payment API times 
out, we will see a result like this:

```json
{
  "user": {
    "userId": "8b9c71dc-3706-41f5-884d-370a428acac6",
    "name": "Test User",
    "email": "test.user@gmail.com",
    "birthday": "1985-02-13"
  },
  "reservations": [
    {
      "hotelName": "Hilton",
      "startDate": "2022-01-01",
      "endDate": "2022-01-05"
    }
  ],
  "payments": null
}
```

It is also possible to configure a "fail-early" approach, where, if one of the 
external APIs fail, the entire aggregation is failed.

# Effects of caching and parallelism

This table shows the response times from the app:

| Cache miss | Cache hit |
|------------|-----------|
| 4037 ms    | 27 ms     |

Two conclusions can be drawn:

- Caching is highly effective when the operations that takes a long time is repeated.
- There is true parallelism happening inside Spring WebFlux. Each request is delayed by 3 seconds by WireMock, so if the requests 
were sequential, the time of completion would be at least 9 seconds, but it was only around 4.

# How to run

Start the required containers:

```bash
docker compose up -d
```

This will launch a Redis and a WireMock container. Redis is the cache backend, while WireMock is the substitute 
for a real external API.

Now the application is ready to start. Either use a run configuration in your IDE, or it can be done 
with the following Maven command:

```bash
mvn spring-boot:run
```

To run the tests that launch the entire app and use WireMock:

```bash
mvn test
```