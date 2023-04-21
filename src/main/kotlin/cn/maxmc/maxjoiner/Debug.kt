package cn.maxmc.maxjoiner

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

fun main() {
    val pool = Executors.newFixedThreadPool(10)
    val future = CompletableFuture.allOf()
    future.thenAccept {
        println("Finish!")
    }

    future.join()
//    repeat(10) {
//        val start = System.currentTimeMillis()
//        val futures = ArrayList<CompletableFuture<Void?>>()
//        repeat(10) {
//            val futuresChildList = ArrayList<CompletableFuture<Unit>>()
//            repeat(10) {
//                val oneFuture = CompletableFuture<Unit>()
//                futuresChildList.add(oneFuture)
//                pool.submit {
//                    Thread.sleep(10)
//                    println("Finish! (${Thread.currentThread().name})")
//                    oneFuture.complete(Unit)
//                }
//            }
//            val futuresChild = CompletableFuture.allOf(*futuresChildList.toTypedArray())
//            futures.add(futuresChild)
//        }
//        val global = CompletableFuture.allOf(*futures.toTypedArray())
//        global.thenAccept {
//            println("总耗时: ${System.currentTimeMillis() - start} ms")
//        }
//    }
//
//    pool.shutdown()
}