package org.skillbridge.dto.response;

import org.skillbridge.enums.AcademicLevel;

public record CourseResponse(
        Long id, Long departmentId, String departmentName,
        String code, String title, String description,
        AcademicLevel level, int credits, Integer durationHours, boolean active,
        Long prerequisiteCourseId,          // ← new
        String prerequisiteCourseCode,      // ← new
        String prerequisiteCourseTitle      // ← new
) {}