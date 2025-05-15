package com.thanhtam.backend.service;

import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.repository.RoleRepository;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Unit Test for RoleService
 * 
 * This test class verifies the functionality of the RoleService implementation.
 * It tests the findByName method with various inputs to ensure all branches are covered.
 * 
 * Test environment:
 * - Uses Spring Boot test context
 * - Runs with transactional boundaries
 * - Performs rollback after each test
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
@Rollback // Ensures database changes are rolled back after each test
@Sql(scripts = "/cleanup-role.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-role.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class RoleServiceTest {

    // Service under test
    @Autowired
    private RoleService roleService;
    
    // Dependencies
    @Autowired
    private RoleRepository roleRepository;
    
    // Test data
    private Role adminRole;
    private Role lecturerRole;
    private Role studentRole;

    /**
     * Setup method that runs before each test.
     * Creates test roles in the database.
     */
    @Before
    public void setUp() {
        // Create test roles
        adminRole = new Role(null, ERole.ROLE_ADMIN);
        lecturerRole = new Role(null, ERole.ROLE_LECTURER);
        studentRole = new Role(null, ERole.ROLE_STUDENT);
        
        // Save roles to database
        adminRole = roleRepository.save(adminRole);
        lecturerRole = roleRepository.save(lecturerRole);
        studentRole = roleRepository.save(studentRole);
    }
    
    /**
     * Teardown method that runs after each test.
     * Cleans up any test data.
     */
    @After
    public void tearDown() {
        // Additional cleanup if needed
    }

    /**
     * Test Case ID: TC01
     * Test Objective: Verify that findByName returns the correct role for ROLE_ADMIN
     * Input: ERole.ROLE_ADMIN
     * Expected Output: Role with name ROLE_ADMIN
     */
    @Test
    public void findByName_AdminRole_ShouldReturnAdminRole() {
        // Arrange - Use the admin role enum
        ERole roleName = ERole.ROLE_ADMIN;
        
        // Act - Call the service method
        Optional<Role> foundRole = roleService.findByName(roleName);
        
        // Assert - Verify the correct role was returned
        assertTrue("Role should be present", foundRole.isPresent());
        assertEquals("Role name should be ROLE_ADMIN", ERole.ROLE_ADMIN, foundRole.get().getName());
        assertEquals("Role ID should match", adminRole.getId(), foundRole.get().getId());
        
        // Verify database state
        Optional<Role> roleInDb = roleRepository.findById(adminRole.getId());
        assertTrue("Role should exist in database", roleInDb.isPresent());
        assertEquals("Role name in database should be ROLE_ADMIN", ERole.ROLE_ADMIN, roleInDb.get().getName());
    }
    
    /**
     * Test Case ID: TC02
     * Test Objective: Verify that findByName returns the correct role for ROLE_LECTURER
     * Input: ERole.ROLE_LECTURER
     * Expected Output: Role with name ROLE_LECTURER
     */
    @Test
    public void findByName_LecturerRole_ShouldReturnLecturerRole() {
        // Arrange - Use the lecturer role enum
        ERole roleName = ERole.ROLE_LECTURER;
        
        // Act - Call the service method
        Optional<Role> foundRole = roleService.findByName(roleName);
        
        // Assert - Verify the correct role was returned
        assertTrue("Role should be present", foundRole.isPresent());
        assertEquals("Role name should be ROLE_LECTURER", ERole.ROLE_LECTURER, foundRole.get().getName());
        assertEquals("Role ID should match", lecturerRole.getId(), foundRole.get().getId());
        
        // Verify database state
        Optional<Role> roleInDb = roleRepository.findById(lecturerRole.getId());
        assertTrue("Role should exist in database", roleInDb.isPresent());
        assertEquals("Role name in database should be ROLE_LECTURER", ERole.ROLE_LECTURER, roleInDb.get().getName());
    }
    
    /**
     * Test Case ID: TC03
     * Test Objective: Verify that findByName returns the correct role for ROLE_STUDENT
     * Input: ERole.ROLE_STUDENT
     * Expected Output: Role with name ROLE_STUDENT
     */
    @Test
    public void findByName_StudentRole_ShouldReturnStudentRole() {
        // Arrange - Use the student role enum
        ERole roleName = ERole.ROLE_STUDENT;
        
        // Act - Call the service method
        Optional<Role> foundRole = roleService.findByName(roleName);
        
        // Assert - Verify the correct role was returned
        assertTrue("Role should be present", foundRole.isPresent());
        assertEquals("Role name should be ROLE_STUDENT", ERole.ROLE_STUDENT, foundRole.get().getName());
        assertEquals("Role ID should match", studentRole.getId(), foundRole.get().getId());
        
        // Verify database state
        Optional<Role> roleInDb = roleRepository.findById(studentRole.getId());
        assertTrue("Role should exist in database", roleInDb.isPresent());
        assertEquals("Role name in database should be ROLE_STUDENT", ERole.ROLE_STUDENT, roleInDb.get().getName());
    }
    
    /**
     * Test Case ID: TC04
     * Test Objective: Verify that findByName returns empty Optional for non-existent role
     * Input: Null
     * Expected Output: Empty Optional
     */
    @Test
    public void findByName_NullInput_ShouldReturnEmptyOptional() {
        // Arrange - Use null as input
        ERole roleName = null;
        
        // Act - Call the service method
        Optional<Role> foundRole = roleService.findByName(roleName);
        
        // Assert - Verify an empty Optional was returned
        assertFalse("Role should not be present", foundRole.isPresent());
    }
    
    /**
     * Test Case ID: TC05
     * Test Objective: Verify that findByName returns empty Optional when no roles exist in database
     * Input: ERole.ROLE_ADMIN
     * Expected Output: Empty Optional
     */
    @Test
    public void findByName_EmptyDatabase_ShouldReturnEmptyOptional() {
        // Arrange - Clear all roles from database
        roleRepository.deleteAll();
        
        // Act - Call the service method
        Optional<Role> foundRole = roleService.findByName(ERole.ROLE_ADMIN);
        
        // Assert - Verify an empty Optional was returned
        assertFalse("Role should not be present", foundRole.isPresent());
        
        // Verify database state
        long count = roleRepository.count();
        assertEquals("Database should be empty", 0, count);
    }
    
    /**
     * Test Case ID: TC06
     * Test Objective: Verify that findByName correctly handles case when multiple roles with same name exist
     * Input: ERole.ROLE_ADMIN
     * Expected Output: One of the roles with name ROLE_ADMIN
     * Note: This is an edge case that should not occur in a properly constrained database,
     * but we test it to ensure the service behaves predictably even in unexpected scenarios.
     */
    @Test
    public void findByName_DuplicateRoles_ShouldReturnOneRole() {
        // Arrange - Create a duplicate admin role
        Role duplicateAdminRole = new Role(null, ERole.ROLE_ADMIN);
        roleRepository.save(duplicateAdminRole);
        
        // Act - Call the service method
        Optional<Role> foundRole = roleService.findByName(ERole.ROLE_ADMIN);
        
        // Assert - Verify a role was returned
        assertTrue("Role should be present", foundRole.isPresent());
        assertEquals("Role name should be ROLE_ADMIN", ERole.ROLE_ADMIN, foundRole.get().getName());
        
        // Verify database state - should have at least 2 admin roles
        long adminRoleCount = roleRepository.findAll().stream()
                .filter(role -> role.getName() == ERole.ROLE_ADMIN)
                .count();
        assertTrue("Should have at least 2 admin roles in database", adminRoleCount >= 2);
    }
}