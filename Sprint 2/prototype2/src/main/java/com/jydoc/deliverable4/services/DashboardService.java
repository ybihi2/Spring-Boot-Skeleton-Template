package com.jydoc.deliverable4.services;

import com.jydoc.deliverable4.dtos.DashboardDTO;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Service interface for dashboard-related operations.
 * <p>
 * Defines the contract for services that provide dashboard data and functionality
 * to the presentation layer. Implementations should handle the retrieval and
 * processing of user-specific dashboard information.
 * </p>
 */
public interface DashboardService {

    /**
     * Retrieves comprehensive dashboard data for the authenticated user.
     * <p>
     * The implementation should gather all necessary information to populate
     * the user's dashboard view, including:
     * <ul>
     *   <li>User information and profile data</li>
     *   <li>Health conditions and status</li>
     *   <li>Medication information and alerts</li>
     *   <li>Upcoming medication schedules</li>
     * </ul>
     * </p>
     *
     * @param userDetails The authenticated user's details, containing security
     *                    and identification information. Must not be null.
     * @return A fully populated {@link DashboardDTO} containing all dashboard data
     * @throws IllegalArgumentException if userDetails parameter is null
     */
    DashboardDTO getUserDashboardData(UserDetails userDetails);

    /**
     * Checks whether the user has any medications associated with their account.
     * <p>
     * This information is typically used to determine whether to display
     * medication-related UI components or reminders to add medications.
     * </p>
     *
     * @param userDetails The authenticated user's details. Must not be null.
     * @return true if the user has one or more medications, false otherwise
     * @throws IllegalArgumentException if userDetails parameter is null
     */
    boolean hasMedications(UserDetails userDetails);
}