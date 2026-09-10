package com.example.myeduapp.core.sound

import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.Foundation.NSUUID

actual fun playNotificationSound() {
    AudioServicesPlaySystemSound(1007u)
}

actual fun showAppNotification(title: String, message: String) {
    playNotificationSound()
    val center = UNUserNotificationCenter.currentNotificationCenter()
    center.requestAuthorizationWithOptions(7u) { granted, _ ->
        if (!granted) return@requestAuthorizationWithOptions
        val content = UNMutableNotificationContent()
        content.setTitle(title)
        content.setBody(message)
        content.setSound(UNNotificationSound.defaultSound)
        val request = UNNotificationRequest.requestWithIdentifier(
            NSUUID().UUIDString,
            content,
            null
        )
        center.addNotificationRequest(request, null)
    }
}
