package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("مدیریت حسابداری گلی", appName)
    }

    @Test
    fun `test default admin login and session persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val securityManager = AppSecurityAndSettingsManager(context)

        // Invalid credentials test
        val failLogin = securityManager.login("admin", "wrong_pass")
        assertFalse(failLogin)

        // Valid default admin login
        val successLogin = securityManager.login("admin", "1234")
        assertTrue(successLogin)
        assertTrue(securityManager.isLoggedIn.value)
        assertEquals(UserRole.ADMIN, securityManager.currentRole.value)
        assertEquals("admin", securityManager.loggedInUsername.value)

        // Session check: creating a new instance on same context preserves login
        val restoredManager = AppSecurityAndSettingsManager(context)
        assertTrue(restoredManager.isLoggedIn.value)
        assertEquals(UserRole.ADMIN, restoredManager.currentRole.value)

        // Logout
        securityManager.logout()
        assertFalse(securityManager.isLoggedIn.value)
    }

    @Test
    fun `test role based access permissions`() {
        // Admin
        assertTrue(UserRole.ADMIN.canViewProfit)
        assertTrue(UserRole.ADMIN.canDeleteDocuments)
        assertTrue(UserRole.ADMIN.canAccessSettings)
        assertTrue(UserRole.ADMIN.canAccessAccountingLedger)

        // POS User / Seller
        assertFalse(UserRole.POS_USER.canViewProfit)
        assertFalse(UserRole.POS_USER.canDeleteDocuments)
        assertFalse(UserRole.POS_USER.canAccessSettings)
        assertTrue(UserRole.POS_USER.canAccessPos)

        // Visitor
        assertFalse(UserRole.VISITOR.canViewProfit)
        assertFalse(UserRole.VISITOR.canDeleteDocuments)
        assertFalse(UserRole.VISITOR.canAccessSettings)
        assertFalse(UserRole.VISITOR.canAccessPos)
        assertTrue(UserRole.VISITOR.isVisitorOnly)
    }
}
