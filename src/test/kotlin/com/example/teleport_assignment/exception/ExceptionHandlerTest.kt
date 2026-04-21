package com.example.teleport_assignment.exception

import com.example.teleport_assignment.controller.LoadOptimizerController
import com.example.teleport_assignment.service.LoadOptimizerService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(LoadOptimizerController::class)
class ExceptionHandlerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var loadOptimizerService: LoadOptimizerService

    companion object {
        private const val URL = "/api/v1/load-optimizer/optimize"

        private val VALID_REQUEST = """
            {
              "truck": { "id": "truck-1", "max_weight_lbs": 44000, "max_volume_cuft": 3000 },
              "orders": [
                {
                  "id": "ord-001",
                  "payout_cents": 100000,
                  "weight_lbs": 5000,
                  "volume_cuft": 300,
                  "origin": "A",
                  "destination": "B",
                  "pickup_date": "2026-04-21",
                  "delivery_date": "2026-04-22",
                  "is_hazmat": false
                }
              ]
            }
        """.trimIndent()
    }

    @Test
    fun `should return 400 with REQ_VALIDATION_ERROR when service throws IllegalArgumentException`() {
        whenever(loadOptimizerService.optimizeLoad(any()))
            .thenThrow(IllegalArgumentException("Too many orders: 31. Maximum supported is 25"))

        mockMvc.post(URL) {
            contentType = MediaType.APPLICATION_JSON
            content = VALID_REQUEST
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("REQ_VALIDATION_ERROR") }
            jsonPath("$.errorMessage") { value("Too many orders: 31. Maximum supported is 25") }
            jsonPath("$.details.path") { value(URL) }
        }
    }


    @Test
    fun `should return 400 with MALFORMED_REQUEST when JSON body is completely malformed`() {
        mockMvc.post(URL) {
            contentType = MediaType.APPLICATION_JSON
            content = "{ gibberish"
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("MALFORMED_REQUEST") }
            jsonPath("$.errorMessage") { value("Request is not readable") }
            jsonPath("$.details.path") { value(URL) }
        }
    }

    @Test
    fun `should return 400 with MALFORMED_REQUEST when date field has invalid format`() {
        val badDateRequest = VALID_REQUEST.replace("2026-04-21", "21-04-2026")

        mockMvc.post(URL) {
            contentType = MediaType.APPLICATION_JSON
            content = badDateRequest
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("MALFORMED_REQUEST") }
            jsonPath("$.details.path") { value(URL) }
        }
    }

    @Test
    fun `should return 400 with MALFORMED_REQUEST when request body is empty`() {
        mockMvc.post(URL) {
            contentType = MediaType.APPLICATION_JSON
            content = ""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("MALFORMED_REQUEST") }
        }
    }

    @Test
    fun `should return 400 with REQ_VALIDATION_ERROR when jakarta validation fails`() {
        val invalidRequest = """
            {
              "truck": { "id": "truck-1", "max_weight_lbs": 44000, "max_volume_cuft": 3000 },
              "orders": [
                {
                  "id": "ord-001",
                  "payout_cents": -100000,
                  "weight_lbs": 5000,
                  "volume_cuft": 300,
                  "origin": "A",
                  "destination": "B",
                  "pickup_date": "2026-04-21",
                  "delivery_date": "2026-04-22",
                  "is_hazmat": false
                }
              ]
            }
        """.trimIndent()

        mockMvc.post(URL) {
            contentType = MediaType.APPLICATION_JSON
            content = invalidRequest
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value("REQ_VALIDATION_ERROR") }
            jsonPath("$.errorMessage") { value("orders[0].payoutCents: order.payout_cents must be greater than 0") }
            jsonPath("$.details.path") { value(URL) }
        }
    }

    @Test
    fun `should return 500 with INTERNAL_SERVER_ERROR when service throws unexpected exception`() {
        whenever(loadOptimizerService.optimizeLoad(any()))
            .thenThrow(RuntimeException("Unexpected failure"))

        mockMvc.post(URL) {
            contentType = MediaType.APPLICATION_JSON
            content = VALID_REQUEST
        }.andExpect {
            status { isInternalServerError() }
            jsonPath("$.errorCode") { value("INTERNAL_SERVER_ERROR") }
            jsonPath("$.errorMessage") { value("Unexpected failure") }
            jsonPath("$.details.path") { value(URL) }
        }
    }
}
