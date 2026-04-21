package com.example.teleport_assignment.controller

import com.example.teleport_assignment.exchange_model.LoadOptimizerRequest
import com.example.teleport_assignment.exchange_model.LoadOptimizerResponse
import com.example.teleport_assignment.service.LoadOptimizerService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/load-optimizer")
class LoadOptimizerController(
    @Autowired val loadOptimizerService: LoadOptimizerService
) {

    @PostMapping("/optimize")
    fun optimizeLoad(@RequestBody @Valid request: LoadOptimizerRequest): LoadOptimizerResponse {
        return loadOptimizerService.optimizeLoad(request)
    }
}