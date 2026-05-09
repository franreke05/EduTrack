package com.example.edutrack.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanManagerTest {

    @Test
    fun `free plan limits courses to 2`() {
        assertTrue(PlanManager.canCreateCourse(UserPlan.FREE, 0))
        assertTrue(PlanManager.canCreateCourse(UserPlan.FREE, 1))
        assertFalse(PlanManager.canCreateCourse(UserPlan.FREE, 2))
        assertFalse(PlanManager.canCreateCourse(UserPlan.FREE, 5))
    }

    @Test
    fun `premium plan has no course limit`() {
        assertTrue(PlanManager.canCreateCourse(UserPlan.PREMIUM, 0))
        assertTrue(PlanManager.canCreateCourse(UserPlan.PREMIUM, 10))
        assertTrue(PlanManager.canCreateCourse(UserPlan.PREMIUM, 100))
        assertNull(PlanManager.limitsFor(UserPlan.PREMIUM).maxCourses)
    }

    @Test
    fun `free plan cannot create groups`() {
        assertFalse(PlanManager.canCreateGroup(UserPlan.FREE))
    }

    @Test
    fun `premium plan can create groups`() {
        assertTrue(PlanManager.canCreateGroup(UserPlan.PREMIUM))
    }

    @Test
    fun `premium plan can manage groups`() {
        assertTrue(PlanManager.canManageGroup(UserPlan.PREMIUM))
    }

    @Test
    fun `free plan cannot manage groups`() {
        assertFalse(PlanManager.canManageGroup(UserPlan.FREE))
    }

    @Test
    fun `free plan can join at most 1 group`() {
        assertTrue(PlanManager.canJoinMoreGroups(UserPlan.FREE, 0))
        assertFalse(PlanManager.canJoinMoreGroups(UserPlan.FREE, 1))
        assertFalse(PlanManager.canJoinMoreGroups(UserPlan.FREE, 2))
    }

    @Test
    fun `premium plan can join unlimited groups`() {
        assertTrue(PlanManager.canJoinMoreGroups(UserPlan.PREMIUM, 0))
        assertTrue(PlanManager.canJoinMoreGroups(UserPlan.PREMIUM, 10))
        assertTrue(PlanManager.canJoinMoreGroups(UserPlan.PREMIUM, 100))
        assertNull(PlanManager.limitsFor(UserPlan.PREMIUM).maxGroupsJoined)
    }

    @Test
    fun `free plan only has basic simulator mode`() {
        assertEquals(SimulatorAccess.BASIC_APPROVE_ONLY, PlanManager.limitsFor(UserPlan.FREE).simulatorMode)
        assertFalse(PlanManager.canUseCustomTargets(UserPlan.FREE))
    }

    @Test
    fun `premium plan has full simulator access`() {
        assertEquals(SimulatorAccess.FULL, PlanManager.limitsFor(UserPlan.PREMIUM).simulatorMode)
        assertTrue(PlanManager.canUseCustomTargets(UserPlan.PREMIUM))
    }

    @Test
    fun `free plan shows ads`() {
        assertTrue(PlanManager.shouldShowAds(UserPlan.FREE))
    }

    @Test
    fun `premium plan hides ads`() {
        assertFalse(PlanManager.shouldShowAds(UserPlan.PREMIUM))
    }

    @Test
    fun `free plan cannot export pdf`() {
        assertFalse(PlanManager.canExportPdf(UserPlan.FREE))
    }

    @Test
    fun `premium plan can export pdf`() {
        assertTrue(PlanManager.canExportPdf(UserPlan.PREMIUM))
    }

    @Test
    fun `free plan has no advanced stats`() {
        assertFalse(PlanManager.canUseAdvancedStats(UserPlan.FREE))
    }

    @Test
    fun `premium plan has advanced stats`() {
        assertTrue(PlanManager.canUseAdvancedStats(UserPlan.PREMIUM))
    }

    @Test
    fun `isPremium returns correct result`() {
        assertFalse(PlanManager.isPremium(UserPlan.FREE))
        assertTrue(PlanManager.isPremium(UserPlan.PREMIUM))
    }
}
