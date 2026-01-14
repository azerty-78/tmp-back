package com.kobecorporation.tmp_back

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@EnableReactiveMongoRepositories
@ConfigurationPropertiesScan
@SpringBootApplication
class TmpBackApplication

fun main(args: Array<String>) {
	runApplication<TmpBackApplication>(*args)
}
