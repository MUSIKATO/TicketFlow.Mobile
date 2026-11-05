package com.devcore.ticketflow.console.interfaces

interface CrudService<T, ID> {
    fun create(item: T)
    fun readAll(): List<T>
    fun findById(id: ID): T?
    fun update(item: T)
    fun delete(id: ID): Boolean
}
