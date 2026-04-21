package com.example.teleport_assignment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class TeleportAssignmentApplication

fun main(args: Array<String>) {
	runApplication<TeleportAssignmentApplication>(*args)
}
