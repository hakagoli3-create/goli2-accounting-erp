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

@org.junit.Ignore("Disabled in CI builds")
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

    @Test
    fun `test database user management and loginWithDb`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.database.AppDatabase.getInstance(context)
        val securityManager = AppSecurityAndSettingsManager(context)

        // Ensure default admin exists
        com.example.data.database.DatabaseSeeder.ensureDefaultUsers(db)
        val admin = db.userDao().getUserByUsername("admin")
        org.junit.Assert.assertNotNull(admin)
        assertEquals("admin", admin?.username)
        assertEquals("1234", admin?.passwordHash)

        // Login with DB as admin
        val (adminSuccess, adminError) = securityManager.loginWithDb("admin", "1234", db)
        assertTrue(adminSuccess)
        org.junit.Assert.assertNull(adminError)
        assertEquals("admin", securityManager.loggedInUsername.value)
        assertEquals(UserRole.ADMIN, securityManager.currentRole.value)

        // Create a new seller user
        val sellerUser = com.example.data.model.UserEntity(
            username = "seller_test",
            passwordHash = "pass123",
            fullName = "علی فروشنده",
            role = UserRole.POS_USER.name,
            isActive = true
        )
        db.userDao().insertUser(sellerUser)

        // Test successful seller login
        val (sellerSuccess, _) = securityManager.loginWithDb("seller_test", "pass123", db)
        assertTrue(sellerSuccess)
        assertEquals(UserRole.POS_USER, securityManager.currentRole.value)

        // Test wrong password
        val (wrongPassSuccess, wrongPassError) = securityManager.loginWithDb("seller_test", "wrong", db)
        assertFalse(wrongPassSuccess)
        org.junit.Assert.assertNotNull(wrongPassError)

        // Deactivate seller account
        val fetchedSeller = db.userDao().getUserByUsername("seller_test")!!
        db.userDao().updateUserStatus(fetchedSeller.id, false)

        // Test login with inactive account
        val (inactiveSuccess, inactiveError) = securityManager.loginWithDb("seller_test", "pass123", db)
        assertFalse(inactiveSuccess)
        assertTrue(inactiveError?.contains("غیرفعال") == true)

        // Update password
        db.userDao().updatePassword(fetchedSeller.id, "newpass456")
        db.userDao().updateUserStatus(fetchedSeller.id, true)
        val (newPassSuccess, _) = securityManager.loginWithDb("seller_test", "newpass456", db)
        assertTrue(newPassSuccess)

        // Cleanup
        db.userDao().deleteUserById(fetchedSeller.id)
        org.junit.Assert.assertNull(db.userDao().getUserByUsername("seller_test"))
    }
}
