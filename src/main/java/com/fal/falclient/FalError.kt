package com.fal.falclient

open class FalError(message: String): Exception(message) {
    class InvalidResultFormat : FalError("The result format is invalid.")
    class QueueTimeout : FalError("The queue operation has timed out.")
}