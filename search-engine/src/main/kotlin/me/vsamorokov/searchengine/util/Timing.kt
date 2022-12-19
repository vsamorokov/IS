package me.vsamorokov.searchengine.util

import org.jboss.logging.Logger


fun measureTime(blockName: String, log: Logger, block: () -> Unit) {
    val start = System.currentTimeMillis()
    block()
    log.info("$blockName took ${(System.currentTimeMillis() - start).div(1000.0)} seconds")
}