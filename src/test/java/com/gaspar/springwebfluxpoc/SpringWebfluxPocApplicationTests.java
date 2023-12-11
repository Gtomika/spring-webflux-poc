package com.gaspar.springwebfluxpoc;

import com.gaspar.springwebfluxpoc.config.properties.ExternalApiProperties;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0) //set WireMock to random port and override property
@SpringBootTest(properties = "external-api.url=http://localhost:${wiremock.server.port}")
@ActiveProfiles({"test", "disable-caching"})
class SpringWebfluxPocApplicationTests {

	public static final String USER_ID_PATH_VARIABLE = "\\{userId\\}";
	private static final String WIREMOCK_UUID_PATTERN = "([0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12})";

	@Autowired
	private ExternalApiProperties externalApiProperties;

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void shouldAggregateData_whenAllExternalApiEndpointsRespondSuccessfully() throws Exception {
		stubUserPathWithSuccessfulResponse();
		stubReservationsPathWithSuccessfulResponse();
		stubPaymentsPathWithSuccessfulResponse();

		String userId = UUID.randomUUID().toString();
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/aggregation/{userId}", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user.userId", Matchers.equalTo(userId)))
				.andExpect(jsonPath("$.user.name", Matchers.equalTo("Test User")))
				.andExpect(jsonPath("$.reservations", Matchers.hasSize(2)))
				.andExpect(jsonPath("$.payments", Matchers.hasSize(3)));
	}

	@Test
	public void shouldAggregateData_whenPaymentsApiReturnsUnauthorized_otherApisReturnsSuccessfully() throws Exception {
		stubUserPathWithSuccessfulResponse();
		stubReservationsPathWithSuccessfulResponse();
		stubPaymentsPathWithErrorResponse();

		String userId = UUID.randomUUID().toString();
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/aggregation/{userId}", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user.userId", Matchers.equalTo(userId)))
				.andExpect(jsonPath("$.user.name", Matchers.equalTo("Test User")))
				.andExpect(jsonPath("$.reservations", Matchers.hasSize(2)))
				.andExpect(jsonPath("$.payments", Matchers.nullValue()));
	}

	@Test
	public void shouldAggregateData_whenReservationsApiTimesOut_otherApisReturnsSuccessfully() throws Exception {
		stubUserPathWithSuccessfulResponse();
		stubReservationsPathWithTimeoutResponse();
		stubPaymentsPathWithSuccessfulResponse();

		String userId = UUID.randomUUID().toString();
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/aggregation/{userId}", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user.userId", Matchers.equalTo(userId)))
				.andExpect(jsonPath("$.user.name", Matchers.equalTo("Test User")))
				.andExpect(jsonPath("$.reservations", Matchers.nullValue()))
				.andExpect(jsonPath("$.payments", Matchers.hasSize(3)));
	}

	private void stubUserPathWithSuccessfulResponse() {
		String userPathPattern = externalApiProperties.userPath().replaceAll(USER_ID_PATH_VARIABLE, WIREMOCK_UUID_PATTERN);
		stubFor(get(urlPathMatching(userPathPattern))
				.withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + externalApiProperties.apiKey()))
				.withHeader(HttpHeaders.USER_AGENT, matching(".*"))
				.willReturn(aResponse()
						.withTransformers("response-template")
						.withStatus(200)
						.withBodyFile("user-response.json")
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
	}

	private void stubReservationsPathWithSuccessfulResponse() {
		String reservationsPathPattern = externalApiProperties.reservationsPath().replaceAll(USER_ID_PATH_VARIABLE, WIREMOCK_UUID_PATTERN);
		stubFor(get(urlPathMatching(reservationsPathPattern))
				.withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + externalApiProperties.apiKey()))
				.withHeader(HttpHeaders.USER_AGENT, matching(".*"))
				.willReturn(aResponse()
						.withStatus(200)
						.withBodyFile("reservations-response.json")
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
	}

	private void stubReservationsPathWithTimeoutResponse() {
		String reservationsPathPattern = externalApiProperties.reservationsPath().replaceAll(USER_ID_PATH_VARIABLE, WIREMOCK_UUID_PATTERN);
		stubFor(get(urlPathMatching(reservationsPathPattern))
				.withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + externalApiProperties.apiKey()))
				.withHeader(HttpHeaders.USER_AGENT, matching(".*"))
				.willReturn(aResponse()
						.withStatus(200)
						.withFixedDelay(externalApiProperties.timeoutMillis() + 1000)
						.withBodyFile("reservations-response.json")
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
	}

	private void stubPaymentsPathWithSuccessfulResponse() {
		String paymentsPathPattern = externalApiProperties.paymentHistoryPath().replaceAll(USER_ID_PATH_VARIABLE, WIREMOCK_UUID_PATTERN);
		stubFor(get(urlPathMatching(paymentsPathPattern))
				.withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + externalApiProperties.apiKey()))
				.withHeader(HttpHeaders.USER_AGENT, matching(".*"))
				.willReturn(aResponse()
						.withStatus(200)
						.withBodyFile("payments-response.json")
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
	}

	private void stubPaymentsPathWithErrorResponse() {
		String paymentsPathPattern = externalApiProperties.paymentHistoryPath().replaceAll(USER_ID_PATH_VARIABLE, WIREMOCK_UUID_PATTERN);
		stubFor(get(urlPathMatching(paymentsPathPattern))
				.withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + externalApiProperties.apiKey()))
				.withHeader(HttpHeaders.USER_AGENT, matching(".*"))
				.willReturn(aResponse()
						.withStatus(403)
						.withBodyFile("payments-error-response.json")
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
	}

}
