package com.mtw.supplier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class SupplierApplication

@RestController
class RootController {
	@GetMapping("/health")
	fun health(): Boolean{
		return true
	}
}

fun main(args: Array<String>) {
	runApplication<SupplierApplication>(*args)
}
